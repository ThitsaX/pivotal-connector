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
import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.DisputedStatus;
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

    private final Map<String, DisputedTransaction> disputedTransaction = new ConcurrentHashMap<>();

    private final Map<String, DisputedStatus.Response> disputeResults = new ConcurrentHashMap<>();

    public void storeDisputedTransfer(String transferId, ExtensionList extensionList) {

        if (!StringUtils.hasLength(transferId)) {
            LOG.info("Ignoring dispute mark because transferId is blank.");
            return;
        }

        LOG.info(
            "Mark dispute requested for transferId {} with extensionList={}. Current pendingCount={}.",
            transferId, extensionList, this.disputedTransaction.size());

        DisputedTransaction existing = this.disputedTransaction.putIfAbsent(
            transferId,
            new DisputedTransaction(transferId, extensionList, System.currentTimeMillis()));

        if (existing == null) {
            LOG.info(
                "Marked transferId {} as dispute. It will be checked every {} minute(s).",
                transferId, STATUS_CHECK_PERIOD_MINUTES);
        } else {
            LOG.info(
                "Dispute already marked for transferId {}. Existing age={} ms. PendingCount={}.",
                transferId, this.ageMillis(existing), this.disputedTransaction.size());
        }
    }

    public Collection<DisputedTransaction> getPendingTransfers() {

        return this.disputedTransaction.values();
    }

    public void removePendingTransfer(String transferId) {

        this.disputedTransaction.remove(transferId);
    }

    public void saveResult(String transferId, boolean dispute) {

        this.disputeResults.put(transferId, new DisputedStatus.Response(dispute));
    }

    public int getPendingCount() {

        return this.disputedTransaction.size();
    }

    public int getResultCount() {

        return this.disputeResults.size();
    }

    private long ageMillis(DisputedTransaction disputedTransfer) {

        return System.currentTimeMillis() - disputedTransfer.markedAt();
    }

    public record DisputedTransaction(String transferId,
                                      ExtensionList extensionList,
                                      long markedAt) { }

}
