/*
 * Copyright (c) 2026 ThitsaWorks
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

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class JsonWebToken {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonWebToken.class);

    public static Token signWithPrivateKeyPem(String privateKeyPem,
                                              Map<String, Object> headers,
                                              String payload) throws NoSuchAlgorithmException, InvalidKeySpecException {

        String privateKeyContent = JsonWebToken.removeHeaders(privateKeyPem);

        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        String token = Jwts.builder().signWith(privateKey).header().add(headers).and().content(
                payload).compact();

        String[] parts = token.split("\\.");

        return new Token(parts[0], parts[1], parts[2], token);
    }

    public static boolean verifyUsingCertificate(String certificatePem, String token) throws CertificateException {

        // Replace the header and footer with an empty string
        String certificateContent = JsonWebToken.removeHeaders(certificatePem);

        byte[] certificateBytes = Base64.getDecoder().decode(certificateContent);

        // Load the certificate from string
        ByteArrayInputStream inputStream = new ByteArrayInputStream(certificateBytes);

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);

        PublicKey publicKey = certificate.getPublicKey();

        try {

            Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);

        } catch (JwtException e) {

            LOGGER.error("Error : {0}", e);
            return false;
        }

        return true;
    }

    public static boolean verifyUsingPublicKey(String publicKeyPem, String token)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        String publicKeyContent = JsonWebToken.removeHeaders(publicKeyPem);

        // Decode Base64 encoded public key
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);

        // Create a PublicKey object
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Or whichever algorithm you're using
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        try {

            Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);

        } catch (JwtException e) {

            LOGGER.error("Error : {0}", e);
            return false;
        }

        return true;
    }

    private static String removeHeaders(String data) {

        return data.replaceAll("-----BEGIN (.*?)-----", "")
                         .replaceAll("-----END (.*?)-----", "")
                         .replaceAll("\\s", "");
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Token{

        String header;

        String body;

        String signature;

        String full;
    }


}
