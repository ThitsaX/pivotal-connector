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
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Executes the shared FSPIOP JWS conformance vectors.
 * <p>
 * {@code fspiop-jws-vectors.json} is byte-identical to the copy the Pivotal monorepo runs
 * (<code>tests/shared/fspiop/component/vectors/</code>). Two implementations of one wire contract
 * drift silently otherwise, and the drift only shows up as a rejected signature at a peer weeks
 * later — see trust-manager-docs, hub-facing-leg.md section A6.
 * <p>
 * Signatures are not pinned: RS256 over a fixed input is deterministic, but pinning one would pin a
 * key. Each implementation signs the shared inputs with its own key and verifies the round trip;
 * what the vectors pin is the <em>signing input</em>, which is where the two could diverge.
 */
public class FspiopJwsConformanceUnitTest {

    private static final String VECTORS = "/fspiop-jws-vectors.json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static JsonNode loadVectors() throws Exception {

        try (InputStream stream = FspiopJwsConformanceUnitTest.class.getResourceAsStream(VECTORS)) {

            if (stream == null) {
                throw new IllegalStateException("Conformance vectors not found on the test classpath: " + VECTORS);
            }

            return MAPPER.readTree(stream);
        }
    }

    private static String header(JsonNode vector, String name) {

        JsonNode value = vector.path("request")
                               .path("headers")
                               .path(name);

        return value.isMissingNode() ? null : value.asText();
    }

    private static FspiopProtectedHeader.Input inputFrom(JsonNode vector) {

        return new FspiopProtectedHeader.Input(
            vector.path("request")
                  .path("method")
                  .asText(),
            vector.path("request")
                  .path("url")
                  .asText(),
            header(vector, "fspiop-source"),
            header(vector, "fspiop-destination"),
            header(vector, "date"));
    }

    private static KeyPair generateRsa2048() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    @Test
    public void shouldExtractTheExpectedFspiopUriForEveryVector() throws Exception {

        for (JsonNode vector : loadVectors().path("vectors")) {

            assertEquals(vector.path("name")
                               .asText(),
                         vector.path("expected")
                               .path("fspiopUri")
                               .asText(),
                         FspiopUri.extract(vector.path("request")
                                                 .path("url")
                                                 .asText()));
        }
    }

    @Test
    public void shouldBuildTheExpectedProtectedHeaderFieldsForEveryVector() throws Exception {

        for (JsonNode vector : loadVectors().path("vectors")) {

            JsonNode expected = vector.path("expected")
                                      .path("protectedHeader");

            Map<String, String> expectedFields = new LinkedHashMap<>();
            Iterator<Map.Entry<String, JsonNode>> entries = expected.fields();

            while (entries.hasNext()) {
                Map.Entry<String, JsonNode> entry = entries.next();
                expectedFields.put(entry.getKey(), entry.getValue()
                                                        .asText());
            }

            assertEquals(vector.path("name")
                               .asText(),
                         expectedFields,
                         FspiopProtectedHeader.build(inputFrom(vector)));
        }
    }

    @Test
    public void shouldSerializeTheProtectedHeaderInTheSignedOrderForEveryVector() throws Exception {

        // Field equality is not enough: the serialization is what gets signed, so order matters.
        for (JsonNode vector : loadVectors().path("vectors")) {

            assertEquals(vector.path("name")
                               .asText(),
                         vector.path("expected")
                               .path("protectedHeaderJson")
                               .asText(),
                         FspiopProtectedHeader.serialize(inputFrom(vector)));
        }
    }

    @Test
    public void shouldProduceTheExpectedSigningInputForEveryVector() throws Exception {

        KeyPair keyPair = generateRsa2048();

        for (JsonNode vector : loadVectors().path("vectors")) {

            String name = vector.path("name")
                                .asText();
            byte[] payload = vector.path("expected")
                                   .path("payloadJson")
                                   .asText()
                                   .getBytes(StandardCharsets.UTF_8);

            FspiopSignature.Header signature = FspiopSignature.sign(keyPair.getPrivate(), inputFrom(vector), payload);

            assertEquals(name,
                         vector.path("expected")
                               .path("protectedHeaderB64")
                               .asText(),
                         signature.protectedHeader());

            String payloadB64 = Base64.getUrlEncoder()
                                      .withoutPadding()
                                      .encodeToString(payload);

            assertEquals(name,
                         vector.path("expected")
                               .path("payloadB64")
                               .asText(),
                         payloadB64);

            assertEquals(name,
                         vector.path("expected")
                               .path("signingInput")
                               .asText(),
                         signature.protectedHeader() + "." + payloadB64);

            assertTrue(name, FspiopSignature.verify(keyPair.getPublic(), signature, payload));
        }
    }

    @Test
    public void shouldThrowForEveryRejectVector() throws Exception {

        for (JsonNode reject : loadVectors().path("rejects")) {

            String url = reject.path("url")
                               .asText();

            assertThrows(reject.path("reason")
                               .asText(),
                         IllegalArgumentException.class,
                         () -> FspiopUri.extract(url));
        }
    }

}
