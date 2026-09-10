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
package com.thitsaworks.mojaloop.coreconnector.tazama;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorToTazamaConfiguration {

    @Bean
    public ConnectorToTazamaPublisher.Settings connectorToTazamaSettings() {

        String bootstrapServers = System.getProperty("connectorToTazamaKafkaBootstrapServers");
        String enabledProperty = System.getProperty("connectorToTazamaKafkaEnabled");
        boolean enabled = "true".equalsIgnoreCase(enabledProperty) ||
            (enabledProperty == null && bootstrapServers != null && !bootstrapServers.trim()
                                                                                      .isEmpty());

        return new ConnectorToTazamaPublisher.Settings(
            enabled,
            bootstrapServers,
            getPropertyOrDefault("connectorToTazamaKafkaTopic", "test-topic"),
            getPropertyOrDefault("connectorToTazamaKafkaClientId", "connector-to-tazama"));
    }

    private String getPropertyOrDefault(String propertyName, String defaultValue) {

        String value = System.getProperty(propertyName);
        if (value == null || value.trim()
                                  .isEmpty()) {
            return defaultValue;
        }
        return value;
    }

}
