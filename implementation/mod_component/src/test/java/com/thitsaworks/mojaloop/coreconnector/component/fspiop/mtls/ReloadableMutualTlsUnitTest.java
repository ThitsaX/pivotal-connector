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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Exercises the handshake itself rather than a stub.
 * <p>
 * Whether a client certificate is actually presented, and whether swapping the material reaches
 * the next connection, are facts about the JSSE that only a real TLS server can show.
 */
public class ReloadableMutualTlsUnitTest {

    @Rule
    public final TemporaryFolder workDir = new TemporaryFolder();

    private TestAuthority authority;

    private ExecutorService serverThread;

    @Before
    public void setUp() {

        this.authority = new TestAuthority();
        this.serverThread = Executors.newSingleThreadExecutor();
    }

    @After
    public void tearDown() {

        this.serverThread.shutdownNow();
    }

    @Test
    public void shouldRefuseToStartWithoutAClientCertificate() throws Exception {

        Path caPath = write("ca.pem", this.authority.caPem());

        IllegalStateException failure = assertThrows(
            IllegalStateException.class,
            () -> ReloadableMutualTls.create(
                new MutualTlsMaterial.Settings(caPath.toString(), null, null)));

        assertTrue(failure.getMessage().contains("no client certificate is configured"));
    }

    @Test
    public void shouldRefuseToStartWithoutATrustAnchor() throws Exception {

        TestAuthority.Issued client = this.authority.issue("wallet2-connector");
        Path certPath = write("tls.crt", client.certificatePem());
        Path keyPath = write("tls.key", client.privateKeyPem());

        IllegalStateException failure = assertThrows(
            IllegalStateException.class,
            () -> ReloadableMutualTls.create(
                new MutualTlsMaterial.Settings(null, certPath.toString(), keyPath.toString())));

        assertTrue(failure.getMessage().contains("no certificate authority is configured"));
    }

    @Test
    public void shouldRejectHalfAPair() throws Exception {

        TestAuthority.Issued client = this.authority.issue("wallet2-connector");
        Path caPath = write("ca.pem", this.authority.caPem());
        Path certPath = write("tls.crt", client.certificatePem());

        IllegalStateException failure = assertThrows(
            IllegalStateException.class,
            () -> ReloadableMutualTls.create(
                new MutualTlsMaterial.Settings(caPath.toString(), certPath.toString(), null)));

        assertTrue(failure.getMessage().contains("must be configured together"));
    }

    @Test
    public void shouldPresentTheConfiguredClientCertificate() throws Exception {

        TestAuthority.Issued client = this.authority.issue("wallet2-connector");
        ReloadableMutualTls mutualTls = create(client);

        try (SSLServerSocket server = startServer()) {
            assertEquals("wallet2-connector", connectAndReadPresentedName(mutualTls, server));
        }
    }

    @Test
    public void shouldPresentTheRenewedCertificateAfterAReload() throws Exception {

        TestAuthority.Issued first = this.authority.issue("wallet2-connector");
        TestAuthority.Issued renewed = this.authority.issue("wallet2-connector-renewed");

        ReloadableMutualTls mutualTls = create(first);

        try (SSLServerSocket server = startServer()) {
            assertEquals("wallet2-connector", connectAndReadPresentedName(mutualTls, server));

            // Standing in for the renewed certificate being written into the mounted Secret.
            write("tls.crt", renewed.certificatePem());
            write("tls.key", renewed.privateKeyPem());

            assertTrue(mutualTls.reload());
            assertEquals("wallet2-connector-renewed", connectAndReadPresentedName(mutualTls, server));
        }
    }

    @Test
    public void shouldReportNoChangeWhenARewriteLeavesTheMaterialIdentical() throws Exception {

        TestAuthority.Issued client = this.authority.issue("wallet2-connector");
        ReloadableMutualTls mutualTls = create(client);

        // A Secret update rewrites the file whether or not the bytes changed.
        write("tls.crt", client.certificatePem());

        assertFalse(mutualTls.reload());
    }

    @Test
    public void shouldKeepServingWithTheLoadedMaterialWhenAReloadFails() throws Exception {

        TestAuthority.Issued client = this.authority.issue("wallet2-connector");
        ReloadableMutualTls mutualTls = create(client);

        // Half a certificate, as briefly seen while a mounted Secret is updated.
        write("tls.crt", client.certificatePem().substring(0, 120));

        assertFalse(mutualTls.reload());

        try (SSLServerSocket server = startServer()) {
            assertEquals("wallet2-connector", connectAndReadPresentedName(mutualTls, server));
        }
    }

    private ReloadableMutualTls create(TestAuthority.Issued client) throws Exception {

        Path caPath = write("ca.pem", this.authority.caPem());
        Path certPath = write("tls.crt", client.certificatePem());
        Path keyPath = write("tls.key", client.privateKeyPem());

        return ReloadableMutualTls.create(
            new MutualTlsMaterial.Settings(caPath.toString(), certPath.toString(), keyPath.toString()));
    }

    /** A TLS server that demands a client certificate and reports the name it saw. */
    private SSLServerSocket startServer() throws Exception {

        TestAuthority.Issued serverCert = this.authority.issue("localhost");

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry("server",
                             serverCert.privateKey(),
                             "in-memory".toCharArray(),
                             new X509Certificate[]{serverCert.certificate()});

        KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagers.init(keyStore, "in-memory".toCharArray());

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStore.setCertificateEntry("ca", this.authority.certificate());

        TrustManagerFactory trustManagers =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagers.init(trustStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagers.getKeyManagers(), trustManagers.getTrustManagers(), null);

        SSLServerSocket server =
            (SSLServerSocket) context.getServerSocketFactory().createServerSocket(0, 1, null);
        server.setNeedClientAuth(true);

        return server;
    }

    /**
     * Connects once and returns the common name the server saw on the client certificate.
     * <p>
     * The server reads a byte before reporting, because under TLS 1.3 the client certificate
     * arrives after the server's Finished — asking earlier would race the handshake.
     */
    private String connectAndReadPresentedName(ReloadableMutualTls mutualTls, SSLServerSocket server)
        throws Exception {

        Future<String> presented = this.serverThread.submit(() -> {
            try (SSLSocket accepted = (SSLSocket) server.accept()) {
                InputStream in = accepted.getInputStream();
                in.read();

                X509Certificate peer = (X509Certificate) accepted.getSession().getPeerCertificates()[0];

                return commonName(peer);
            }
        });

        try (SSLSocket socket = (SSLSocket) mutualTls.sslSocketFactory()
                                                     .createSocket("localhost", server.getLocalPort())) {
            OutputStream out = socket.getOutputStream();
            out.write('x');
            out.flush();

            return presented.get();
        }
    }

    private static String commonName(X509Certificate certificate) {

        for (String part : certificate.getSubjectX500Principal().getName().split(",")) {
            if (part.trim().startsWith("CN=")) {
                return part.trim().substring(3);
            }
        }

        return "";
    }

    private Path write(String name, String contents) throws Exception {

        Path file = this.workDir.getRoot().toPath().resolve(name);
        Files.writeString(file, contents, StandardCharsets.UTF_8);

        return file;
    }

}
