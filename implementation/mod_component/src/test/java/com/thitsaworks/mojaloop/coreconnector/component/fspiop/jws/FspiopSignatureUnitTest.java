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

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class FspiopSignatureUnitTest {

    private static final FspiopProtectedHeader.Input INPUT = new FspiopProtectedHeader.Input(
        "PUT",
        "https://hub.example.com/quotes/abc-123",
        "payeefsp",
        "payerfsp",
        "Tue, 23 Aug 2026 09:15:00 GMT");

    private static final byte[] PAYLOAD = "{\"amount\":\"100\",\"currency\":\"USD\"}".getBytes(StandardCharsets.UTF_8);

    private static KeyPair generateRsa2048() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    @Test
    public void shouldSignAndVerifyWithAMatchingKeyPair() throws Exception {

        KeyPair keyPair = generateRsa2048();
        FspiopSignature.Header header = FspiopSignature.sign(keyPair.getPrivate(), INPUT, PAYLOAD);

        assertFalse(header.signature()
                          .isEmpty());
        assertFalse(header.protectedHeader()
                          .isEmpty());
        assertTrue(FspiopSignature.verify(keyPair.getPublic(), header, PAYLOAD));
    }

    @Test
    public void shouldFailVerificationWithANonMatchingKey() throws Exception {

        KeyPair signer = generateRsa2048();
        KeyPair other = generateRsa2048();

        FspiopSignature.Header header = FspiopSignature.sign(signer.getPrivate(), INPUT, PAYLOAD);

        assertFalse(FspiopSignature.verify(other.getPublic(), header, PAYLOAD));
    }

    @Test
    public void shouldFailVerificationWhenTheBodyIsAltered() throws Exception {

        KeyPair keyPair = generateRsa2048();
        FspiopSignature.Header header = FspiopSignature.sign(keyPair.getPrivate(), INPUT, PAYLOAD);

        byte[] tampered = "{\"amount\":\"9999\",\"currency\":\"USD\"}".getBytes(StandardCharsets.UTF_8);

        assertFalse(FspiopSignature.verify(keyPair.getPublic(), header, tampered));
    }

    @Test
    public void shouldFailVerificationWhenTheProtectedHeaderIsAltered() throws Exception {

        KeyPair keyPair = generateRsa2048();
        FspiopSignature.Header header = FspiopSignature.sign(keyPair.getPrivate(), INPUT, PAYLOAD);

        String tamperedJson = "{\"alg\":\"RS256\",\"FSPIOP-URI\":\"/quotes/abc-123\","
            + "\"FSPIOP-HTTP-Method\":\"PUT\",\"FSPIOP-Source\":\"payeefsp\","
            + "\"FSPIOP-Destination\":\"attackerfsp\"}";

        FspiopSignature.Header tampered = new FspiopSignature.Header(
            header.signature(),
            Base64.getUrlEncoder()
                  .withoutPadding()
                  .encodeToString(tamperedJson.getBytes(StandardCharsets.UTF_8)));

        assertFalse(FspiopSignature.verify(keyPair.getPublic(), tampered, PAYLOAD));
    }

    @Test
    public void shouldReturnFalseRatherThanThrowOnAMalformedHeader() throws Exception {

        KeyPair keyPair = generateRsa2048();
        FspiopSignature.Header malformed = new FspiopSignature.Header("nope", "not-base64-json");

        assertFalse(FspiopSignature.verify(keyPair.getPublic(), malformed, PAYLOAD));
    }

    @Test
    public void shouldExposeTheDecodedProtectedHeaderForCrossChecking() throws Exception {

        KeyPair keyPair = generateRsa2048();
        FspiopSignature.Header header = FspiopSignature.sign(keyPair.getPrivate(), INPUT, PAYLOAD);

        Map<String, String> fields = FspiopSignature.decodeProtectedHeader(header.protectedHeader());

        assertEquals("RS256", fields.get("alg"));
        assertEquals("/quotes/abc-123", fields.get("FSPIOP-URI"));
        assertEquals("PUT", fields.get("FSPIOP-HTTP-Method"));
        assertEquals("payeefsp", fields.get("FSPIOP-Source"));
        assertEquals("payerfsp", fields.get("FSPIOP-Destination"));
    }

    @Test
    public void shouldRejectAnUnsupportedAlgorithm() throws Exception {

        KeyPair keyPair = generateRsa2048();

        FspiopProtectedHeader.Input none = new FspiopProtectedHeader.Input(
            "PUT", "https://hub.example.com/quotes/abc-123", "payeefsp", "payerfsp", null, "none");

        assertThrows(IllegalArgumentException.class,
                     () -> FspiopSignature.sign(keyPair.getPrivate(), none, PAYLOAD));
    }

    @Test
    public void shouldEmitTheHeaderValueAsCompactJson() throws Exception {

        KeyPair keyPair = generateRsa2048();
        FspiopSignature.Header header = FspiopSignature.sign(keyPair.getPrivate(), INPUT, PAYLOAD);

        String value = header.toHeaderValue();

        assertTrue(value.startsWith("{\"signature\":\""));
        assertTrue(value.contains("\"protectedHeader\":\""));
    }

    @Test
    public void shouldCarryNoFieldsBeyondTheContract() {

        Map<String, String> fields = FspiopProtectedHeader.build(INPUT);

        assertEquals("[alg, FSPIOP-URI, FSPIOP-HTTP-Method, FSPIOP-Source, FSPIOP-Destination, Date]",
                     fields.keySet()
                           .toString());
    }

    @Test
    public void shouldOmitConditionalFieldsWhenBlank() {

        FspiopProtectedHeader.Input sparse = new FspiopProtectedHeader.Input(
            "PUT", "/quotes/q-1", "payeefsp", "  ", "");

        Map<String, String> fields = FspiopProtectedHeader.build(sparse);

        assertFalse(fields.containsKey("FSPIOP-Destination"));
        assertFalse(fields.containsKey("Date"));
        assertFalse(fields.containsKey("typ"));
        assertFalse(fields.containsKey("cty"));
    }

    @Test
    public void shouldRejectAMissingSourceOrMethod() {

        assertThrows(IllegalArgumentException.class,
                     () -> FspiopProtectedHeader.build(
                         new FspiopProtectedHeader.Input("PUT", "/quotes/q-1", "", null, null)));

        assertThrows(IllegalArgumentException.class,
                     () -> FspiopProtectedHeader.build(
                         new FspiopProtectedHeader.Input("", "/quotes/q-1", "payeefsp", null, null)));
    }

}
