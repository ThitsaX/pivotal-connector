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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DisputeStatusStore {

    private static final Logger LOG = LoggerFactory.getLogger(DisputeStatusStore.class);

    private final Map<String, DisputedTransaction> disputedTransactions = new ConcurrentHashMap<>();

    public DisputeStatusStore() { }

    public void storeDisputedTransaction(String transactionId, String homeTransactionId) {

        if (!StringUtils.hasLength(transactionId)) {
            LOG.info("TransactionId of disputed transaction is blank.");
            return;
        }

        Instant disputedDateTime = Instant.now();

        DisputedTransaction disputedTransaction = new DisputedTransaction(
            transactionId,
            homeTransactionId, disputedDateTime);

        DisputedTransaction existingDisputedTransaction = this.disputedTransactions.putIfAbsent(
            transactionId, disputedTransaction);

        LOG.info(
            "Disputed transaction store for transactionId {}. Current disputed transaction count={}.",
            transactionId, this.disputedTransactions.size());

        if (existingDisputedTransaction == null) {

            LOG.info(
                "Disputed transaction stored for transactionId {} at disputedDateTime {}.",
                transactionId, disputedTransaction.disputedDateTime());

        } else {

            LOG.info(
                "Disputed transaction already exists for transactionId {}. Current disputed transaction count={}.",
                transactionId, this.disputedTransactions.size());
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
