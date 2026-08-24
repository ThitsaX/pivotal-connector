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
package com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws;

import com.thitsaworks.mojaloop.coreconnector.component.vault.VaultClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.util.Optional;

/**
 * Reads this connector's JWS private key from Vault KV — the KMS-backed profile.
 * <p>
 * One connector, one tenant, one Vault path: {@code <mount>/pivotal/jwskey/<fspId>}, with the Vault
 * policy granting exactly that path. In this profile <em>that policy is the isolation boundary</em>:
 * with no HSM there is no non-exportable handle, so the key is held in process memory and per-tenant
 * path scoping is what keeps "a compromised connector signs as one DFSP" true.
 * <p>
 * Read once at startup and cached. Vault is never on the signing path.
 * <p>
 * Not environment variables, deliberately: those sit in the Deployment manifest, appear in
 * {@code kubectl describe}, need a redeploy to rotate, and leave no record of who read them. Vault
 * gives short-lived tokens, per-path policy, and an audit line per read.
 */
public class VaultJwsKeyProvider implements JwsKeyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(VaultJwsKeyProvider.class);

    private static final String KEY_FIELD = "privateKey";

    private final VaultClient vaultClient;

    private final String fspId;

    private final String keyPathPrefix;

    private volatile PrivateKey signingKey;

    public VaultJwsKeyProvider(VaultClient vaultClient, String fspId, String keyPathPrefix) {

        this.vaultClient = vaultClient;
        this.fspId = fspId;
        this.keyPathPrefix = keyPathPrefix;
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
     * Loads the key. A missing secret leaves signing disabled rather than failing startup: a
     * connector that cannot sign still processes traffic, and peers ignore signatures until they
     * enable verification. A malformed key is a different matter and does throw — that is a
     * misconfiguration which would otherwise surface as silent unsigned traffic.
     */
    @Override
    public void refresh() {

        String path = this.keyPathPrefix + "/" + this.fspId;
        String token = this.vaultClient.login();

        Optional<String> pem = this.vaultClient.readKvField(token, path, KEY_FIELD);

        if (pem.isEmpty()) {
            this.signingKey = null;
            LOG.warn("No JWS signing key at Vault path '{}' field '{}'. Outbound FSPIOP callbacks "
                         + "will be sent unsigned.", path, KEY_FIELD);
            return;
        }

        this.signingKey = Pem.readPrivateKey(pem.get());

        LOG.info("Loaded the JWS signing key for '{}' from Vault path '{}'", this.fspId, path);
    }

}
