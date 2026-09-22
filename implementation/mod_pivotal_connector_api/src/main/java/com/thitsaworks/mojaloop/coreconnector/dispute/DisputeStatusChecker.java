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

import com.thitsaworks.mojaloop.coreconnector.audit.AuditPublisherService;
import com.thitsaworks.mojaloop.coreconnector.services.FspClientService;
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
    name = "pivotalDisputeStatusEnabled",
    havingValue = "true"
)
public class DisputeStatusChecker implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(DisputeStatusChecker.class);

    private static final long STATUS_CHECK_INITIAL_DELAY_MINUTES = 1L;

    private static final long STATUS_CHECK_PERIOD_MINUTES = 1L;

    private static final long STATUS_RETRY_DELAY_SECONDS = 10L;

    private final DisputeStatusStore disputeStatusStore;

    private final FspClientService fspClientService;

    private final AuditPublisherService auditPublisherService;

    private final ScheduledExecutorService checker = Executors.newSingleThreadScheduledExecutor(
        r -> {
            Thread thread = new Thread(r, "dispute-status-checker");
            thread.setDaemon(true);
            return thread;
        });

    @Autowired
    public DisputeStatusChecker(DisputeStatusStore disputeStatusStore,
                                FspClientService fspClientService,
                                AuditPublisherService auditPublisherService) {

        this.disputeStatusStore = disputeStatusStore;
        this.fspClientService = fspClientService;
        this.auditPublisherService = auditPublisherService;
    }

    @Override
    public void afterPropertiesSet() {

        LOG.info(
            "Starting dispute status checker with initialDelay={} minute(s), period={} minute(s), retryDelay={} second(s).",
            STATUS_CHECK_INITIAL_DELAY_MINUTES, STATUS_CHECK_PERIOD_MINUTES,
            STATUS_RETRY_DELAY_SECONDS);

        this.checker.scheduleAtFixedRate(
            this::checkDisputedTransfers,
            STATUS_CHECK_INITIAL_DELAY_MINUTES,
            STATUS_CHECK_PERIOD_MINUTES,
            TimeUnit.MINUTES);
    }

    @Override
    public void destroy() {

        LOG.info("Stopping dispute status checker.");

        this.checker.shutdownNow();
    }

    private void checkDisputedTransfers() {

        LOG.info(
            "Running dispute status check. PendingCount={}, resultCount={}.",
            this.disputeStatusStore.getPendingCount(), this.disputeStatusStore.getResultCount());

        this.disputeStatusStore.getPendingTransfers().forEach(disputedTransfer -> {

            LOG.info(
                "Evaluating disputed transferId {}. age={} ms, readyForCheck={}.",
                disputedTransfer.transferId(), this.ageMillis(disputedTransfer),
                this.isReadyForStatusCheck(disputedTransfer));

            if (this.isReadyForStatusCheck(disputedTransfer)) {

                try {

                    this.checkDisputedTransfer(disputedTransfer);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void checkDisputedTransfer(DisputeStatusStore.DisputedTransaction disputedTransfer)
        throws Exception {

        LOG.info(
            "Checking disputed transferId {} with extensionList={}.",
            disputedTransfer.transferId(), disputedTransfer.extensionList());

        TransactionStatus transactionStatus = this.resolveDispute(disputedTransfer);

        if (this.isRetryableStatus(transactionStatus)) {

            LOG.info(
                "Transaction status is {} for transferId {}. Retrying after {} second(s).",
                transactionStatus, disputedTransfer.transferId(), STATUS_RETRY_DELAY_SECONDS);

            transactionStatus = this.retryDisputeStatus(disputedTransfer);
        }

        this.disputeStatusStore.removePendingTransfer(disputedTransfer.transferId());

        boolean dispute = !TransactionStatus.SUCCESS.equals(transactionStatus);

        LOG.info(
            "Dispute check finished for transferId {}. finalTransactionStatus={}, dispute={}, pendingCountBeforeStore={}, resultCountBeforeStore={}.",
            disputedTransfer.transferId(), transactionStatus, dispute,
            this.disputeStatusStore.getPendingCount(), this.disputeStatusStore.getResultCount());

        this.disputeStatusStore.saveResult(disputedTransfer.transferId(), dispute);

        if (dispute) {

            LOG.info(
                "Confirmed dispute for transferId {} because transaction status is not successful.",
                disputedTransfer.transferId());
        } else {
            LOG.info(
                "Resolved dispute for transferId {} because transaction status is successful.",
                disputedTransfer.transferId());
        }

        this.auditPublisherService.publishAudit(
            new AuditPublisherService.DisputeResultInput(disputedTransfer.transferId(), dispute));
    }

    private TransactionStatus resolveDispute(DisputeStatusStore.DisputedTransaction disputedTransfer) {

        try {

            LOG.info(
                "Calling Get Transaction Status for transferId {}.",
                disputedTransfer.transferId());

            TransactionStatus transactionStatus = this.fspClientService.getTransactionStatus(
                disputedTransfer.transferId(), disputedTransfer.extensionList());

            if (transactionStatus == null) {
                LOG.info(
                    "Get Transaction Status returned null for transferId {}. Dispute remains true.",
                    disputedTransfer.transferId());

                return TransactionStatus.FAILED;
            }

            LOG.info(
                "Get Transaction Status returned {} for transferId {}.",
                transactionStatus, disputedTransfer.transferId());

            return transactionStatus;

        } catch (Exception e) {

            LOG.error(
                "Transaction status check failed for transferId {}. Dispute remains true.",
                disputedTransfer.transferId(), e);

            return TransactionStatus.FAILED;
        }
    }

    private boolean isReadyForStatusCheck(DisputeStatusStore.DisputedTransaction disputedTransfer) {

        return System.currentTimeMillis() - disputedTransfer.markedAt() >=
                   TimeUnit.MINUTES.toMillis(1);
    }

    private boolean isRetryableStatus(TransactionStatus status) {

        return TransactionStatus.PENDING.equals(status);
    }

    private TransactionStatus retryDisputeStatus(DisputeStatusStore.DisputedTransaction disputedTransfer) {

        try {

            LOG.info(
                "Waiting {} second(s) before retrying transaction status for transferId {}.",
                STATUS_RETRY_DELAY_SECONDS, disputedTransfer.transferId());

            TimeUnit.SECONDS.sleep(STATUS_RETRY_DELAY_SECONDS);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            LOG.error(
                "Transaction status retry interrupted for transferId {}. Dispute remains true.",
                disputedTransfer.transferId(), e);

            return TransactionStatus.FAILED;
        }

        return this.resolveDispute(disputedTransfer);
    }

    private long ageMillis(DisputeStatusStore.DisputedTransaction disputedTransfer) {

        return System.currentTimeMillis() - disputedTransfer.markedAt();
    }

}
