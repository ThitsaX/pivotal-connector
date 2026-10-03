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

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Adds the Hub's bearer token to every outbound FSPIOP request.
 * <p>
 * Independent of signing and of mutual TLS: the token says which client is calling, the signature
 * says which participant sent the message, and the certificate secures the connection. The signature
 * does not cover this header, so where this sits relative to the signing interceptor does not matter.
 * <p>
 * A {@code 401} is retried once with a fresh token. A token can stop being accepted before its
 * stated expiry — revoked, or the identity provider's keys rotated — and without the retry every
 * callback would fail until the cached one aged out. Once only, so a client that is genuinely refused
 * fails at the cost of one extra request rather than looping.
 */
public final class FspiopAccessTokenInterceptor implements Interceptor {

    private static final String AUTHORIZATION = "Authorization";

    private static final int UNAUTHORIZED = 401;

    private final ClientCredentialsTokenProvider tokens;

    public FspiopAccessTokenInterceptor(ClientCredentialsTokenProvider tokens) {

        this.tokens = tokens;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {

        Request request = chain.request();
        String token = this.tokens.accessToken();

        Response response = chain.proceed(withToken(request, token));

        if (response.code() != UNAUTHORIZED) {
            return response;
        }

        response.close();
        this.tokens.invalidate(token);

        return chain.proceed(withToken(request, this.tokens.accessToken()));
    }

    private static Request withToken(Request request, String token) {

        return request.newBuilder()
                      .header(AUTHORIZATION, "Bearer " + token)
                      .build();
    }

}
