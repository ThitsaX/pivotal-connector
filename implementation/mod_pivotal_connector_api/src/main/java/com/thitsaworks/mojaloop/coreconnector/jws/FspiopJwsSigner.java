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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.FspiopSigningInterceptor;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.JwsKeyProvider;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.StaticJwsKeyProvider;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.VaultJwsKeyProvider;
import com.thitsaworks.mojaloop.coreconnector.component.vault.VaultClient;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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

    private final CoreConnectorConfiguration.Settings config;

    private final OkHttpClient http;

    private final ObjectMapper objectMapper;

    private JwsKeyProvider keyProvider;

    private Interceptor interceptor;

    public FspiopJwsSigner(CoreConnectorConfiguration.Settings config,
                           OkHttpClient http,
                           ObjectMapper objectMapper) {

        this.config = config;
        this.http = http;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterPropertiesSet() {

        if (!this.config.isFspiopUseJws()) {
            LOG.info("FSPIOP JWS signing is disabled; outbound callbacks will be sent unsigned.");
            this.interceptor = new FspiopSigningInterceptor(new StaticJwsKeyProvider(this.config.getConnectorId(), null), false);
            return;
        }

        VaultClient.Settings vaultSettings = new VaultClient.Settings(
            this.config.getVaultUrl(),
            this.config.getVaultRole(),
            this.config.getVaultKubernetesAuthPath(),
            this.config.getVaultKvMount(),
            this.config.getVaultServiceAccountTokenPath());

        if (!vaultSettings.isConfigured()) {
            // Enabling signing without somewhere to get a key is a misconfiguration, not a state to
            // degrade through: it would send unsigned traffic while the operator believed otherwise.
            throw new IllegalStateException(
                "fspiopUseJws is enabled but Vault is not configured. Set vaultUrl and vaultRole.");
        }

        VaultClient vaultClient = new VaultClient(this.http, this.objectMapper, vaultSettings);

        this.keyProvider = new VaultJwsKeyProvider(vaultClient,
                                                   this.config.getConnectorId(),
                                                   this.config.getVaultJwsKeyPathPrefix());
        this.keyProvider.refresh();

        this.interceptor = new FspiopSigningInterceptor(this.keyProvider, true);
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
