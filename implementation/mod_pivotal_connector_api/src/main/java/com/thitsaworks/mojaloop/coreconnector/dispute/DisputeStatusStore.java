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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DisputeStatusStore {

    private static final Logger LOG = LoggerFactory.getLogger(DisputeStatusStore.class);

    private final Map<String, DisputedTransaction> disputedTransactions = new ConcurrentHashMap<>();

    private final Duration schedulerInterval;

    public DisputeStatusStore(CoreConnectorConfiguration.Settings settings) {

        this.schedulerInterval = Duration.ofMinutes(settings.getDisputeSchedulerIntervalMinutes());
    }

    public void storeDisputedTransaction(String transactionId, String homeTransactionId) {

        if (!StringUtils.hasLength(transactionId)) {
            LOG.info("Ignoring dispute store request because transactionId is blank.");
            return;
        }

        Instant disputedDateTime = Instant.now();

        DisputedTransaction disputedTransaction = new DisputedTransaction(
            transactionId,
            homeTransactionId, disputedDateTime);

        DisputedTransaction existingDisputedTransaction = this.disputedTransactions.putIfAbsent(
            transactionId, disputedTransaction);

        LOG.info(
            "Dispute store requested for transactionId {}. Current disputed transaction count={}.",
            transactionId, this.disputedTransactions.size());

        if (existingDisputedTransaction == null) {

            LOG.info(
                "Stored transactionId {} as a disputed transaction at {}. It will be checked every {} minute(s).",
                transactionId, disputedTransaction.disputedDateTime(),
                this.schedulerInterval.toMinutes());

        } else {

            LOG.info(
                "Dispute already exists for transactionId {}. It was first stored at {}. Current disputed transaction count={}.",
                transactionId, existingDisputedTransaction.disputedDateTime(),
                this.disputedTransactions.size());
        }
    }

    public Collection<DisputedTransaction> getPendingDisputedTransactions() {

        return this.disputedTransactions.values();
    }

    public void removeDisputedTransaction(String transactionId) {

        this.disputedTransactions.remove(transactionId);
    }

    public int getPendingDisputedTransactionCount() {

        return this.disputedTransactions.size();
    }

}
