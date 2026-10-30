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

import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CloudHsmJwsKeyProviderUnitTest {

    private static final String CREDENTIAL_PATH = "pivotal/cloudhsm/credentials/payerfsp";

    private static final String KEY_REF_PATH = "pivotal/keyref/payerfsp"; // gitleaks:allow

    @Test
    public void failsWhenTheCredentialSecretIsMissing() {

        CloudHsmJwsKeyProvider provider = provider(Map.of());

        assertRefreshFails(
            provider,
            "CloudHSM credential is missing at Vault path '" + CREDENTIAL_PATH + "'.");
    }

    @Test
    public void failsWhenTheUsernameIsBlank() {

        CloudHsmJwsKeyProvider provider = provider(Map.of(
            CREDENTIAL_PATH, new CloudHsmJwsKeyProvider.HsmCredential(" ", "password")));

        assertRefreshFails(
            provider,
            "CloudHSM credential at Vault path '" + CREDENTIAL_PATH +
                "' must contain non-blank username and password.");
    }

    @Test
    public void failsWhenThePasswordIsBlank() {

        CloudHsmJwsKeyProvider provider = provider(Map.of(
            CREDENTIAL_PATH, new CloudHsmJwsKeyProvider.HsmCredential("crypto-user", "")));

        assertRefreshFails(
            provider,
            "CloudHSM credential at Vault path '" + CREDENTIAL_PATH +
                "' must contain non-blank username and password.");
    }

    @Test
    public void failsWhenTheKeyReferenceSecretIsMissing() {

        CloudHsmJwsKeyProvider provider = provider(Map.of(
            CREDENTIAL_PATH, new CloudHsmJwsKeyProvider.HsmCredential("crypto-user", "password")));

        assertRefreshFails(
            provider,
            "CloudHSM key reference is missing at Vault path '" + KEY_REF_PATH + "'.");
    }

    @Test
    public void failsWhenTheKeyReferenceIsBlank() {

        CloudHsmJwsKeyProvider provider = provider(Map.of(
            CREDENTIAL_PATH, new CloudHsmJwsKeyProvider.HsmCredential("crypto-user", "password"),
            KEY_REF_PATH, new CloudHsmJwsKeyProvider.KeyRef(" ")));

        assertRefreshFails(
            provider,
            "CloudHSM key reference at Vault path '" + KEY_REF_PATH +
                "' must contain a non-blank keyRef.");
    }

    private static CloudHsmJwsKeyProvider provider(Map<String, Object> secrets) {

        return new CloudHsmJwsKeyProvider(
            new StubVault(secrets), "payerfsp", CREDENTIAL_PATH,
            "pivotal/keyref"); // gitleaks:allow
    }

    private static void assertRefreshFails(CloudHsmJwsKeyProvider provider, String message) {

        try {
            provider.refresh();
            fail("Expected CloudHSM configuration validation to fail.");
        } catch (IllegalStateException e) {
            assertEquals(message, e.getMessage());
        }
    }

    private static final class StubVault extends Vault {

        private final Map<String, Object> secrets;

        private StubVault(Map<String, Object> secrets) {

            super(new OkHttpClient(), new ObjectMapper(), "https://vault.test", "role",
                  "kubernetes", "secret", "/unused");
            this.secrets = secrets;
        }

        @Override
        public <T> Optional<T> get(String path, Class<T> template) {

            return Optional.ofNullable(this.secrets.get(path)).map(template::cast);
        }

    }

}
