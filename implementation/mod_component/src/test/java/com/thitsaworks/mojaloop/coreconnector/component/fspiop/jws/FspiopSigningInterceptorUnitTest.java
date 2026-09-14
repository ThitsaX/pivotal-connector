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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws.key.StaticJwsKeyProvider;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FspiopSigningInterceptorUnitTest {

    private static final MediaType JSON = MediaType.get("application/json");

    private static final String BODY = "{\"quoteId\":\"q-1\",\"transactionId\":\"t-1\"}";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Captures the request the interceptor produced, without performing a call. */
    private static final class CapturingChain implements Interceptor.Chain {

        private final Request request;

        private Request captured;

        private CapturingChain(Request request) {

            this.request = request;
        }

        @NotNull
        @Override
        public Request request() {

            return this.request;
        }

        @NotNull
        @Override
        public Response proceed(@NotNull Request request) throws IOException {

            this.captured = request;

            return new Response.Builder().request(request)
                                         .protocol(Protocol.HTTP_1_1)
                                         .code(200)
                                         .message("OK")
                                         .build();
        }

        @Override
        public Connection connection() {

            return null;
        }

        @NotNull
        @Override
        public Call call() {

            return new OkHttpClient().newCall(this.request);
        }

        @Override
        public int connectTimeoutMillis() {

            return 0;
        }

        @NotNull
        @Override
        public Interceptor.Chain withConnectTimeout(int timeout, @NotNull java.util.concurrent.TimeUnit unit) {

            return this;
        }

        @Override
        public int readTimeoutMillis() {

            return 0;
        }

        @NotNull
        @Override
        public Interceptor.Chain withReadTimeout(int timeout, @NotNull java.util.concurrent.TimeUnit unit) {

            return this;
        }

        @Override
        public int writeTimeoutMillis() {

            return 0;
        }

        @NotNull
        @Override
        public Interceptor.Chain withWriteTimeout(int timeout, @NotNull java.util.concurrent.TimeUnit unit) {

            return this;
        }
    }

    private static KeyPair generateRsa2048() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    private static Request putQuotes() {

        return new Request.Builder().url("http://moja-quoting-service.mojaloop/quotes/q-1")
                                    .put(RequestBody.create(BODY, JSON))
                                    .header("fspiop-source", "payeefsp")
                                    .header("fspiop-destination", "payerfsp")
                                    .header("date", "Tue, 23 Aug 2026 09:15:00 GMT")
                                    .build();
    }

    private static Request capture(Interceptor interceptor, Request request) throws IOException {

        CapturingChain chain = new CapturingChain(request);
        interceptor.intercept(chain);

        return chain.captured;
    }

    @Test
    public void shouldSignAndSetTheCrossCheckedHeaders() throws Exception {

        KeyPair keyPair = generateRsa2048();
        Interceptor interceptor = new FspiopSigningInterceptor(
            new StaticJwsKeyProvider("payeefsp", keyPair.getPrivate()), true);

        Request signed = capture(interceptor, putQuotes());

        assertEquals("/quotes/q-1", signed.header("fspiop-uri"));
        assertEquals("PUT", signed.header("fspiop-http-method"));
        assertNotNull(signed.header("fspiop-signature"));

        JsonNode header = MAPPER.readTree(signed.header("fspiop-signature"));
        FspiopSignature.Header parsed = new FspiopSignature.Header(
            header.path("signature")
                  .asText(),
            header.path("protectedHeader")
                  .asText());

        assertTrue(FspiopSignature.verify(keyPair.getPublic(), parsed, BODY.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void shouldSignTheExactBytesThatWillBeTransmitted() throws Exception {

        KeyPair keyPair = generateRsa2048();
        Interceptor interceptor = new FspiopSigningInterceptor(
            new StaticJwsKeyProvider("payeefsp", keyPair.getPrivate()), true);

        Request signed = capture(interceptor, putQuotes());

        // Buffering the body to sign it must leave the outgoing body intact and unchanged.
        Buffer buffer = new Buffer();
        signed.body()
              .writeTo(buffer);

        assertEquals(BODY, buffer.readUtf8());
    }

    @Test
    public void shouldStripABasePathPrefixFromTheSignedUri() throws Exception {

        KeyPair keyPair = generateRsa2048();
        Interceptor interceptor = new FspiopSigningInterceptor(
            new StaticJwsKeyProvider("payeefsp", keyPair.getPrivate()), true);

        Request request = new Request.Builder().url("https://gateway.example.com/fspiop/v2/transfers/t-1/error")
                                               .put(RequestBody.create(BODY, JSON))
                                               .header("fspiop-source", "payeefsp")
                                               .build();

        assertEquals("/transfers/t-1/error", capture(interceptor, request).header("fspiop-uri"));
    }

    @Test
    public void shouldPassThroughUnsignedWhenDisabled() throws Exception {

        KeyPair keyPair = generateRsa2048();
        Interceptor interceptor = new FspiopSigningInterceptor(
            new StaticJwsKeyProvider("payeefsp", keyPair.getPrivate()), false);

        assertNull(capture(interceptor, putQuotes()).header("fspiop-signature"));
    }

    @Test
    public void shouldPassThroughUnsignedWhenNoKeyIsConfigured() throws Exception {

        Interceptor interceptor = new FspiopSigningInterceptor(
            new StaticJwsKeyProvider("payeefsp", null), true);

        assertNull(capture(interceptor, putQuotes()).header("fspiop-signature"));
    }

    @Test
    public void shouldPassThroughUnsignedWhenThereIsNoFspiopSource() throws Exception {

        KeyPair keyPair = generateRsa2048();
        Interceptor interceptor = new FspiopSigningInterceptor(
            new StaticJwsKeyProvider("payeefsp", keyPair.getPrivate()), true);

        Request request = new Request.Builder().url("http://hub/quotes/q-1")
                                               .put(RequestBody.create(BODY, JSON))
                                               .build();

        assertNull(capture(interceptor, request).header("fspiop-signature"));
    }

    @Test
    public void shouldNotSignABodylessRequest() throws Exception {

        KeyPair keyPair = generateRsa2048();
        Interceptor interceptor = new FspiopSigningInterceptor(
            new StaticJwsKeyProvider("payeefsp", keyPair.getPrivate()), true);

        Request request = new Request.Builder().url("http://hub/parties/MSISDN/959123456")
                                               .get()
                                               .header("fspiop-source", "payeefsp")
                                               .build();

        assertNull(capture(interceptor, request).header("fspiop-signature"));
    }

}
