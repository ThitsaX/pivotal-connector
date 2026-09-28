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

package com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.key;

import com.thitsaworks.mojaloop.coreconnector.component.vault.Vault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.util.Optional;

/**
 * Signs through a key held inside a PKCS#11 device — the HSM-backed profile.
 * <p>
 * No key material is present in this process. What it holds is a crypto-user credential and a key
 * reference: enough to <em>ask</em> the device to sign while the credential lasts, never enough to
 * sign without it. A heap dump yields nothing a peer would accept.
 * <p>
 * <strong>Both values come from Vault, never from configuration.</strong> A credential in the
 * environment sits in the Deployment manifest, appears in {@code kubectl describe}, needs a redeploy
 * to rotate, and leaves no record of who read it. Read once at startup and cached, so Vault is not
 * on the signing path and can be down while payments continue.
 * <p>
 * <strong>One connector, one tenant, one credential.</strong> This logs in as that tenant's own
 * crypto user — the one that owns the key — so a compromised connector can sign as one DFSP. That
 * is the same boundary the Vault profile draws with path policy, drawn here by the device instead.
 */
public class Pkcs11JwsKeyProvider implements JwsKeyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(Pkcs11JwsKeyProvider.class);

    /**
     * The device authenticates with a username and a password; a software module takes a PIN alone
     * and ignores the username. Both are carried so that moving to hardware changes the module path
     * and nothing else — not the shape of the secret, and not this class.
     */
    private record HsmCredential(String username, String password) {

        String pin() {

            return this.username == null || this.username.isBlank()
                ? this.password
                : this.username + ":" + this.password;
        }
    }

    private record KeyRef(String keyRef) { }

    private final Vault vault;

    private final String fspId;

    private final String modulePath;

    private final int slotListIndex;

    private final String credentialPath;

    private final String keyRefPathPrefix;

    private volatile PrivateKey signingKey;

    private volatile Provider provider;

    public Pkcs11JwsKeyProvider(Vault vault,
                                String fspId,
                                String modulePath,
                                int slotListIndex,
                                String credentialPath,
                                String keyRefPathPrefix) {

        this.vault = vault;
        this.fspId = fspId;
        this.modulePath = modulePath;
        this.slotListIndex = slotListIndex;
        this.credentialPath = credentialPath;
        this.keyRefPathPrefix = keyRefPathPrefix;
    }

    @Override
    public String fspId() {

        return this.fspId;
    }

    @Override
    public PrivateKey signingKey() {

        return this.signingKey;
    }

    /**
     * The device's own provider, which is the only one that can use this key.
     * <p>
     * {@code Signature.getInstance(algorithm)} selects by algorithm and would return the software
     * implementation, which rejects an opaque key it cannot read.
     */
    @Override
    public Provider signingProvider() {

        return this.provider;
    }

    /**
     * Opens the device, logs in as this tenant, and resolves the key its reference names.
     * <p>
     * A missing credential or reference leaves signing disabled rather than failing startup,
     * matching the Vault profile: a connector that cannot sign still processes traffic, and peers
     * ignore signatures until they enable verification. A credential that is present but rejected
     * is a different matter and does throw — that is a misconfiguration which would otherwise
     * surface as silently unsigned traffic.
     */
    @Override
    public void refresh() {

        Optional<HsmCredential> credential = this.vault.get(this.credentialPath, HsmCredential.class);

        if (credential.isEmpty() || credential.get().password() == null
            || credential.get().password().isBlank()) {

            this.signingKey = null;
            this.provider = null;
            LOG.warn("No crypto-user credential at Vault path '{}'. Outbound FSPIOP callbacks will "
                     + "be sent unsigned.", this.credentialPath);
            return;
        }

        String keyRefPath = this.keyRefPathPrefix + "/" + this.fspId;
        Optional<KeyRef> keyRef = this.vault.get(keyRefPath, KeyRef.class);

        if (keyRef.isEmpty() || keyRef.get().keyRef() == null || keyRef.get().keyRef().isBlank()) {
            this.signingKey = null;
            this.provider = null;
            LOG.warn("No key reference at Vault path '{}'. The crypto user exists but no key has "
                     + "been provisioned; callbacks will be sent unsigned.", keyRefPath);
            return;
        }

        load(credential.get(), keyRef.get().keyRef());
    }

    private void load(HsmCredential credential, String label) {

        try {
            Provider configured = configureProvider();

            // The key store is how the JDK surfaces a logged-in token's objects. Logging in here
            // rather than per signature is what keeps the device off the request path for
            // authentication -- a session is established once and reused.
            KeyStore keyStore = KeyStore.getInstance("PKCS11", configured);
            keyStore.load(null, credential.pin().toCharArray());

            PrivateKey key = (PrivateKey) keyStore.getKey(label, null);

            if (key == null) {
                // Distinct from an absent reference: the reference exists and names nothing this
                // crypto user can see. Either it is stale, or the key was never shared with it.
                throw new IllegalStateException(
                    "No key labelled '" + label + "' is visible to this crypto user. Either the "
                    + "reference is stale, or the key was generated by another user and not shared.");
            }

            this.provider = configured;
            this.signingKey = key;

            LOG.info("Signing for '{}' through PKCS#11, key reference '{}'", this.fspId, label);

        } catch (Exception e) {
            // Thrown rather than swallowed. A credential that is present but rejected means the
            // deployment is misconfigured, and continuing would send unsigned traffic while an
            // operator believed otherwise.
            throw new IllegalStateException(
                "Failed to open the PKCS#11 key for '" + this.fspId + "'.", e);
        }
    }

    /**
     * Installs a provider bound to this module and slot.
     * <p>
     * Configured in memory rather than from a file on disk, because the only values it carries are
     * ones this class already holds — writing them out would add a file to keep in step and a path
     * to get wrong. The name is per-tenant so two providers in one JVM cannot collide.
     * <p>
     * <strong>Selected by slot index, not by token label.</strong> The JDK's PKCS#11 provider has
     * no label option — it takes {@code slot} or {@code slotListIndex} only. That is an asymmetry
     * with the services that find their token by name, and it matters where a module presents one
     * token per tenant: there the index decides which tenant's token this connector opens, and
     * indexes are assigned by the module rather than chosen. A device presenting a single token,
     * which is the hardware case, is always index 0.
     */
    private Provider configureProvider() {

        String name = "PivotalHsm-" + this.fspId;
        Provider existing = Security.getProvider("SunPKCS11-" + name);

        if (existing != null) {
            return existing;
        }

        String config = "--"
            + "name = " + name + "\n"
            + "library = " + this.modulePath + "\n"
            + "slotListIndex = " + this.slotListIndex + "\n";

        Provider configured = Security.getProvider("SunPKCS11").configure(config);

        Security.addProvider(configured);

        return configured;
    }

}
