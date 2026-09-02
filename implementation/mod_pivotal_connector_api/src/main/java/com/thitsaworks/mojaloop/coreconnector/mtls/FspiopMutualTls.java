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
package com.thitsaworks.mojaloop.coreconnector.mtls;

import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.mtls.MutualTlsMaterial;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.mtls.ReloadableMutualTls;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Assembles the Hub-facing mutual TLS path and applies it to an OkHttp client.
 * <p>
 * A component-scanned {@code @Component} rather than a {@code @Bean} on
 * {@link CoreConnectorConfiguration}, for the same reason the JWS signer is: deployable connectors
 * exclude that configuration and import their own, so a bean declared there reaches only part of
 * the fleet, whereas every deployable scans this package.
 * <p>
 * That same fact is why this applies itself to a <em>derived</em> client rather than configuring
 * the shared one. Deployables override {@code sharedOkHttpClient}, so a pre-configured client
 * cannot be relied on to carry anything.
 */
@Component
public class FspiopMutualTls implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(FspiopMutualTls.class);

    private final CoreConnectorConfiguration.Settings config;

    private ReloadableMutualTls mutualTls;

    private ScheduledExecutorService reloader;

    public FspiopMutualTls(CoreConnectorConfiguration.Settings config) {

        this.config = config;
    }

    @Override
    public void afterPropertiesSet() {

        if (!this.config.isFspiopUseMutualTls()) {
            LOG.info("Hub-facing mutual TLS is disabled; outbound callbacks will not present a certificate.");
            return;
        }

        this.mutualTls = ReloadableMutualTls.create(
            new MutualTlsMaterial.Settings(this.config.getFspiopMtlsCaPath(),
                                           this.config.getFspiopMtlsClientCertPath(),
                                           this.config.getFspiopMtlsClientKeyPath()));

        // Polled rather than watched. A Secret update arrives as an atomic symlink swap that file
        // watches report inconsistently, and the kubelet's own sync period is around a minute
        // regardless, so a faster or cleverer mechanism would buy nothing.
        long intervalMs = this.config.getFspiopMtlsReloadIntervalMs();

        this.reloader = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "fspiop-mtls-reload");
            thread.setDaemon(true);
            return thread;
        });
        this.reloader.scheduleWithFixedDelay(this::reload, intervalMs, intervalMs, TimeUnit.MILLISECONDS);

        LOG.info("Hub-facing mutual TLS is enabled; material is re-read every {} ms.", intervalMs);
    }

    /**
     * Installs the client certificate and trust anchor on a derived client.
     * <p>
     * Returns the builder unchanged when mutual TLS is off, so a caller can apply this
     * unconditionally.
     */
    public OkHttpClient.Builder apply(OkHttpClient.Builder builder) {

        if (this.mutualTls == null) {
            return builder;
        }

        return builder.sslSocketFactory(this.mutualTls.sslSocketFactory(), this.mutualTls.trustManager());
    }

    /** Re-reads the material. For a rotation nudge — never per request. */
    public boolean reload() {

        return this.mutualTls != null && this.mutualTls.reload();
    }

    @Override
    public void destroy() {

        if (this.reloader != null) {
            this.reloader.shutdownNow();
        }
    }

}
