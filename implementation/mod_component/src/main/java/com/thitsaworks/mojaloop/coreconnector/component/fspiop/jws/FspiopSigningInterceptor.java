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

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;

/**
 * Signs outbound FSPIOP requests, producing the {@code fspiop-signature} header.
 * <p>
 * An OkHttp {@link Interceptor} rather than a configured client, deliberately: every deployable
 * connector overrides the framework's {@code sharedOkHttpClient} bean with its own, so anything
 * attached to that bean is dead in production. An interceptor installed by the caller survives the
 * override, and no deployable repo has to change.
 * <p>
 * Also sets {@code fspiop-uri} and {@code fspiop-http-method}. These are not decoration: the
 * receiving validator cross-checks them against the protected header and rejects at the presence
 * check, before the signature is examined. Emitting the signature without them fails at every peer.
 * <p>
 * {@code FSPIOP-URI} is derived from the outgoing URL, so callers need not separate path from base
 * URL — {@link FspiopUri} strips scheme, host and any base-path prefix. This matters here because
 * each callback target comes from a different setting pointing at a different Hub service.
 */
public class FspiopSigningInterceptor implements Interceptor {

    private static final Logger LOG = LoggerFactory.getLogger(FspiopSigningInterceptor.class);

    private static final String FSPIOP_SOURCE = "fspiop-source";

    private static final String FSPIOP_DESTINATION = "fspiop-destination";

    private static final String FSPIOP_SIGNATURE = "fspiop-signature";

    private static final String FSPIOP_URI = "fspiop-uri";

    private static final String FSPIOP_HTTP_METHOD = "fspiop-http-method";

    private static final String DATE = "date";

    private final JwsKeyProvider keyProvider;

    private final boolean enabled;

    public FspiopSigningInterceptor(JwsKeyProvider keyProvider, boolean enabled) {

        this.keyProvider = keyProvider;
        this.enabled = enabled;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {

        Request request = chain.request();

        if (!this.enabled) {
            return chain.proceed(request);
        }

        PrivateKey privateKey = this.keyProvider.signingKey();

        // No key means this tenant is not switched on yet. Pass through unsigned rather than
        // failing: peers ignore signatures until they enable verification, so rollout is a data
        // change rather than a redeploy.
        if (privateKey == null) {
            return chain.proceed(request);
        }

        String source = request.header(FSPIOP_SOURCE);

        if (source == null || source.isBlank()) {
            return chain.proceed(request);
        }

        byte[] payload = readBody(request);

        // A detached JWS signs the body, so a bodyless request has nothing to sign. The reference
        // implementation does not sign these either — its _get carries no signing call and its
        // signer throws 'Cannot sign with no body'. Substituting a synthetic payload would produce
        // a signature no peer can reconstruct, which is worse than none because it looks like
        // protection.
        if (payload == null || payload.length == 0) {
            return chain.proceed(request);
        }

        String uri = FspiopUri.extract(request.url()
                                              .toString());
        String method = request.method()
                               .toUpperCase(java.util.Locale.ROOT);

        FspiopSignature.Header signature = FspiopSignature.sign(
            privateKey,
            new FspiopProtectedHeader.Input(method,
                                            uri,
                                            source,
                                            request.header(FSPIOP_DESTINATION),
                                            request.header(DATE)),
            payload);

        LOG.debug("Signed {} {} as '{}'", method, uri, source);

        return chain.proceed(request.newBuilder()
                                    .header(FSPIOP_URI, uri)
                                    .header(FSPIOP_HTTP_METHOD, method)
                                    .header(FSPIOP_SIGNATURE, signature.toHeaderValue())
                                    .build());
    }

    /**
     * Buffers the request body so the exact transmitted bytes can be signed.
     * <p>
     * FSPIOP callback bodies are small JSON documents, so buffering costs nothing. Reading through a
     * {@link Buffer} leaves the original body untouched — OkHttp writes it again when the call
     * proceeds — which is what keeps the signed bytes and the wire bytes identical.
     */
    private static byte[] readBody(Request request) throws IOException {

        RequestBody body = request.body();

        if (body == null) {
            return null;
        }

        try (Buffer buffer = new Buffer()) {
            body.writeTo(buffer);
            return buffer.readByteArray();
        }
    }

}
