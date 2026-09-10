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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class ConnectorToTazamaPublisher implements DisposableBean {

    public static final String QUOTE_RESPONSE = "core.quote.response";

    public static final String TRANSFER_REQUEST = "core.transfer.request";

    public static final String TRANSFER_RESPONSE = "core.transfer.response";

    private static final Logger LOG = LoggerFactory.getLogger(ConnectorToTazamaPublisher.class);

    private final ObjectMapper objectMapper;

    private final Settings settings;

    private final Producer<String, String> producer;

    public ConnectorToTazamaPublisher(ObjectMapper objectMapper, Settings settings) {

        this.objectMapper = objectMapper;
        this.settings = settings;
        this.producer = createProducer(settings);
    }

    public void publishQuoteResponse(String quoteId, Object payload) {

        publish(QUOTE_RESPONSE, quoteId, payload);
    }

    public void publishTransferRequest(String transferId, Object payload) {

        publish(TRANSFER_REQUEST, transferId, payload);
    }

    public void publishTransferResponse(String transferId, Object payload) {

        publish(TRANSFER_RESPONSE, transferId, payload);
    }

    public void publish(String eventType, String key, Object payload) {

        if (!settings.isEnabled()) {
            LOG.debug("Connector to Tazama Kafka publisher disabled, skipping {}", eventType);
            return;
        }

        if (producer == null) {
            LOG.warn("Connector to Tazama Kafka publisher is enabled but not configured, skipping {}", eventType);
            return;
        }

        try {
            String message = objectMapper.writeValueAsString(new ConnectorToTazamaEvent(eventType, payload));

            producer.send(new ProducerRecord<>(settings.getTopic(), key, message), (metadata, exception) -> {
                if (exception != null) {
                    LOG.error("Failed to publish {} event to Tazama for key {}", eventType, key, exception);
                    return;
                }

                LOG.info("Published {} event to Tazama topic {} partition {} offset {} for key {}",
                         eventType,
                         metadata.topic(),
                         metadata.partition(),
                         metadata.offset(),
                         key);
            });
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize {} event for Tazama with key {}", eventType, key, e);
        } catch (RuntimeException e) {
            LOG.error("Failed to publish {} event to Tazama for key {}", eventType, key, e);
        }
    }

    @Override
    public void destroy() {

        if (producer != null) {
            producer.close();
        }
    }

    private Producer<String, String> createProducer(Settings settings) {

        if (!settings.isEnabled()) {
            return null;
        }

        if (settings.getBootstrapServers() == null || settings.getBootstrapServers()
                                                            .trim()
                                                            .isEmpty()) {
            return null;
        }

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.getBootstrapServers());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, settings.getClientId());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "1000");

        return new KafkaProducer<>(properties);
    }

    public static class Settings {

        private final boolean enabled;

        private final String bootstrapServers;

        private final String topic;

        private final String clientId;

        public Settings(boolean enabled, String bootstrapServers, String topic, String clientId) {

            this.enabled = enabled;
            this.bootstrapServers = bootstrapServers;
            this.topic = topic;
            this.clientId = clientId;
        }

        public boolean isEnabled() {

            return enabled;
        }

        public String getBootstrapServers() {

            return bootstrapServers;
        }

        public String getTopic() {

            return topic;
        }

        public String getClientId() {

            return clientId;
        }

    }

}
