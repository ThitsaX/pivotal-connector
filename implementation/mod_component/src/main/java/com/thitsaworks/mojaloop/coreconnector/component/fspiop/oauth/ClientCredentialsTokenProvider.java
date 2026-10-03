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
package com.thitsaworks.mojaloop.coreconnector.component.fspiop.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Clock;

/**
 * Obtains the bearer token the Hub's API gateway requires on every FSPIOP request.
 * <p>
 * The gateway checks a token as well as the client certificate, so mutual TLS on its own is
 * refused. The token comes from the OAuth client-credentials grant against the Hub's identity
 * provider, for the client the Hub issued to Pivotal.
 * <p>
 * Callbacks are on the transfer path, so one token is shared by every request until shortly before
 * it expires, and a refresh is done by one thread while the others wait for it rather than each
 * fetching their own. The common case — a valid cached token — takes no lock.
 */
public final class ClientCredentialsTokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCredentialsTokenProvider.class);

    /**
     * Refresh this long before the token's stated expiry, so a token is never sent with only moments
     * left and rejected in transit. Capped at half the lifetime, so a short-lived token is still
     * reused rather than refetched on every request.
     */
    private static final long MAX_EXPIRY_SKEW_MS = 30_000L;

    /** Assumed when the identity provider omits {@code expires_in}: short, so a guess is never held long. */
    private static final long DEFAULT_EXPIRES_IN_SECONDS = 60L;

    private final String tokenUrl;

    private final String clientId;

    private final String clientSecret;

    private final OkHttpClient http;

    private final ObjectMapper objectMapper;

    private final Clock clock;

    private volatile Cached cached;

    public ClientCredentialsTokenProvider(String tokenUrl,
                                          String clientId,
                                          String clientSecret,
                                          OkHttpClient http,
                                          ObjectMapper objectMapper,
                                          Clock clock) {

        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.http = http;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public String accessToken() throws IOException {

        Cached current = this.cached;

        if (current != null && current.isValidAt(this.clock.millis())) {
            return current.token;
        }

        synchronized (this) {
            // Re-checked under the lock: the thread that held it may have refreshed already.
            current = this.cached;

            if (current != null && current.isValidAt(this.clock.millis())) {
                return current.token;
            }

            this.cached = fetch();
            return this.cached.token;
        }
    }

    /**
     * Drops the cached token, but only if it is still the one that was rejected. Several requests
     * can be refused with the same token at once; without the comparison, the later ones would
     * discard the replacement the first had already fetched.
     */
    public synchronized void invalidate(String rejectedToken) {

        if (this.cached != null && this.cached.token.equals(rejectedToken)) {
            this.cached = null;
        }
    }

    private Cached fetch() throws IOException {

        Request request = new Request.Builder()
                              .url(this.tokenUrl)
                              .post(new FormBody.Builder()
                                        .add("grant_type", "client_credentials")
                                        .add("client_id", this.clientId)
                                        .add("client_secret", this.clientSecret)
                                        .build())
                              .build();

        JsonNode json;

        try (Response response = this.http.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                // The status only. The response body is the identity provider's to word, and the
                // request carried the client secret, so neither is repeated here.
                throw new IOException(
                    "Could not obtain a Hub access token for client '" + this.clientId + "' (HTTP "
                        + response.code() + ").");
            }

            ResponseBody body = response.body();
            json = this.objectMapper.readTree(body == null ? "{}" : body.string());
        }

        String token = json.path("access_token").asText("");

        if (token.isEmpty()) {
            throw new IOException("No access_token returned for client '" + this.clientId + "'.");
        }

        long lifetimeMs = json.path("expires_in").asLong(DEFAULT_EXPIRES_IN_SECONDS) * 1_000L;
        long skewMs = Math.min(MAX_EXPIRY_SKEW_MS, lifetimeMs / 2);

        LOG.info("Obtained a Hub access token for client '{}'.", this.clientId);

        return new Cached(token, this.clock.millis() + lifetimeMs - skewMs);
    }

    private static final class Cached {

        private final String token;

        private final long refreshAtMs;

        private Cached(String token, long refreshAtMs) {

            this.token = token;
            this.refreshAtMs = refreshAtMs;
        }

        private boolean isValidAt(long nowMs) {

            return nowMs < this.refreshAtMs;
        }

    }

}
