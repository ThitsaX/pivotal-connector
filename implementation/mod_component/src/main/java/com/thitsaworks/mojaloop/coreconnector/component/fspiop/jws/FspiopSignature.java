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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FSPIOP detached JWS — the {@code fspiop-signature} header.
 * <p>
 * "Detached" means the payload is not carried in the signature header: the HTTP body <em>is</em> the
 * payload. The header transports only {@code {signature, protectedHeader}}, and the verifier
 * reconstructs the signing input from the body it received.
 * <p>
 * Signing input, per RFC 7515:
 *
 * <pre>
 *   base64url(protectedHeaderJson) + "." + base64url(payloadBytes)
 * </pre>
 *
 * <h2>Why the payload is signed verbatim</h2>
 * <p>
 * The TypeScript implementation re-stringifies the payload before signing, because it is handed a
 * parsed object and axios may serialize it again on the way out. Here the caller already holds the
 * exact bytes it is about to transmit, so those bytes are signed as-is.
 * <p>
 * Signing what is actually sent is the safer of the two: any re-serialization step risks the signed
 * bytes and the wire bytes diverging, which fails at the peer with no local symptom. For FSPIOP
 * payloads the two approaches agree — every field is a string, and both emit compact JSON — and the
 * shared conformance vectors pin that agreement. They would diverge on a JSON number such as
 * {@code 1.0}, which Jackson preserves and {@code JSON.stringify} renders as {@code 1}; FSPIOP
 * amounts are strings, so this does not arise in practice, but do not introduce a numeric field
 * without revisiting it.
 */
public final class FspiopSignature {

    private static final String SIGNING_ALGORITHM = "RS256";

    private static final String JCA_ALGORITHM = "SHA256withRSA";

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder()
                                                        .withoutPadding();

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    /** See the note on {@link FspiopProtectedHeader}: not the shared application mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FspiopSignature() {

    }

    /**
     * @param privateKey the signing tenant's key
     * @param input      request metadata bound into the protected header
     * @param payload    the exact request body bytes that will be transmitted
     */
    public static Header sign(PrivateKey privateKey, FspiopProtectedHeader.Input input, byte[] payload) {

        assertSupported(input.algorithm());

        String protectedHeader = encode(FspiopProtectedHeader.serialize(input)
                                                             .getBytes(StandardCharsets.UTF_8));
        String encodedPayload = encode(payload);

        try {
            Signature signature = Signature.getInstance(JCA_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(signingInput(protectedHeader, encodedPayload));

            return new Header(ENCODER.encodeToString(signature.sign()), protectedHeader);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to produce the FSPIOP JWS signature.", e);
        }
    }

    /**
     * Verifies a received {@code fspiop-signature} against the received body.
     * <p>
     * The protected header is verified <strong>as received</strong>, never rebuilt — rebuilding it
     * would make the check tautological. Cross-checking its claims against the actual request is a
     * separate concern and belongs to the caller.
     */
    public static boolean verify(PublicKey publicKey, Header header, byte[] payload) {

        try {
            assertSupported(decodeProtectedHeader(header.protectedHeader())
                                .get("alg"));

            Signature signature = Signature.getInstance(JCA_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(signingInput(header.protectedHeader(), encode(payload)));

            return signature.verify(DECODER.decode(header.signature()));
        } catch (GeneralSecurityException | RuntimeException e) {
            return false;
        }
    }

    /** Decodes the received protected header so a caller can cross-check its claims. */
    public static Map<String, String> decodeProtectedHeader(String protectedHeader) {

        try {
            String json = new String(DECODER.decode(protectedHeader), StandardCharsets.UTF_8);

            @SuppressWarnings("unchecked")
            Map<String, String> fields = MAPPER.readValue(json, LinkedHashMap.class);

            return fields;
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new IllegalArgumentException("FSPIOP protected header must decode to a JSON object.", e);
        }
    }

    private static byte[] signingInput(String protectedHeader, String encodedPayload) {

        return (protectedHeader + "." + encodedPayload).getBytes(StandardCharsets.US_ASCII);
    }

    private static String encode(byte[] value) {

        return ENCODER.encodeToString(value);
    }

    private static void assertSupported(String algorithm) {

        if (algorithm != null && !SIGNING_ALGORITHM.equals(algorithm)) {
            throw new IllegalArgumentException(
                "Unsupported FSPIOP signing algorithm '" + algorithm + "'. Supported: " + SIGNING_ALGORITHM + ".");
        }
    }

    /**
     * The JSON value carried in the {@code fspiop-signature} HTTP header.
     *
     * @param signature       base64url detached signature
     * @param protectedHeader base64url of the serialized protected header
     */
    public record Header(String signature, String protectedHeader) {

        public String toHeaderValue() {

            try {
                Map<String, String> value = new LinkedHashMap<>();
                value.put("signature", signature);
                value.put("protectedHeader", protectedHeader);

                return MAPPER.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to serialize the fspiop-signature header.", e);
            }
        }
    }

}
