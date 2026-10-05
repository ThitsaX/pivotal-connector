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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(
    name = "disputeSchedulerEnabled",
    havingValue = "true"
)
public class DisputeStatusChecker implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(DisputeStatusChecker.class);

    private final DisputeStatusStore disputeStatusStore;

    private final DisputeStatusClient disputeStatusClient;

    private final AuditPublisherService auditPublisherService;

    private final CoreConnectorConfiguration.Settings settings;

    private final ScheduledExecutorService checker = Executors.newSingleThreadScheduledExecutor(
        r -> {
            Thread thread = new Thread(r, "dispute-status-checker");
            thread.setDaemon(true);
            return thread;
        });

    @Autowired
    public DisputeStatusChecker(DisputeStatusStore disputeStatusStore,
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
            "Starting dispute status checker with initial delay={} minute(s), check period={} minute(s).",
            this.settings.getDisputeSchedulerIntervalMinutes(),
            this.settings.getDisputeSchedulerIntervalMinutes());

        this.checker.scheduleAtFixedRate(
            this::checkDisputedTransaction,
            this.settings.getDisputeSchedulerIntervalMinutes(),
            this.settings.getDisputeSchedulerIntervalMinutes(),
            TimeUnit.MINUTES);
    }

    @Override
    public void destroy() {

        LOG.info("Stopping dispute status checker.");

        this.checker.shutdownNow();
    }

    private void checkDisputedTransaction() {

        LOG.info(
            "Running dispute status check with disputed transaction count={}.",
            this.disputeStatusStore.getPendingDisputedTransactionCount());

        this.disputeStatusStore.getPendingDisputedTransactions().forEach(disputedTransaction -> {

            LOG.info(
                "Evaluating disputed transactionId {}. disputeDuration={} ms, readyForStatusCheck={}.",
                disputedTransaction.transactionId(), this.getDisputeDurationMillis(disputedTransaction),
                this.isReadyForStatusCheck(disputedTransaction));

            if (this.isReadyForStatusCheck(disputedTransaction)) {
                try {
                    this.checkDisputedTransaction(disputedTransaction);
                } catch (Exception e) {
                    LOG.error(
                        "Dispute status processing failed for transactionId {}.",
                        disputedTransaction.transactionId(), e);
                }
            }
        });
    }

    private void checkDisputedTransaction(DisputeStatusStore.DisputedTransaction disputedTransaction)
        throws Exception {

        LOG.info(
            "Checking disputed transaction for transactionId {} with extensionList={}.",
            disputedTransaction.transactionId(), disputedTransaction.extensionList());

        DisputeStatus disputeStatus = this.resolveDisputeStatus(disputedTransaction);

        this.disputeStatusStore.removeDisputedTransaction(disputedTransaction.transactionId());

        boolean dispute = DisputeStatus.ACTUAL_DISPUTE.equals(disputeStatus);

        LOG.info(
            "Dispute check finished for transactionId {}. finalDisputeStatus={}, dispute={}.",
            disputedTransaction.transactionId(), disputeStatus, dispute);

        if (dispute) {

            LOG.info(
                "Confirmed dispute for transactionId {} because transaction status is not successful.",
                disputedTransaction.transactionId());
            return;
        }

        LOG.info(
            "Resolved dispute for transactionId {} because transaction status is successful.",
            disputedTransaction.transactionId());

        this.auditPublisherService.publishDisputeStatus(
            new AuditPublisherService.DisputeResultInput(disputedTransaction.transactionId(), false));
    }

    private DisputeStatus resolveDisputeStatus(DisputeStatusStore.DisputedTransaction disputedTransfer) {

        try {

            LOG.info(
                "Calling Get Transaction Status for transactionId {}.",
                disputedTransfer.transactionId());

            DisputeStatus disputeStatus = this.disputeStatusClient.checkStatus(
                disputedTransfer.transactionId());

            if (disputeStatus == null) {
                LOG.info(
                    "Dispute status check returned null for transactionId {}. Treating as actual dispute.",
                    disputedTransfer.transactionId());

                return DisputeStatus.ACTUAL_DISPUTE;
            }

            LOG.info(
                "Get Transaction Status returned {} for transactionId {}.",
                disputeStatus, disputedTransfer.transactionId());

            return disputeStatus;

        } catch (Exception e) {

            LOG.error(
                "Dispute status check failed for transactionId {}. Treating as actual dispute.",
                disputedTransfer.transactionId(), e);

            return DisputeStatus.ACTUAL_DISPUTE;
        }
    }

    private boolean isReadyForStatusCheck(DisputeStatusStore.DisputedTransaction disputedTransfer) {

        return System.currentTimeMillis() - disputedTransfer.disputedDateTime().toEpochMilli() >=
                   TimeUnit.MINUTES.toMillis(1);
    }

    private long getDisputeDurationMillis(DisputeStatusStore.DisputedTransaction disputedTransaction) {

        LOG.info(
            "Getting dispute duration for transactionId {}, disputed at {}.",
            disputedTransaction.transactionId(), disputedTransaction.disputedDateTime());

        return System.currentTimeMillis() - disputedTransaction.disputedDateTime().toEpochMilli();
    }

}
