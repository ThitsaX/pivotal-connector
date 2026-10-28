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

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FspiopAccessTokenUnitTest {

    private static final MediaType JSON = MediaType.get("application/json");

    private static final String SECRET = "very-secret";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** A clock the test moves by hand. */
    private static final class ManualClock extends Clock {

        private long millis = 1_000_000L;

        @Override
        public ZoneId getZone() {

            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {

            return this;
        }

        @Override
        public Instant instant() {

            return Instant.ofEpochMilli(this.millis);
        }

        void advance(long ms) {

            this.millis += ms;
        }

    }

    /** An identity provider that answers from a script and counts the requests it was sent. */
    private static final class FakeIdentityProvider {

        private final AtomicInteger calls = new AtomicInteger();

        private final List<String> bodies = new ArrayList<>();

        private final OkHttpClient client;

        FakeIdentityProvider(IntFunction<Response.Builder> respond) {

            this.client = new OkHttpClient.Builder()
                              .addInterceptor(chain -> {
                                  int n = this.calls.incrementAndGet();
                                  Buffer buffer = new Buffer();
                                  chain.request().body().writeTo(buffer);
                                  synchronized (this.bodies) {
                                      this.bodies.add(buffer.readUtf8());
                                  }
                                  return respond.apply(n).request(chain.request()).protocol(Protocol.HTTP_1_1).build();
                              })
                              .build();
        }

        static Response.Builder token(String token, long expiresIn) {

            return new Response.Builder()
                       .code(200)
                       .message("OK")
                       .body(ResponseBody.create(
                           "{\"access_token\":\"" + token + "\",\"expires_in\":" + expiresIn + "}", JSON));
        }

    }

    private static ClientCredentialsTokenProvider provider(FakeIdentityProvider idp, Clock clock) {

        return new ClientCredentialsTokenProvider("https://idp.example/token", "pivotal", SECRET, idp.client, MAPPER,
                                                  clock);
    }

    @Test
    public void sendsTheClientCredentialsGrant() throws IOException {

        FakeIdentityProvider idp = new FakeIdentityProvider(n -> FakeIdentityProvider.token("tok-" + n, 300));

        assertEquals("tok-1", provider(idp, new ManualClock()).accessToken());

        String body = idp.bodies.get(0);
        assertTrue(body.contains("grant_type=client_credentials"));
        assertTrue(body.contains("client_id=pivotal"));
        assertTrue(body.contains("client_secret=" + SECRET));
    }

    @Test
    public void reusesTheTokenUntilItNearsExpiry() throws IOException {

        FakeIdentityProvider idp = new FakeIdentityProvider(n -> FakeIdentityProvider.token("tok-" + n, 300));
        ManualClock clock = new ManualClock();
        ClientCredentialsTokenProvider tokens = provider(idp, clock);

        tokens.accessToken();
        clock.advance(260_000L);
        assertEquals("tok-1", tokens.accessToken());

        clock.advance(20_000L);
        assertEquals("tok-2", tokens.accessToken());
        assertEquals(2, idp.calls.get());
    }

    @Test
    public void reusesATokenShorterLivedThanTheRefreshMargin() throws IOException {

        FakeIdentityProvider idp = new FakeIdentityProvider(n -> FakeIdentityProvider.token("tok-" + n, 10));
        ClientCredentialsTokenProvider tokens = provider(idp, new ManualClock());

        tokens.accessToken();
        tokens.accessToken();
        assertEquals(1, idp.calls.get());
    }

    @Test
    public void concurrentRequestsShareOneRefresh() throws Exception {

        CountDownLatch release = new CountDownLatch(1);
        FakeIdentityProvider idp = new FakeIdentityProvider(n -> {
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return FakeIdentityProvider.token("tok-" + n, 300);
        });
        ClientCredentialsTokenProvider tokens = provider(idp, new ManualClock());

        ExecutorService pool = Executors.newFixedThreadPool(20);
        try {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                results.add(pool.submit(tokens::accessToken));
            }
            release.countDown();

            for (Future<String> result : results) {
                assertEquals("tok-1", result.get(5, TimeUnit.SECONDS));
            }
            assertEquals(1, idp.calls.get());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void failsWithoutExposingTheSecret() {

        FakeIdentityProvider idp = new FakeIdentityProvider(n -> new Response.Builder()
                                                                     .code(401)
                                                                     .message("Unauthorized")
                                                                     .body(ResponseBody.create(
                                                                         "client_secret=" + SECRET, JSON)));

        try {
            provider(idp, new ManualClock()).accessToken();
            fail("expected the token request to fail");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("HTTP 401"));
            assertFalse(e.getMessage().contains(SECRET));
        }
    }

    @Test
    public void addsTheBearerToken() throws IOException {

        FakeIdentityProvider idp = new FakeIdentityProvider(n -> FakeIdentityProvider.token("tok-" + n, 300));
        List<String> seen = new ArrayList<>();
        OkHttpClient hub = hub(new FspiopAccessTokenInterceptor(provider(idp, new ManualClock())), seen, 200);

        try (Response response = hub.newCall(new Request.Builder().url("https://hub.example/quotes/1").build())
                                    .execute()) {
            assertEquals(200, response.code());
        }

        assertEquals(List.of("Bearer tok-1"), seen);
    }

    @Test
    public void retriesOnceWithAFreshTokenWhenRefused() throws IOException {

        FakeIdentityProvider idp = new FakeIdentityProvider(n -> FakeIdentityProvider.token("tok-" + n, 300));
        List<String> seen = new ArrayList<>();
        OkHttpClient hub = hub(new FspiopAccessTokenInterceptor(provider(idp, new ManualClock())), seen, 401);

        try (Response response = hub.newCall(new Request.Builder().url("https://hub.example/quotes/1").build())
                                    .execute()) {
            assertEquals(401, response.code());
        }

        assertEquals(List.of("Bearer tok-1", "Bearer tok-2"), seen);
    }

    /** A Hub that records the Authorization header it receives and answers with a fixed status. */
    private static OkHttpClient hub(FspiopAccessTokenInterceptor interceptor, List<String> seen, int status) {

        return new OkHttpClient.Builder()
                   .addInterceptor(interceptor)
                   .addInterceptor(chain -> {
                       seen.add(chain.request().header("Authorization"));
                       return new Response.Builder()
                                  .request(chain.request())
                                  .protocol(Protocol.HTTP_1_1)
                                  .code(status)
                                  .message("")
                                  .body(ResponseBody.create("{}", JSON))
                                  .build();
                   })
                   .build();
    }

}
