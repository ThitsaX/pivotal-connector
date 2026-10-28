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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.component.vault.Vault;
import okhttp3.OkHttpClient;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class VaultJwsKeyProviderUnitTest {

    private static final String KEY_PATH = "pivotal/jwskey/payerfsp"; // gitleaks:allow

    @Test
    public void failsWhenTheSigningKeySecretIsMissing() {

        assertRefreshFails(
            provider(Optional.empty()),
            "JWS signing key is missing at Vault path '" + KEY_PATH + "'.");
    }

    @Test
    public void failsWhenThePrivateKeyFieldIsMissing() {

        assertRefreshFails(
            provider(Optional.of(new VaultJwsKeyProvider.JwsKeySecret(null))),
            "JWS signing key at Vault path '" + KEY_PATH +
                "' must contain a non-blank privateKey.");
    }

    @Test
    public void failsWhenThePrivateKeyIsBlank() {

        assertRefreshFails(
            provider(Optional.of(new VaultJwsKeyProvider.JwsKeySecret(" "))),
            "JWS signing key at Vault path '" + KEY_PATH +
                "' must contain a non-blank privateKey.");
    }

    private static VaultJwsKeyProvider provider(
        Optional<VaultJwsKeyProvider.JwsKeySecret> secret) {

        return new VaultJwsKeyProvider(new StubVault(secret), "payerfsp", "pivotal/jwskey");
    }

    private static void assertRefreshFails(VaultJwsKeyProvider provider, String message) {

        try {
            provider.refresh();
            fail("Expected Vault JWS key validation to fail.");
        } catch (IllegalStateException e) {
            assertEquals(message, e.getMessage());
        }
    }

    private static final class StubVault extends Vault {

        private final Optional<VaultJwsKeyProvider.JwsKeySecret> secret;

        private StubVault(Optional<VaultJwsKeyProvider.JwsKeySecret> secret) {

            super(new OkHttpClient(), new ObjectMapper(), "https://vault.test", "role",
                  "kubernetes", "secret", "/unused");
            this.secret = secret;
        }

        @Override
        public <T> Optional<T> get(String path, Class<T> template) {

            return this.secret.map(template::cast);
        }

    }

}
