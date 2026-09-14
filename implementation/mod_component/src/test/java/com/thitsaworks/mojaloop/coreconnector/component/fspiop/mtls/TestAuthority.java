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

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A throwaway certificate authority for tests.
 * <p>
 * Real material is used rather than a stub because whether a certificate is presented, and whether
 * the peer accepts it, are facts about the handshake that a mock cannot show.
 */
final class TestAuthority {

    private static final AtomicLong SERIAL = new AtomicLong(System.currentTimeMillis());

    private final KeyPair key;

    private final X509Certificate certificate;

    TestAuthority() {

        try {
            this.key = generateKeyPair();
            this.certificate = certify("test-ca", "test-ca", this.key.getPublic(), this.key.getPrivate(), true);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create the test authority.", e);
        }
    }

    X509Certificate certificate() {

        return this.certificate;
    }

    String caPem() {

        return toPem("CERTIFICATE", encoded(this.certificate));
    }

    /** A leaf signed by this authority. */
    Issued issue(String commonName) {

        try {
            KeyPair pair = generateKeyPair();
            X509Certificate leaf =
                certify(commonName, "test-ca", pair.getPublic(), this.key.getPrivate(), false);

            return new Issued(leaf, pair.getPrivate());
        } catch (Exception e) {
            throw new IllegalStateException("Could not issue a test certificate.", e);
        }
    }

    private static KeyPair generateKeyPair() throws Exception {

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        return generator.generateKeyPair();
    }

    private static X509Certificate certify(String commonName,
                                           String issuerCommonName,
                                           java.security.PublicKey publicKey,
                                           PrivateKey signingKey,
                                           boolean isAuthority) throws Exception {

        long now = System.currentTimeMillis();

        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
            new X500Name("CN=" + issuerCommonName),
            BigInteger.valueOf(SERIAL.incrementAndGet()),
            new Date(now - 60_000L),
            new Date(now + 3_600_000L),
            new X500Name("CN=" + commonName),
            publicKey);

        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isAuthority));
        builder.addExtension(Extension.subjectAlternativeName,
                             false,
                             new GeneralNames(new GeneralName(GeneralName.dNSName, "localhost")));

        X509CertificateHolder holder =
            builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(signingKey));

        return new JcaX509CertificateConverter().getCertificate(holder);
    }

    private static byte[] encoded(X509Certificate certificate) {

        try {
            return certificate.getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Could not encode a test certificate.", e);
        }
    }

    private static String toPem(String label, byte[] der) {

        StringWriter out = new StringWriter();
        out.write("-----BEGIN " + label + "-----\n");

        String body = Base64.getEncoder().encodeToString(der);
        for (int index = 0; index < body.length(); index += 64) {
            out.write(body, index, Math.min(64, body.length() - index));
            out.write("\n");
        }

        out.write("-----END " + label + "-----\n");

        return out.toString();
    }

    /** A certificate and the private key that goes with it. */
    static final class Issued {

        private final X509Certificate certificate;

        private final PrivateKey privateKey;

        private Issued(X509Certificate certificate, PrivateKey privateKey) {

            this.certificate = certificate;
            this.privateKey = privateKey;
        }

        X509Certificate certificate() {

            return this.certificate;
        }

        PrivateKey privateKey() {

            return this.privateKey;
        }

        String certificatePem() {

            return toPem("CERTIFICATE", encoded(this.certificate));
        }

        /** PKCS#8, which is the only private key encoding the connector reads. */
        String privateKeyPem() {

            return toPem("PRIVATE KEY", this.privateKey.getEncoded());
        }
    }

}
