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

package com.thitsaworks.mojaloop.coreconnector.component.vault;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;

/** Creates the shared Vault client used by connector infrastructure. */
public class VaultConfiguration {

    @Bean
    public Vault vault(Settings settings, OkHttpClient http, ObjectMapper objectMapper) {

        return new Vault(
            http, objectMapper, settings.vaultUrl(), settings.vaultRole(),
            settings.vaultKubernetesAuthPath(), settings.vaultKvMount(),
            settings.vaultServiceAccountTokenPath());
    }

    public record Settings(String vaultUrl,
                           String vaultRole,
                           String vaultKubernetesAuthPath,
                           String vaultKvMount,
                           String vaultServiceAccountTokenPath) { }

}
