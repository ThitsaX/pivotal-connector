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

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Builds the FSPIOP JWS protected header.
 * <p>
 * The header binds request <em>metadata</em> into the signature, so a validly-signed body cannot be
 * replayed against a different endpoint, method or destination.
 * <p>
 * The contract is three mandatory fields, two conditional, and <strong>nothing else</strong> — no
 * {@code typ}, no {@code cty}, no HTTP headers beyond those named. Property names are
 * <strong>case-sensitive</strong> here even though HTTP header names are not, which is the single
 * most common way to get this wrong.
 *
 * <pre>
 *   { "alg", "FSPIOP-URI", "FSPIOP-HTTP-Method", "FSPIOP-Source"[, "FSPIOP-Destination"][, "Date"] }
 * </pre>
 * <p>
 * Field order is preserved deliberately: the signature covers the serialized header bytes, so a
 * reordered header is a different signing input. {@link LinkedHashMap} is what guarantees it.
 */
public final class FspiopProtectedHeader {

    public static final String DEFAULT_ALGORITHM = "RS256";

    /**
     * A private, minimally-configured mapper.
     * <p>
     * Deliberately <strong>not</strong> the shared application {@code ObjectMapper}: that one is
     * configured with {@code NON_NULL}/{@code NON_EMPTY} inclusion and
     * {@code WRITE_NUMBERS_AS_STRINGS}, and any future change to it would silently alter the bytes
     * being signed. Serialization here is part of the wire contract, not an application concern.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FspiopProtectedHeader() {

    }

    public static Map<String, String> build(Input input) {

        if (isBlank(input.source())) {
            throw new IllegalArgumentException(
                "Cannot build the FSPIOP protected header: fspiop-source is required.");
        }

        if (isBlank(input.method())) {
            throw new IllegalArgumentException(
                "Cannot build the FSPIOP protected header: the HTTP method is required.");
        }

        // Insertion order is the serialization order, and the serialization is what gets signed.
        Map<String, String> fields = new LinkedHashMap<>();

        fields.put("alg", input.algorithm() == null ? DEFAULT_ALGORITHM : input.algorithm());
        fields.put("FSPIOP-URI", FspiopUri.extract(input.uri()));
        fields.put("FSPIOP-HTTP-Method", input.method().toUpperCase(Locale.ROOT));
        fields.put("FSPIOP-Source", input.source());

        if (!isBlank(input.destination())) {
            fields.put("FSPIOP-Destination", input.destination());
        }

        if (!isBlank(input.date())) {
            fields.put("Date", input.date());
        }

        return fields;
    }

    public static String serialize(Input input) {

        try {
            return MAPPER.writeValueAsString(build(input));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize the FSPIOP protected header.", e);
        }
    }

    private static boolean isBlank(String value) {

        return value == null || value.isBlank();
    }

    /**
     * Request metadata bound into the protected header.
     *
     * @param method      HTTP method of the request being signed; upper-cased into the header
     * @param uri         request URL or path, reduced to the resource path by {@link FspiopUri}
     * @param source      value of the {@code fspiop-source} HTTP header
     * @param destination value of the {@code fspiop-destination} header, when present
     * @param date        value of the {@code date} header, when present
     * @param algorithm   signing algorithm; {@code null} means RS256 — settled decision 3
     */
    public record Input(String method,
                        String uri,
                        String source,
                        String destination,
                        String date,
                        String algorithm) {

        public Input(String method, String uri, String source, String destination, String date) {

            this(method, uri, source, destination, date, DEFAULT_ALGORITHM);
        }

    }

}
