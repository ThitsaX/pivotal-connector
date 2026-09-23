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

import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.DisputeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DisputeStatusStore {

    private static final Logger LOG = LoggerFactory.getLogger(DisputeStatusStore.class);

    private static final long STATUS_CHECK_PERIOD_MINUTES = 1L;

    private final Map<String, DisputedTransaction> disputedTransactions = new ConcurrentHashMap<>();

    private final Map<String, DisputeResult.Response> disputeResults = new ConcurrentHashMap<>();

    public void storeDisputedTransaction(String transferId, ExtensionList extensionList) {

        if (!StringUtils.hasLength(transferId)) {
            LOG.info("Ignoring dispute store request because transferId is blank.");
            return;
        }

        DisputedTransaction disputedTransaction = new DisputedTransaction(
            transferId,
            extensionList,
            System.currentTimeMillis());

        DisputedTransaction existingDisputedTransaction = this.disputedTransactions.putIfAbsent(
            transferId,
            disputedTransaction);

        LOG.info(
            "Dispute store requested for transferId {} with extensionList={}. Current disputed transaction count={}.",
            transferId, extensionList, this.disputedTransactions.size());

        if (existingDisputedTransaction == null) {

            LOG.info(
                "Stored transferId {} as a disputed transaction at {}. It will be checked every {} minute(s).",
                transferId, disputedTransaction.disputedAt(), STATUS_CHECK_PERIOD_MINUTES);

        } else {

            LOG.info(
                "Dispute already exists for transferId {}. It was first stored at {}. Current disputed transaction count={}.",
                transferId, existingDisputedTransaction.disputedAt(), this.disputedTransactions.size());
        }
    }

    public Collection<DisputedTransaction> getPendingDisputedTransactions() {

        return this.disputedTransactions.values();
    }

    public void removeDisputedTransaction(String transferId) {

        this.disputedTransactions.remove(transferId);
    }

    public void saveDisputeResult(String transferId, boolean dispute) {

        this.disputeResults.put(transferId, new DisputeResult.Response(dispute));
    }

    public int getPendingDisputedTransactionCount() {

        return this.disputedTransactions.size();
    }

    public int getDisputeResultCount() {

        return this.disputeResults.size();
    }

    public record DisputedTransaction(String transferId,
                                      ExtensionList extensionList,
                                      long disputedAt) { }

}
