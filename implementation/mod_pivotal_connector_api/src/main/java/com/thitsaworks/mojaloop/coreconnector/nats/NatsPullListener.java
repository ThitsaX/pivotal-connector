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

package com.thitsaworks.mojaloop.coreconnector.nats;

import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import com.thitsaworks.mojaloop.coreconnector.logging.MdcContext;
import io.nats.client.ConsumeOptions;
import io.nats.client.ConsumerContext;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageConsumer;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class NatsPullListener<T> {

    @FunctionalInterface
    public interface Handler<T> {

        void handle(T message) throws Exception;

    }

    private final Logger log;

    private final NatsService natsService;

    private final String operationName;

    private final Class<T> messageType;

    private final Handler<T> handler;

    private final Function<T, Map<String, String>> mdcExtractor;

    private final int batchSize;

    // Submitting a task and shutting down the executor must be mutually exclusive.
    private final Object lifecycle = new Object();

    private volatile boolean running;

    private boolean started;

    private ExecutorService workers;

    private Dispatcher dispatcher;

    private MessageConsumer consumer;

    public NatsPullListener(Logger log,
                            NatsService natsService,
                            CoreConnectorConfiguration.Settings config,
                            String operationName,
                            Class<T> messageType,
                            Handler<T> handler,
                            Function<T, Map<String, String>> mdcExtractor) {

        this.log = log;
        this.natsService = natsService;
        this.operationName = operationName;
        this.messageType = messageType;
        this.handler = handler;
        this.mdcExtractor = mdcExtractor != null ? mdcExtractor : ignored -> Collections.emptyMap();
        this.batchSize = config.getNatsPullBatchSize();
    }

    public synchronized void start(String subject, String stream, String durable, String threadName) throws Exception {

        if (started) {
            throw new IllegalStateException("Listener has already been started");
        }
        started = true;
        try {
            synchronized (lifecycle) {
                ConsumerContext context = natsService.consumerContext(stream, durable);
                workers = Executors.newThreadPerTaskExecutor(
                    Thread.ofVirtual().name(threadName + "-worker-", 1).factory());
                dispatcher = natsService.createDispatcher();
                running = true;
                consumer = context.consume(ConsumeOptions.builder()
                                                         .batchSize(batchSize)
                                                         .build(), dispatcher, this::dispatch);
                log.info("{} consuming '{}' with pull batch size {} using a virtual thread per message",
                         operationName, subject, batchSize);
            }
        } catch (Exception err) {
            stop();
            throw err;
        }
    }

    private void dispatch(Message msg) {

        synchronized (lifecycle) {
            if (!running) {
                // Do not ack a message that shutdown prevented us from processing.
                return;
            }
            try {
                // JetStream maxAckPending, rather than a local worker pool/semaphore,
                // supplies the delivery budget across all pods sharing this durable.
                workers.execute(() -> handle(msg));
            } catch (RejectedExecutionException err) {
                log.warn("{} executor rejected message; leaving it unacked for redelivery", operationName, err);
            }
        }
    }

    public synchronized void stop() {

        ExecutorService currentWorkers;
        synchronized (lifecycle) {
            running = false;
            currentWorkers = workers;
            if (currentWorkers != null) {
                currentWorkers.shutdown();
            }
        }
        closeConsumerAndDispatcher();

        boolean interrupted = false;
        if (currentWorkers != null) {
            boolean finished = false;
            while (!finished) {
                try {
                    finished = currentWorkers.awaitTermination(10, TimeUnit.SECONDS);
                    if (!finished) {
                        log.warn("{} still waiting for in-flight handlers to finish before shutdown", operationName);
                    }
                } catch (InterruptedException err) {
                    interrupted = true;
                }
            }
        }
        if (interrupted) {
            Thread.currentThread()
                  .interrupt();
        }
    }

    private void closeConsumerAndDispatcher() {

        if (consumer != null) {
            try {
                consumer.stop();
                consumer.close();
            } catch (Exception err) {
                log.warn("{} consumer close failed: {}", operationName, err.getMessage());
            } finally {
                consumer = null;
            }
        }
        if (dispatcher != null) {
            try {
                natsService.closeDispatcher(dispatcher);
            } catch (Exception err) {
                log.warn("{} dispatcher close failed: {}", operationName, err.getMessage());
            } finally {
                dispatcher = null;
            }
        }
    }

    private void handle(Message msg) {

        try {
            T data = natsService.deserialize(msg.getData(), messageType);
            try (MdcContext.Scope ignored = MdcContext.open(mdcExtractor.apply(data))) {
                handler.handle(data);
            }
        } catch (Exception err) {
            log.error("{} failed - acking to avoid requeue: {}", operationName, err.getMessage(), err);
        } finally {
            try {
                msg.ack();
            } catch (Exception ackError) {
                log.warn("{} message ack failed: {}", operationName, ackError.getMessage());
            }
        }
    }

}
