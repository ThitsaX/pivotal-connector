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
package com.thitsaworks.mojaloop.coreconnector.nats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import com.thitsaworks.mojaloop.coreconnector.component.util.LogMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import io.nats.client.*;
import io.nats.client.api.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
public class NatsService implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(NatsService.class);

    private final CoreConnectorConfiguration.Settings config;

    private final ObjectMapper objectMapper;

    private Connection connection;

    public NatsService(CoreConnectorConfiguration.Settings config, ObjectMapper objectMapper) {

        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Options
            options =
            new Options.Builder().server(config.getNatsUrl())
                                 .connectionName(config.getConnectorId())
                                 .reconnectWait(Duration.ofSeconds(1))
                                 .maxReconnects(-1)
                                 .build();

        this.connection = Nats.connect(options);
        log.info("Connected to NATS at {} with stream {}", config.getNatsUrl(), config.getFspiopStreamName());
    }

    @Override
    public void destroy() throws Exception {

        if (connection != null) {
            connection.drain(Duration.ofSeconds(10));
            connection.close();
            log.info("NATS connection drained and closed.");
        }
    }

    private Connection nc() {

        if (connection == null || connection.getStatus() == Connection.Status.CLOSED) {
            throw new IllegalStateException("NATS connection is not established");
        }
        return connection;
    }

    public JetStream jetstream() throws IOException {

        return nc().jetStream();
    }

    public JetStreamManagement jetstreamManager() throws IOException {

        return nc().jetStreamManagement();
    }

    public String ensureStream(JetStreamManagement jsm, String subject) throws Exception {

        String streamName = config.getFspiopStreamName();

        try {
            jsm.getStreamInfo(streamName);
            log.debug("Stream '{}' already exists for subject '{}'", streamName, subject);
            return streamName;
        } catch (JetStreamApiException e) {
            if (e.getErrorCode() != 404) {
                throw e;
            }
        }

        try {
            StreamConfiguration
                streamConfig =
                StreamConfiguration.builder()
                                   .name(streamName)
                                   .subjects("fspiop.>")
                                   .storageType(StorageType.File)
                                   .build();

            jsm.addStream(streamConfig);
            log.info("Created stream '{}' with subjects 'fspiop.>'", streamName);
        } catch (JetStreamApiException e) {
            String
                msg =
                e.getMessage() == null ? "" : e.getMessage()
                                               .toLowerCase();

            if (!msg.contains("stream name already in use")) {
                throw e;
            }

            log.debug("Stream '{}' already exists", streamName);
        }

        return streamName;
    }

    public void ensureConsumer(JetStreamManagement jsm, String stream, String durable, String filterSubject)
        throws Exception {

        try {
            jsm.getConsumerInfo(stream, durable);
            return;
        } catch (JetStreamApiException e) {
            if (e.getErrorCode() != 404) {
                throw e;
            }
        }

        ConsumerConfiguration
            consumerConfig =
            ConsumerConfiguration.builder()
                                 .durable(durable)
                                 .filterSubject(filterSubject)
                                 .ackPolicy(AckPolicy.Explicit)
                                 .deliverPolicy(DeliverPolicy.All)
                                 .replayPolicy(ReplayPolicy.Instant)
                                 .ackWait(Duration.ofSeconds(60))
                                 .maxDeliver(5)
                                 .maxAckPending(100)
                                 .build();

        jsm.addOrUpdateConsumer(stream, consumerConfig);
        log.info("Created consumer '{}' on stream '{}' filtering '{}'", durable, stream, filterSubject);
    }

    public JetStreamSubscription pullSubscribe(String subject, String stream, String durable) throws Exception {

        PullSubscribeOptions
            options =
            PullSubscribeOptions.builder()
                                .stream(stream)
                                .durable(durable)
                                .build();

        return jetstream().subscribe(subject, options);
    }

    public <T> T deserialize(byte[] data, Class<T> clazz) {

        try {
            return objectMapper.readValue(data, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Message deserialization failed", e);
        }
    }

    public byte[] serialize(Object value) {

        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new RuntimeException("Message serialization failed", e);
        }
    }

    public String buildSubject(String... parts) {

        return String.join(".", parts);
    }

    public String getPartiesSubject() {

        return buildSubject("fspiop", config.getConnectorId(), "get", "parties");
    }

    public String getQuotesSubject() {

        return buildSubject("fspiop", config.getConnectorId(), "post", "quotes");
    }

    public String getTransfersSubject() {

        return buildSubject("fspiop", config.getConnectorId(), "post", "transfers");
    }

    public String getPatchTransfersSubject() {

        return buildSubject("fspiop", config.getConnectorId(), "patch", "transfers");
    }

    public String normalizeDurable(String connectorId, String prefix) {

        String
            suffix =
            connectorId == null ? "" : connectorId.trim()
                                                  .toLowerCase()
                                                  .replaceAll("[^a-z0-9_-]", "-");

        if (suffix.isEmpty()) {
            suffix = "default";
        }

        return prefix + "-" + suffix;
    }

    public String sanitizeLogMessage(Object message) {

        return LogMasker.mask(message, objectMapper);
    }

}
