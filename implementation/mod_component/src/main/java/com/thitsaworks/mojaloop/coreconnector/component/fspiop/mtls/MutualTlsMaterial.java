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

package com.thitsaworks.mojaloop.coreconnector.component.fspiop.mtls;

import com.thitsaworks.mojaloop.coreconnector.component.util.Pem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;

/**
 * The certificate, key and trust anchor a connector uses to reach the Hub, read from mounted files.
 * <p>
 * Paths rather than inline values, because the material is renewed underneath a running process:
 * the certificate lives in a Secret that is rewritten every few weeks, and a value baked into the
 * Deployment could never reflect that.
 * <p>
 * Reuses {@link Pem} for the private key, so the connector has one answer to what a key PEM may
 * look like rather than two that can disagree. Certificates need no such helper — the JDK reads
 * X.509 natively, including a file holding a leaf followed by its issuers.
 */
public final class MutualTlsMaterial {

    private final List<X509Certificate> clientChain;

    private final PrivateKey clientKey;

    private final List<X509Certificate> trustAnchors;

    private final String fingerprint;

    private MutualTlsMaterial(List<X509Certificate> clientChain,
                              PrivateKey clientKey,
                              List<X509Certificate> trustAnchors,
                              String fingerprint) {

        this.clientChain = clientChain;
        this.clientKey = clientKey;
        this.trustAnchors = trustAnchors;
        this.fingerprint = fingerprint;
    }

    /**
     * Reads every configured file.
     *
     * @throws IllegalStateException when a path is set but unreadable, or the contents are not
     *                               usable material. A connector that cannot present the
     *                               certificate an operator configured must say so plainly rather
     *                               than fall back to an unauthenticated connection.
     */
    public static MutualTlsMaterial read(Settings settings) {

        String caPem = readFile(settings.caPath(), "certificate authority");
        String certPem = readFile(settings.clientCertPath(), "client certificate");
        String keyPem = readFile(settings.clientKeyPath(), "client private key");

        List<X509Certificate> chain = certPem == null ? List.of() : parseCertificates(certPem);
        List<X509Certificate> anchors = caPem == null ? List.of() : parseCertificates(caPem);
        PrivateKey key = keyPem == null ? null : Pem.readPrivateKey(keyPem);

        if (chain.isEmpty() != (key == null)) {
            throw new IllegalStateException(
                "A client certificate and its private key must be configured together; " +
                    "half a pair cannot complete a handshake.");
        }

        return new MutualTlsMaterial(chain, key, anchors, fingerprint(caPem, certPem, keyPem));
    }

    public List<X509Certificate> clientChain() {

        return this.clientChain;
    }

    public PrivateKey clientKey() {

        return this.clientKey;
    }

    public List<X509Certificate> trustAnchors() {

        return this.trustAnchors;
    }

    public boolean hasClientCertificate() {

        return this.clientKey != null && !this.clientChain.isEmpty();
    }

    public boolean hasTrustAnchors() {

        return !this.trustAnchors.isEmpty();
    }

    /**
     * Identifies the material by content.
     * <p>
     * Deliberately not the file's timestamp: a Secret update rewrites the file whether or not the
     * bytes changed, and rebuilding on that would discard pooled connections for nothing.
     */
    public String fingerprint() {

        return this.fingerprint;
    }

    private static String readFile(String path, String what) {

        if (path == null || path.isBlank()) {
            return null;
        }

        try {
            return Files.readString(Path.of(path.trim()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Could not read the " + what + " at '" + path + "'.", e);
        }
    }

    private static List<X509Certificate> parseCertificates(String pem) {

        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> parsed = factory.generateCertificates(
                new ByteArrayInputStream(
                    pem.replace("\\n", "\n").getBytes(StandardCharsets.UTF_8)));

            List<X509Certificate> certificates = new ArrayList<>(parsed.size());
            for (Certificate certificate : parsed) {
                certificates.add((X509Certificate) certificate);
            }

            if (certificates.isEmpty()) {
                throw new IllegalStateException("No certificate was found in the configured PEM.");
            }

            return List.copyOf(certificates);
        } catch (CertificateException e) {
            throw new IllegalStateException(
                "The configured PEM does not hold valid X.509 certificates.", e);
        }
    }

    private static String fingerprint(String caPem, String certPem, String keyPem) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(String.valueOf(caPem).getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(String.valueOf(certPem).getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(String.valueOf(keyPem).getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable in this JVM.", e);
        }
    }

    /** Where the material is read from. Any of the three may be absent. */
    public record Settings(String caPath, String clientCertPath, String clientKeyPath) {

        public boolean isConfigured() {

            return isSet(this.caPath) || isSet(this.clientCertPath) || isSet(this.clientKeyPath);
        }

        private static boolean isSet(String value) {

            return value != null && !value.isBlank();
        }

    }

}
