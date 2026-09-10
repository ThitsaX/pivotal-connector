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
package com.thitsaworks.mojaloop.coreconnector.component.util;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Minimal PEM decoding for RSA keys, using only the JDK.
 * <p>
 * Supports the two encodings Pivotal produces: <strong>PKCS#8</strong> for private keys
 * ({@code -----BEGIN PRIVATE KEY-----}) and <strong>X.509 SubjectPublicKeyInfo</strong> for public
 * keys ({@code -----BEGIN PUBLIC KEY-----}).
 * <p>
 * <strong>PKCS#1 is not supported</strong> ({@code -----BEGIN RSA PRIVATE KEY-----}). The JDK has no
 * parser for it and adding BouncyCastle to an artifact four connectors inherit is not worth it.
 * Convert instead:
 * <pre>
 *   openssl pkcs8 -topk8 -nocrypt -in pkcs1.pem -out pkcs8.pem
 * </pre>
 * The error message says so, because this fails at deploy time on a key an operator supplied.
 */
public final class Pem {

    private static final String PKCS1_PRIVATE_HEADER = "-----BEGIN RSA PRIVATE KEY-----";

    private static final String ALGORITHM = "RSA";

    private Pem() {

    }

    public static PrivateKey readPrivateKey(String pem) {

        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("Cannot read a private key: the PEM is empty.");
        }

        if (pem.contains(PKCS1_PRIVATE_HEADER)) {
            throw new IllegalArgumentException(
                "PKCS#1 private keys are not supported. Convert to PKCS#8 with: "
                    + "openssl pkcs8 -topk8 -nocrypt -in pkcs1.pem -out pkcs8.pem");
        }

        try {
            return KeyFactory.getInstance(ALGORITHM)
                             .generatePrivate(new PKCS8EncodedKeySpec(decode(pem)));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Not a valid PKCS#8 RSA private key.", e);
        }
    }

    public static PublicKey readPublicKey(String pem) {

        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("Cannot read a public key: the PEM is empty.");
        }

        try {
            return KeyFactory.getInstance(ALGORITHM)
                             .generatePublic(new X509EncodedKeySpec(decode(pem)));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Not a valid X.509 RSA public key.", e);
        }
    }

    /**
     * Strips the armour and decodes. Literal {@code \n} sequences are normalised first: key material
     * routinely arrives having been through a JSON or YAML round trip that escaped the newlines.
     */
    private static byte[] decode(String pem) {

        String body = pem.replace("\\n", "\n")
                         .replaceAll("-----BEGIN [A-Z ]+-----", "")
                         .replaceAll("-----END [A-Z ]+-----", "")
                         .replaceAll("\\s", "");

        try {
            return Base64.getDecoder()
                         .decode(body);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("PEM body is not valid base64.", e);
        }
    }

}
