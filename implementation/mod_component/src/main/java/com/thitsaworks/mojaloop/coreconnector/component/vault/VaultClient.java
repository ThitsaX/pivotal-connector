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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * A deliberately small Vault client: Kubernetes ServiceAccount login, and KV v2 reads.
 * <p>
 * No Vault SDK dependency. This artifact is inherited by every connector, and the two calls needed
 * here are a POST and a GET over the OkHttp already in the tree — a full SDK would be a large
 * transitive surface for that.
 * <p>
 * <strong>Not on the signing path.</strong> Callers read once at startup and cache, so Vault can be
 * down and payments continue. Never call this per request.
 */
public class VaultClient {

    private static final Logger LOG = LoggerFactory.getLogger(VaultClient.class);

    /** Where the kubelet projects the pod's ServiceAccount token. */
    public static final String DEFAULT_SERVICE_ACCOUNT_TOKEN_PATH =
        "/var/run/secrets/kubernetes.io/serviceaccount/token";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static final String VAULT_TOKEN_HEADER = "X-Vault-Token";

    private final OkHttpClient http;

    private final ObjectMapper objectMapper;

    private final Settings settings;

    public VaultClient(OkHttpClient http, ObjectMapper objectMapper, Settings settings) {

        this.http = http;
        this.objectMapper = objectMapper;
        this.settings = settings;
    }

    /**
     * Exchanges the pod's ServiceAccount token for a short-lived Vault token.
     * <p>
     * Chosen over AppRole or a mounted token because the credential is the pod's own identity: there
     * is nothing to distribute, nothing to rotate, and the identity is already per-tenant.
     */
    public String login() {

        String serviceAccountToken = readServiceAccountToken();

        String url = settings.address() + "/v1/auth/" + settings.kubernetesAuthPath() + "/login";
        String body = writeJson(Map.of("role", settings.role(), "jwt", serviceAccountToken));

        Request request = new Request.Builder().url(url)
                                               .post(RequestBody.create(body, JSON))
                                               .build();

        JsonNode response = execute(request, "Vault login");

        String token = response.path("auth")
                               .path("client_token")
                               .asText(null);

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Vault login returned no client_token.");
        }

        LOG.info("Authenticated to Vault at {} as role '{}'", settings.address(), settings.role());

        return token;
    }

    /**
     * Reads one field from a KV v2 secret.
     *
     * @param token     a Vault token from {@link #login()}
     * @param path      path below the mount, e.g. {@code pivotal/jwskey/payerfsp}
     * @param field     field within the secret
     * @return the value, or empty when the secret or the field is absent
     */
    public Optional<String> readKvField(String token, String path, String field) {

        // KV v2 inserts /data/ between the mount and the path, and nests the payload under data.data.
        String url = settings.address() + "/v1/" + settings.kvMount() + "/data/" + path;

        Request request = new Request.Builder().url(url)
                                               .header(VAULT_TOKEN_HEADER, token)
                                               .get()
                                               .build();

        JsonNode response = execute(request, "Vault read of " + path);

        if (response == null) {
            return Optional.empty();
        }

        JsonNode value = response.path("data")
                                 .path("data")
                                 .path(field);

        return value.isMissingNode() || value.isNull() ? Optional.empty() : Optional.of(value.asText());
    }

    private String readServiceAccountToken() {

        Path path = Path.of(settings.serviceAccountTokenPath());

        try {
            return Files.readString(path)
                        .trim();
        } catch (Exception e) {
            throw new IllegalStateException(
                "Cannot read the Kubernetes ServiceAccount token at " + path
                    + ". Outside Kubernetes, configure a different key provider rather than pointing this elsewhere.",
                e);
        }
    }

    /**
     * @return the parsed body, or {@code null} on 404 — an absent secret is a normal answer, not a
     *     failure, so callers can distinguish "not configured" from "Vault is broken"
     */
    private JsonNode execute(Request request, String description) {

        try (Response response = http.newCall(request)
                                     .execute()) {

            String body = response.body() == null ? "" : response.body()
                                                                 .string();

            if (response.code() == 404) {
                return null;
            }

            if (!response.isSuccessful()) {
                // Vault echoes no secret material in errors, but the body can name paths and policies.
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

    /**
     * @param address                 Vault base URL, e.g. {@code https://vault.internal:8200}
     * @param role                    Vault Kubernetes auth role bound to this connector's ServiceAccount
     * @param kubernetesAuthPath      mount path of the Kubernetes auth method, usually {@code kubernetes}
     * @param kvMount                 KV v2 mount, usually {@code secret}
     * @param serviceAccountTokenPath where the pod's token is projected
     */
    public record Settings(String address,
                           String role,
                           String kubernetesAuthPath,
                           String kvMount,
                           String serviceAccountTokenPath) {

        public boolean isConfigured() {

            return address != null && !address.isBlank() && role != null && !role.isBlank();
        }
    }

}
