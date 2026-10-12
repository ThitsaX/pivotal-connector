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

package com.thitsaworks.mojaloop.coreconnector.dispute;

import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import com.thitsaworks.mojaloop.coreconnector.audit.AuditPublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(
    name = "disputeSchedulerEnabled",
    havingValue = "true")
public class DisputeStatusScheduler implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(DisputeStatusScheduler.class);

    private final DisputeStatusStore disputeStatusStore;

    private final DisputeStatusClient disputeStatusClient;

    private final AuditPublisherService auditPublisherService;

    private final CoreConnectorConfiguration.Settings settings;

    private final ScheduledExecutorService checker = Executors.newSingleThreadScheduledExecutor(
        r -> {
            Thread thread = new Thread(r, "dispute-status-scheduler");
            thread.setDaemon(true);
            return thread;
        });

    @Autowired
    public DisputeStatusScheduler(DisputeStatusStore disputeStatusStore,
                                  DisputeStatusClient disputeStatusClient,
                                  AuditPublisherService auditPublisherService,
                                  CoreConnectorConfiguration.Settings settings) {

        this.disputeStatusStore = disputeStatusStore;
        this.disputeStatusClient = disputeStatusClient;
        this.auditPublisherService = auditPublisherService;
        this.settings = settings;
    }

    @Override
    public void afterPropertiesSet() {

        LOG.info(
            "Starting dispute status scheduler with initial delay={} minute(s), check period={} minute(s).",
            this.settings.getDisputeSchedulerIntervalMinutes(),
            this.settings.getDisputeSchedulerIntervalMinutes());

        this.checker.scheduleAtFixedRate(
            this::checkDisputedTransaction,
            this.settings.getDisputeSchedulerIntervalMinutes(),
            this.settings.getDisputeSchedulerIntervalMinutes(), TimeUnit.MINUTES);
    }

    @Override
    public void destroy() {

        LOG.info("Stopping dispute status scheduler.");

        this.checker.shutdownNow();
    }

    private void checkDisputedTransaction() {

        Instant schedulerRuntime = Instant.now();

        LOG.info(
            "Running dispute status scheduler with disputed transaction count={}, schedulerRuntime={}, connectorId={}.",
            this.disputeStatusStore.getPendingDisputedTransactionCount(), schedulerRuntime, this.settings.getConnectorId());

        this.disputeStatusStore.getPendingDisputedTransactions().forEach(disputedTransaction -> {

            Duration elapsed = this.getDisputeDuration(disputedTransaction, schedulerRuntime);

            boolean isReadyForStatusApi = this.isReadyForStatusCheck(elapsed);

            LOG.info(
                "Evaluating dispute status for transactionId {}, disputeElapsedDuration= {}seconds, readyForStatusCheck={}.",
                disputedTransaction.transactionId(), elapsed.toSeconds(),
                isReadyForStatusApi);

            if (isReadyForStatusApi) {

                try {
                    this.checkDisputedTransaction(disputedTransaction);
                } catch (Exception e) {
                    LOG.error(
                        "Disputed transaction status check failed for transactionId {}.",
                        disputedTransaction.transactionId(), e);
                }
            }
        });
    }

    private void checkDisputedTransaction(DisputedTransaction disputedTransaction)
        throws Exception {

        LOG.info(
            "Checking disputed transaction for transactionId {}.",
            disputedTransaction.transactionId());

        DisputeStatus disputeStatus = this.resolveDisputeStatus(disputedTransaction);

        boolean actualDispute = DisputeStatus.ACTUAL_DISPUTE.equals(disputeStatus);

        LOG.info(
            "Disputed transaction status check finished for transactionId {}. finalDisputeStatus={}, dispute={}.",
            disputedTransaction.transactionId(), disputeStatus, actualDispute);

        if (actualDispute) {

            LOG.info(
                "Confirmed dispute for transactionId {} because transaction status is not successful.",
                disputedTransaction.transactionId());

            return;
        }

        LOG.info(
            "Resolved dispute for transactionId {} because transaction status is successful.",
            disputedTransaction.transactionId());

        this.auditPublisherService.publishDisputeStatus(
            new AuditPublisherService.DisputeResultInput(
                disputedTransaction.transactionId()));

        this.disputeStatusStore.removeDisputedTransaction(disputedTransaction.transactionId());
    }

    private DisputeStatus resolveDisputeStatus(DisputedTransaction disputedTransfer) {

        try {

            LOG.info(
                "Calling Get Transaction Status for transactionId {}.",
                disputedTransfer.transactionId());

            DisputeStatus disputeStatus = this.disputeStatusClient.checkStatus(
                disputedTransfer.transactionId(), disputedTransfer.homeTransactionId());

            if (disputeStatus == null) {
                LOG.info(
                    "Get Transaction Status returned null for transactionId {}. Treating as actual dispute.",
                    disputedTransfer.transactionId());

                return DisputeStatus.ACTUAL_DISPUTE;
            }

            LOG.info(
                "Get Transaction Status returned {} for transactionId {}.", disputeStatus,
                disputedTransfer.transactionId());

            return disputeStatus;

        } catch (Exception e) {

            LOG.error(
                "Get Transaction Status failed for transactionId {}. Treating as actual dispute.",
                disputedTransfer.transactionId(), e);

            return DisputeStatus.ACTUAL_DISPUTE;
        }
    }

    private boolean isReadyForStatusCheck(Duration elapsed) {

        Duration disputeStatusCheckDelay = Duration.ofSeconds(
            this.settings.getDisputeStatusCheckDelaySeconds());

        return elapsed.compareTo(disputeStatusCheckDelay) >= 0;
    }

    private Duration getDisputeDuration(DisputedTransaction disputedTransaction,
                                        Instant schedulerRuntime) {

        LOG.info(
            "Getting disputed transaction duration for transactionId {}, disputed date time {}.",
            disputedTransaction.transactionId(), disputedTransaction.disputedDateTime());

        return Duration.between(disputedTransaction.disputedDateTime(), schedulerRuntime);
    }

}
