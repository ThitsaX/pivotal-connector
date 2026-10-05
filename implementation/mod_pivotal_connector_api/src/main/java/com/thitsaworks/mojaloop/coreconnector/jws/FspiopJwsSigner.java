/*
 * Copyright (c) 2024-2026 ThitsaWorks Pte. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thitsaworks.mojaloop.coreconnector.jws;

import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.FspiopSigningInterceptor;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.key.JwsKeyProvider;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.key.CloudHsmJwsKeyProvider;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.key.StaticJwsKeyProvider;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.key.VaultJwsKeyProvider;
import com.thitsaworks.mojaloop.coreconnector.component.vault.Vault;
import okhttp3.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Assembles the FSPIOP JWS signing path and exposes it as an OkHttp interceptor.
 * <p>
 * A component-scanned {@code @Component} rather than a {@code @Bean} on
 * {@link CoreConnectorConfiguration}, deliberately. Deployable connectors exclude that configuration
 * from scanning and import their own; some extend it and inherit its beans, others do not — so a
 * {@code @Bean} declared there reaches only part of the fleet. Every deployable scans
 * {@code com.thitsaworks.mojaloop.coreconnector}, so a component here reaches all of them without
 * any deployable repo changing.
 * <p>
 * The key is read from Vault <strong>once at startup</strong> and cached. Vault is never on the
 * signing path: it can be down and payments continue.
 */
@Component
public class FspiopJwsSigner implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(FspiopJwsSigner.class);

    private static final String VAULT_KV = "vault-kv";

    private static final String PKCS11 = "pkcs11";

    private final CoreConnectorConfiguration.Settings config;

    private final Vault vault;

    private JwsKeyProvider keyProvider;

    private Interceptor interceptor;

    public FspiopJwsSigner(CoreConnectorConfiguration.Settings config, @Nullable Vault vault) {

        this.config = config;
        this.vault = vault;
    }

    @Override
    public void afterPropertiesSet() {

        if (!this.config.isFspiopUseJws()) {
            LOG.info("FSPIOP JWS signing is disabled; outbound callbacks will be sent unsigned.");
            this.interceptor = new FspiopSigningInterceptor(
                new StaticJwsKeyProvider(this.config.getConnectorId(), null), false);
            return;
        }

        if (this.vault == null || !this.vault.isConfigured()) {
            // Enabling signing without somewhere to get a key is a misconfiguration, not a state to
            // degrade through: it would send unsigned traffic while the operator believed otherwise.
            throw new IllegalStateException(
                "fspiopUseJws is enabled but Vault is not configured. Set vaultUrl and vaultRole.");
        }

        this.keyProvider = createKeyProvider();
        this.keyProvider.refresh();

        this.interceptor = new FspiopSigningInterceptor(this.keyProvider, true);
    }

    /**
     * Chooses where this connector's key lives, and therefore where signing happens.
     * <p>
     * An unrecognised value throws rather than defaulting. Falling back would decide key custody on
     * the basis of a typo, and the wrong answer is not a degraded mode — it is a private key in
     * process memory in a deployment that chose hardware to prevent exactly that.
     */
    private JwsKeyProvider createKeyProvider() {

        String provider = this.config.getKeyProvider() == null
            ? VAULT_KV
            : this.config.getKeyProvider().trim().toLowerCase(java.util.Locale.ROOT);

        if (VAULT_KV.equals(provider)) {
            return new VaultJwsKeyProvider(
                this.vault, this.config.getConnectorId(),
                this.config.getVaultJwsKeyPathPrefix());
        }

        if (PKCS11.equals(provider)) {

            if (this.config.getHsmCredPath() == null || this.config.getHsmCredPath().isBlank()) {
                // This connector signs, so it needs its own crypto user. An absent path would
                // leave it unable to log in and every callback unsigned.
                throw new IllegalStateException(
                    "keyProvider is 'pkcs11' but hsmCredPath is not set. Set it to the Vault path "
                    + "holding this tenant's crypto-user credential.");
            }

            // The setting names the custody model, not the interface. The services written in
            // TypeScript do reach the device through PKCS#11; this one cannot, because the JDK's
            // PKCS#11 keystore needs a certificate the device will not store -- so it uses the
            // vendor's own provider. Same profile, same key, different API.
            return new CloudHsmJwsKeyProvider(
                this.vault, this.config.getConnectorId(),
                this.config.getHsmCredPath(),
                this.config.getKeyRefPathPrefix());
        }

        throw new IllegalStateException(
            "Unrecognised keyProvider '" + this.config.getKeyProvider() + "'. Expected '"
            + VAULT_KV + "' or '" + PKCS11 + "'.");
    }

    /** Install on any OkHttp client that carries outbound FSPIOP traffic. */
    public Interceptor interceptor() {

        return this.interceptor;
    }

    /** Re-reads the signing key. For a rotation nudge — never per request. */
    public void refresh() {

        if (this.keyProvider != null) {
            this.keyProvider.refresh();
        }
    }

}
