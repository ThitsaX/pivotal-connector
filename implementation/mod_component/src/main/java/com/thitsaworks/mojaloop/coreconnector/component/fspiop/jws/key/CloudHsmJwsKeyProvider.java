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

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.util.Optional;

/**
 * Signs through a key held inside a CloudHSM cluster — the HSM-backed profile.
 * <p>
 * No key material is present in this process. What it holds is a crypto-user credential and a key
 * reference: enough to <em>ask</em> the device to sign while the credential lasts, never enough to
 * sign without it.
 * <p>
 * <strong>Both values come from Vault, never from configuration.</strong> A credential in the
 * environment sits in the Deployment manifest, appears in {@code kubectl describe}, needs a redeploy
 * to rotate, and leaves no record of who read it. Read once at startup and cached, so Vault is not
 * on the signing path and can be down while payments continue.
 *
 * <h2>Why the vendor's provider rather than the JDK's PKCS#11 one</h2>
 *
 * The services written in TypeScript reach the same device through PKCS#11 directly, and this class
 * cannot. {@code SunPKCS11} surfaces a private key only where a <em>certificate</em> on the token is
 * paired with it, and CloudHSM stores keys but not certificates — {@code C_CreateObject} for one is
 * refused, as is setting the attribute the JDK pairs on. So the JDK's keystore is permanently empty
 * against this device, whatever the key's label. AWS ships this provider for exactly that reason;
 * it resolves a key by label with no certificate involved.
 * <p>
 * <strong>What that costs.</strong> This class is CloudHSM-specific, where the PKCS#11 path is not.
 * A software module cannot stand in for local testing, and another vendor's device would need
 * another implementation. The connector therefore gives up the portability the rest of the profile
 * keeps.
 *
 * <h2>One connector, one tenant</h2>
 *
 * Credentials reach the provider through <strong>system properties</strong>, which are process-wide
 * — so a JVM holds exactly one crypto user. That suits a connector, which <em>is</em> one
 * participant, and is why the same provider would not serve something signing as several. They are
 * set from what Vault returned rather than from the process environment, so the password is never
 * in a manifest.
 */
public class CloudHsmJwsKeyProvider implements JwsKeyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CloudHsmJwsKeyProvider.class);

    private static final String PROVIDER_CLASS = "com.amazonaws.cloudhsm.jce.provider.CloudHsmProvider";

    /** What the vendor's provider reads its credential from. Process-wide; see the class note. */
    private static final String USER_PROPERTY = "HSM_USER";

    private static final String PASSWORD_PROPERTY = "HSM_PASSWORD";

    private record HsmCredential(String username, String password) { }

    private record KeyRef(String keyRef) { }

    private final Vault vault;

    private final String fspId;

    private final String credentialPath;

    private final String keyRefPathPrefix;

    private volatile PrivateKey signingKey;

    private volatile Provider provider;

    public CloudHsmJwsKeyProvider(Vault vault,
                                  String fspId,
                                  String credentialPath,
                                  String keyRefPathPrefix) {

        this.vault = vault;
        this.fspId = fspId;
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
     * Opens the device as this tenant and resolves the key its reference names.
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

            disable("No crypto-user credential at Vault path '" + this.credentialPath + "'.");
            return;
        }

        String keyRefPath = this.keyRefPathPrefix + "/" + this.fspId;
        Optional<KeyRef> keyRef = this.vault.get(keyRefPath, KeyRef.class);

        if (keyRef.isEmpty() || keyRef.get().keyRef() == null || keyRef.get().keyRef().isBlank()) {
            disable("No key reference at Vault path '" + keyRefPath + "'. The crypto user exists "
                    + "but no key has been provisioned.");
            return;
        }

        load(credential.get(), keyRef.get().keyRef());
    }

    private void disable(String reason) {

        this.signingKey = null;
        this.provider = null;
        LOG.warn("{} Outbound FSPIOP callbacks will be sent unsigned.", reason);
    }

    private void load(HsmCredential credential, String label) {

        try {
            Provider configured = installProvider(credential);

            KeyStore keyStore = KeyStore.getInstance("CloudHSM", configured);
            keyStore.load(null, null);

            PrivateKey key = (PrivateKey) keyStore.getKey(label, null);

            if (key == null) {
                // Distinct from an absent reference: the reference exists and names nothing this
                // crypto user can see. Either it is stale, or the key was generated by another
                // user and never shared with this one.
                throw new IllegalStateException(
                    "No key labelled '" + label + "' is visible to this crypto user. Either the "
                    + "reference is stale, or the key was never shared with it.");
            }

            this.provider = configured;
            this.signingKey = key;

            LOG.info("Signing for '{}' through CloudHSM, key reference '{}'", this.fspId, label);

        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to open the CloudHSM key for '" + this.fspId + "'.", e);
        }
    }

    /**
     * Installs the vendor's provider, reflectively.
     * <p>
     * Not a compile-time dependency because the library is not published to a public repository —
     * it arrives with the device SDK, installed into the runtime image. Depending on it directly
     * would make this module's build fetch a vendor package for one optional class, on machines
     * that have no device to talk to. Reflection keeps the build unchanged and turns an absent
     * library into a clear message at startup rather than a link error.
     */
    private Provider installProvider(HsmCredential credential) throws Exception {

        // Set before construction: the provider reads them while it connects. Process-wide, which
        // is why one JVM serves one crypto user.
        System.setProperty(USER_PROPERTY, credential.username());
        System.setProperty(PASSWORD_PROPERTY, credential.password());

        Provider existing = Security.getProvider("CloudHSM");

        if (existing != null) {
            return existing;
        }

        Class<?> type;

        try {
            // Resolved without initialising, so that a jar which is present but unusable is
            // reported as what it is. Initialising loads the SDK's native library, and a missing
            // one would otherwise surface here as though the class itself were absent -- sending
            // whoever reads it to look for the wrong thing.
            type = Class.forName(PROVIDER_CLASS, false, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                "The CloudHSM JCE provider is not on the classpath. It ships with the device SDK, "
                + "must be installed in the image, and -- because it lives outside the application "
                + "archive -- must be named on the classpath at launch.", e);
        }

        Provider configured;

        try {
            configured = (Provider) type.getDeclaredConstructor().newInstance();
        } catch (ExceptionInInitializerError | NoClassDefFoundError e) {
            // The class is there and cannot start. Almost always the SDK's native library is
            // missing, or its configuration still carries the placeholder address it ships with.
            throw new IllegalStateException(
                "The CloudHSM JCE provider is on the classpath but failed to initialise. Check "
                + "that the full device SDK is installed, not only its jar, and that its "
                + "configuration names the cluster address.", e);
        }

        Security.addProvider(configured);

        return configured;
    }

}
