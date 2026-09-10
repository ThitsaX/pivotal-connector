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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Access to Vault using Kubernetes ServiceAccount authentication and typed KV v2 reads.
 * <p>
 * The shape follows the operation portal's Vault module: callers depend on one configured
 * {@code Vault} object and request a secret by path and type. Authentication remains connector
 * specific: each read exchanges the pod's ServiceAccount token for a short-lived Vault token.
 * <p>
 * Callers read at startup or on an explicit refresh and cache the result. Vault is never on the
 * payment signing path.
 */
public class Vault {

    private static final Logger LOG = LoggerFactory.getLogger(Vault.class);

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http;

    private final ObjectMapper objectMapper;

    private final String address;

    private final String role;

    private final String kubernetesAuthPath;

    private final String enginePath;

    private final String serviceAccountTokenPath;

    private final Function<String, VaultKeyValueOperations> keyValueOperations;

    public Vault(OkHttpClient http,
                 ObjectMapper objectMapper,
                 String address,
                 String role,
                 String kubernetesAuthPath,
                 String enginePath,
                 String serviceAccountTokenPath) {

        this(
            http, objectMapper, address, role, kubernetesAuthPath, enginePath,
            serviceAccountTokenPath,
            token -> new VaultTemplate(
                VaultEndpoint.from(URI.create(address)),
                new TokenAuthentication(token)).opsForKeyValue(
                enginePath, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2));
    }

    Vault(OkHttpClient http,
          ObjectMapper objectMapper,
          String address,
          String role,
          String kubernetesAuthPath,
          String enginePath,
          String serviceAccountTokenPath,
          Function<String, VaultKeyValueOperations> keyValueOperations) {

        this.http = http;
        this.objectMapper = objectMapper;
        this.address = address;
        this.role = role;
        this.kubernetesAuthPath = kubernetesAuthPath;
        this.enginePath = enginePath;
        this.serviceAccountTokenPath = serviceAccountTokenPath;
        this.keyValueOperations = keyValueOperations;
    }

    public boolean isConfigured() {

        return this.address != null && !this.address.isBlank() && this.role != null &&
                   !this.role.isBlank();
    }

    /** Exchanges the pod's ServiceAccount token for a short-lived Vault token. */
    public String login() {

        String serviceAccountToken = readServiceAccountToken();
        String url = this.address + "/v1/auth/" + this.kubernetesAuthPath + "/login";
        String body = writeJson(Map.of("role", this.role, "jwt", serviceAccountToken));

        Request request = new Request.Builder()
                              .url(url)
                              .post(RequestBody.create(body, JSON))
                              .build();

        JsonNode response = execute(request, "Vault login");
        String token = response.path("auth").path("client_token").asText(null);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Vault login returned no client_token.");
        }

        LOG.info("Authenticated to Vault at {} as role '{}'", this.address, this.role);
        return token;
    }

    /**
     * Reads and maps one KV v2 secret.
     *
     * @param path     path below the configured engine, for example {@code pivotal/jwskey/payerfsp}
     * @param template type used to map the secret's fields
     * @return the mapped secret, or empty when Vault returns 404
     */
    public <T> Optional<T> get(String path, Class<T> template) {

        String token = login();
        VaultResponseSupport<T> response = this.keyValueOperations.apply(token).get(path, template);

        if (response == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.getData());
    }

    private String readServiceAccountToken() {

        Path path = Path.of(this.serviceAccountTokenPath);
        try {
            return Files.readString(path).trim();
        } catch (Exception e) {
            throw new IllegalStateException(
                "Cannot read the Kubernetes ServiceAccount token at " + path + ".", e);
        }
    }

    private JsonNode execute(Request request, String description) {

        try (Response response = http.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();

            if (response.code() == 404) {
                return null;
            }
            if (!response.isSuccessful()) {
                throw new IllegalStateException(
                    description + " failed: HTTP " + response.code() + " " + body);
            }

            return objectMapper.readTree(body);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(description + " failed.", e);
        }
    }

    private String writeJson(Map<String, String> value) {

        try {
            return objectMapper.writeValueAsString(value);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize a Vault request.", e);
        }
    }

}
