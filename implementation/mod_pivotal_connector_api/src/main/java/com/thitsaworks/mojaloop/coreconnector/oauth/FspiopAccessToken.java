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
package com.thitsaworks.mojaloop.coreconnector.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.oauth.ClientCredentialsTokenProvider;
import com.thitsaworks.mojaloop.coreconnector.component.fspiop.oauth.FspiopAccessTokenInterceptor;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Assembles the Hub bearer token and applies it to an OkHttp client.
 * <p>
 * A component-scanned {@code @Component} for the same reason as the JWS signer and mutual TLS:
 * deployable connectors exclude the framework's configuration and import their own, so a
 * {@code @Bean} declared there would reach only part of the fleet.
 */
@Component
public class FspiopAccessToken implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(FspiopAccessToken.class);

    private final CoreConnectorConfiguration.Settings config;

    private final OkHttpClient http;

    private final ObjectMapper objectMapper;

    private FspiopAccessTokenInterceptor interceptor;

    public FspiopAccessToken(CoreConnectorConfiguration.Settings config, OkHttpClient http, ObjectMapper objectMapper) {

        this.config = config;
        this.http = http;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterPropertiesSet() {

        String tokenUrl = this.config.getFspiopOauthTokenUrl();
        String clientId = this.config.getFspiopOauthClientId();
        String clientSecret = this.config.getFspiopOauthClientSecret();

        boolean anySet = !tokenUrl.isBlank() || !clientId.isBlank() || !clientSecret.isBlank();
        boolean allSet = !tokenUrl.isBlank() && !clientId.isBlank() && !clientSecret.isBlank();

        if (!anySet) {
            LOG.info("No Hub token configured; outbound callbacks will be sent without one.");
            return;
        }

        if (!allSet) {
            // A partial set is a typo, not a choice. Read as "off", it would surface only as every
            // callback being refused by the Hub's gateway, far from the cause.
            throw new IllegalStateException(
                "fspiopOauthTokenUrl, fspiopOauthClientId and fspiopOauthClientSecret must be set "
                    + "together, or not at all.");
        }

        // The framework's own client rather than the derived one callbacks use: the identity
        // provider is not the Hub, so it gets neither the client certificate nor the signature.
        this.interceptor = new FspiopAccessTokenInterceptor(
            new ClientCredentialsTokenProvider(tokenUrl, clientId, clientSecret, this.http, this.objectMapper,
                                               Clock.systemUTC()));

        LOG.info("Outbound callbacks will carry a Hub token for client '{}'.", clientId);
    }

    /**
     * Installs the token on a derived client.
     * <p>
     * Returns the builder unchanged when no token is configured, so a caller can apply this
     * unconditionally.
     */
    public OkHttpClient.Builder apply(OkHttpClient.Builder builder) {

        if (this.interceptor == null) {
            return builder;
        }

        return builder.addInterceptor(this.interceptor);
    }

}
