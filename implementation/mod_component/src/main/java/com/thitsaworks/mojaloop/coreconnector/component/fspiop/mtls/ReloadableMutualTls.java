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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Mutual TLS whose certificate and trust anchor can change while the connector runs.
 * <p>
 * Certificates are renewed on a cadence of weeks. Reading them once at startup means a renewal
 * takes effect only at the next restart, which turns routine rotation into a scheduled outage.
 * <p>
 * The socket factory is created once and never replaced; what changes is what the key and trust
 * managers behind it delegate to. Those are consulted per handshake, so a swap reaches new
 * connections while established ones continue on the material they negotiated with — the overlap
 * both certificates are valid for. It also means the OkHttp client, its connection pool and its
 * dispatcher all survive a rotation untouched.
 */
public final class ReloadableMutualTls {

    private static final Logger LOG = LoggerFactory.getLogger(ReloadableMutualTls.class);

    /** Guards an in-memory key store that is never written anywhere. */
    private static final char[] KEY_STORE_PASSWORD = "in-memory".toCharArray();

    private final MutualTlsMaterial.Settings settings;

    private final AtomicReference<X509ExtendedKeyManager> keyManager = new AtomicReference<>();

    private final AtomicReference<X509ExtendedTrustManager> trustManager = new AtomicReference<>();

    private final AtomicReference<String> fingerprint = new AtomicReference<>();

    private final X509ExtendedTrustManager delegatingTrustManager = new DelegatingTrustManager();

    private SSLSocketFactory socketFactory;

    private ReloadableMutualTls(MutualTlsMaterial.Settings settings) {

        this.settings = settings;
    }

    /**
     * Loads the material and builds the socket factory.
     *
     * @throws IllegalStateException when either half is missing. Mutual TLS names both directions:
     *                               without a client certificate the peer cannot authenticate this
     *                               connector, and without a trust anchor this connector cannot
     *                               authenticate the peer.
     */
    public static ReloadableMutualTls create(MutualTlsMaterial.Settings settings) {

        MutualTlsMaterial material = MutualTlsMaterial.read(settings);

        if (!material.hasClientCertificate()) {
            throw new IllegalStateException(
                "Mutual TLS is enabled but no client certificate is configured. " +
                    "Set fspiopMtlsClientCertPath and fspiopMtlsClientKeyPath.");
        }

        if (!material.hasTrustAnchors()) {
            throw new IllegalStateException(
                "Mutual TLS is enabled but no certificate authority is configured, so the peer " +
                    "could not be verified. Set fspiopMtlsCaPath.");
        }

        ReloadableMutualTls reloadable = new ReloadableMutualTls(settings);
        reloadable.initialise(material);

        return reloadable;
    }

    public SSLSocketFactory sslSocketFactory() {

        return this.socketFactory;
    }

    /** The trust manager OkHttp needs alongside the socket factory. */
    public X509ExtendedTrustManager trustManager() {

        return this.delegatingTrustManager;
    }

    /**
     * Re-reads the material and swaps it in when it has changed.
     * <p>
     * Failures are logged and swallowed: a Secret being updated is briefly half-written, and the
     * material already loaded is still valid, so the connector keeps using it and tries again on
     * the next pass. Taking the process down over a transient read would turn a rotation into the
     * outage this exists to avoid.
     *
     * @return whether the material actually changed
     */
    public boolean reload() {

        try {
            MutualTlsMaterial material = MutualTlsMaterial.read(this.settings);

            if (!material.hasClientCertificate() || !material.hasTrustAnchors()) {
                return false;
            }

            if (material.fingerprint().equals(this.fingerprint.get())) {
                return false;
            }

            this.keyManager.set(buildKeyManager(material));
            this.trustManager.set(buildTrustManager(material));
            this.fingerprint.set(material.fingerprint());

            LOG.info("Reloaded mutual TLS material; new connections will use it.");

            return true;
        } catch (RuntimeException | GeneralSecurityException e) {
            LOG.error(
                "Could not reload mutual TLS material; keeping the current one: {}",
                e.getMessage());

            return false;
        }
    }

    private void initialise(MutualTlsMaterial material) {

        try {
            this.keyManager.set(buildKeyManager(material));
            this.trustManager.set(buildTrustManager(material));
            this.fingerprint.set(material.fingerprint());

            // The delegating managers close over this instance, so the socket factory built here
            // reads whatever a later reload puts into those references.
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(
                new KeyManager[]{new DelegatingKeyManager()},
                new TrustManager[]{this.delegatingTrustManager}, new SecureRandom());

            this.socketFactory = context.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not initialise mutual TLS.", e);
        }
    }

    private static X509ExtendedKeyManager buildKeyManager(MutualTlsMaterial material)
        throws GeneralSecurityException {

        try {
            KeyStore store = KeyStore.getInstance("PKCS12");
            store.load(null, null);
            store.setKeyEntry(
                "client", material.clientKey(), KEY_STORE_PASSWORD,
                material.clientChain().toArray(new X509Certificate[0]));

            KeyManagerFactory factory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
            factory.init(store, KEY_STORE_PASSWORD);

            for (KeyManager manager : factory.getKeyManagers()) {
                if (manager instanceof X509ExtendedKeyManager extended) {
                    return extended;
                }
            }

            throw new IllegalStateException("The JVM produced no X.509 key manager.");
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Could not assemble the client key store.", e);
        }
    }

    private static X509ExtendedTrustManager buildTrustManager(MutualTlsMaterial material)
        throws GeneralSecurityException {

        try {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(null, null);

            List<X509Certificate> anchors = material.trustAnchors();
            for (int index = 0; index < anchors.size(); index++) {
                store.setCertificateEntry("ca-" + index, anchors.get(index));
            }

            TrustManagerFactory factory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
            factory.init(store);

            for (TrustManager manager : factory.getTrustManagers()) {
                if (manager instanceof X509ExtendedTrustManager extended) {
                    return extended;
                }
            }

            throw new IllegalStateException("The JVM produced no X.509 trust manager.");
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Could not assemble the trust store.", e);
        }
    }

    /**
     * Forwards every call to whichever key manager is current.
     * <p>
     * The JSSE resolves the key manager per handshake, which is what makes a swap take effect
     * without rebuilding the socket factory.
     */
    private final class DelegatingKeyManager extends X509ExtendedKeyManager {

        @Override
        public String[] getClientAliases(String keyType, Principal[] issuers) {

            return ReloadableMutualTls.this.keyManager.get().getClientAliases(keyType, issuers);
        }

        @Override
        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {

            return ReloadableMutualTls.this.keyManager
                       .get()
                       .chooseClientAlias(keyType, issuers, socket);
        }

        @Override
        public String chooseEngineClientAlias(String[] keyType,
                                              Principal[] issuers,
                                              SSLEngine engine) {

            return ReloadableMutualTls.this.keyManager
                       .get()
                       .chooseEngineClientAlias(keyType, issuers, engine);
        }

        @Override
        public String[] getServerAliases(String keyType, Principal[] issuers) {

            return ReloadableMutualTls.this.keyManager.get().getServerAliases(keyType, issuers);
        }

        @Override
        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {

            return ReloadableMutualTls.this.keyManager
                       .get()
                       .chooseServerAlias(keyType, issuers, socket);
        }

        @Override
        public String chooseEngineServerAlias(String keyType,
                                              Principal[] issuers,
                                              SSLEngine engine) {

            return ReloadableMutualTls.this.keyManager
                       .get()
                       .chooseEngineServerAlias(keyType, issuers, engine);
        }

        @Override
        public X509Certificate[] getCertificateChain(String alias) {

            return ReloadableMutualTls.this.keyManager.get().getCertificateChain(alias);
        }

        @Override
        public PrivateKey getPrivateKey(String alias) {

            return ReloadableMutualTls.this.keyManager.get().getPrivateKey(alias);
        }

    }

    /** Forwards every call to whichever trust manager is current. */
    private final class DelegatingTrustManager extends X509ExtendedTrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

            ReloadableMutualTls.this.trustManager.get().checkClientTrusted(chain, authType);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {

            ReloadableMutualTls.this.trustManager.get().checkClientTrusted(chain, authType, socket);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {

            ReloadableMutualTls.this.trustManager.get().checkClientTrusted(chain, authType, engine);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

            ReloadableMutualTls.this.trustManager.get().checkServerTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {

            ReloadableMutualTls.this.trustManager.get().checkServerTrusted(chain, authType, socket);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {

            ReloadableMutualTls.this.trustManager.get().checkServerTrusted(chain, authType, engine);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {

            return ReloadableMutualTls.this.trustManager.get().getAcceptedIssuers();
        }

    }

}
