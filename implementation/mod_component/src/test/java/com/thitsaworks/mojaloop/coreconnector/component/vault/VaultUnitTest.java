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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.support.VaultResponseSupport;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VaultUnitTest {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Rule
    public final TemporaryFolder workDir = new TemporaryFolder();

    @Test
    public void shouldLoginAndMapAKvV2Secret() throws Exception {

        List<Request> requests = new ArrayList<>();
        OkHttpClient http = client(request -> {
            requests.add(request);
            return response(request, 200, "{\"auth\":{\"client_token\":\"vault-token\"}}");
        });

        Vault vault = vault(http, token -> {
            assertEquals("vault-token", token);
            return operations(new TestSecret("private-pem"));
        });
        Optional<TestSecret> secret = vault.get("pivotal/jwskey/wallet2", TestSecret.class);

        assertTrue(secret.isPresent());
        assertEquals("private-pem", secret.get().privateKey());
        assertEquals(1, requests.size());
        assertEquals("/v1/auth/kubernetes/login", requests.get(0).url().encodedPath());
        var loginBody = new ObjectMapper().readTree(requestBody(requests.get(0)));
        assertEquals("connector-role", loginBody.path("role").asText());
        assertEquals("service-account-jwt", loginBody.path("jwt").asText());
    }

    @Test
    public void shouldReturnEmptyWhenTheSecretDoesNotExist() throws Exception {

        OkHttpClient http = client(
            request -> response(request, 200, "{\"auth\":{\"client_token\":\"vault-token\"}}"));

        Optional<TestSecret> secret = vault(http, token -> operations(null))
                                          .get("pivotal/jwskey/missing", TestSecret.class);

        assertFalse(secret.isPresent());
    }

    private Vault vault(OkHttpClient http,
                        Function<String, VaultKeyValueOperations> operations) throws Exception {

        Path token = this.workDir.newFile("service-account-token").toPath();
        Files.writeString(token, "service-account-jwt\n", StandardCharsets.UTF_8);

        return new Vault(http,
                         new ObjectMapper(),
                         "https://vault.test",
                         "connector-role",
                         "kubernetes",
                         "secret",
                         token.toString(),
                         operations);
    }

    private static OkHttpClient client(Function<Request, Response> responder) {

        return new OkHttpClient.Builder().addInterceptor(chain -> responder.apply(chain.request()))
                                         .build();
    }

    private static Response response(Request request, int status, String body) {

        return new Response.Builder().request(request)
                                     .protocol(Protocol.HTTP_1_1)
                                     .code(status)
                                     .message(status == 200 ? "OK" : "Not Found")
                                     .body(ResponseBody.create(body, JSON))
                                     .build();
    }

    private static String requestBody(Request request) throws Exception {

        try (Buffer buffer = new Buffer()) {
            request.body().writeTo(buffer);
            return buffer.readUtf8();
        }
    }

    private static VaultKeyValueOperations operations(TestSecret data) {

        return (VaultKeyValueOperations) Proxy.newProxyInstance(
            VaultKeyValueOperations.class.getClassLoader(),
            new Class<?>[]{VaultKeyValueOperations.class},
            (proxy, method, args) -> {
                if (method.getName().equals("get") && args.length == 2) {
                    if (data == null) {
                        return null;
                    }

                    VaultResponseSupport<TestSecret> response = new VaultResponseSupport<>();
                    response.setData(data);
                    return response;
                }

                throw new UnsupportedOperationException(method.getName());
            });
    }

    public record TestSecret(String privateKey) { }

}
