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
package com.thitsaworks.mojaloop.coreconnector.tazama.payload;

import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransfersIDPatchResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransfersPostRequest;
import com.thitsaworks.mojaloop.coreconnector.listeners.pending_transfer_store.IlpAgreement;
import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.DoQuote;
import com.thitsaworks.mojaloop.coreconnector.payload.nats.PatchTransfersNatsMessage;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TazamaPayloadBuilder {

    public Map<String, Object> buildQuoteResponsePayload(DoQuote.Request request, DoQuote.Response response) {

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("quoteId", response.getQuoteId());
        payload.put("transactionId", response.getTransactionId());
        payload.put("transactionRequestId", request.getTransactionRequestId());
        payload.put("amountType", request.getAmountType());
        payload.put("transactionType", request.getTransactionType());
        payload.put("subScenario", request.getSubScenario());
        payload.put("initiator", "PAYER");
        payload.put("initiatorType", request.getPayer() != null ? request.getPayer().getType() : null);
        payload.put("note", request.getNote());

        payload.put("from", request.getPayer());
        payload.put("payer", request.getPayer());
        payload.put("to", request.getPayee());
        payload.put("payee", request.getPayee());

        payload.put("transferAmount",
                    response.getTransferAmount() != null ? response.getTransferAmount() : request.getAmount());
        payload.put("transferAmountCurrency",
                    response.getTransferAmountCurrency() != null ? response.getTransferAmountCurrency() :
                        request.getCurrency());
        payload.put("payeeReceiveAmount",
                    response.getPayeeReceiveAmount() != null ? response.getPayeeReceiveAmount() :
                        response.getTransferAmount());
        payload.put("payeeReceiveAmountCurrency",
                    response.getPayeeReceiveAmountCurrency() != null ? response.getPayeeReceiveAmountCurrency() :
                        response.getTransferAmountCurrency());
        payload.put("payeeFspFeeAmount",
                    response.getPayeeFspFeeAmount() != null ? response.getPayeeFspFeeAmount() :
                        request.getFeesAmount());
        payload.put("payeeFspFeeAmountCurrency",
                    response.getPayeeFspFeeAmountCurrency() != null ? response.getPayeeFspFeeAmountCurrency() :
                        request.getFeesCurrency());
        payload.put("payeeFspCommissionAmount", response.getPayeeFspCommissionAmount());
        payload.put("payeeFspCommissionAmountCurrency", response.getPayeeFspCommissionAmountCurrency());
        payload.put("expiration",
                    response.getExpiration() != null ? response.getExpiration() : request.getExpiration());
        payload.put("geoCode", response.getGeoCode() != null ? response.getGeoCode() : request.getGeoCode());
        payload.put("extensionList",
                    response.getExtensionList() != null ? response.getExtensionList() : request.getExtensionList());
        payload.put("supportedCurrencies", response.getSupportedCurrencies());

        return payload;
    }

    public Map<String, Object> transferRequestPayload(TransfersPostRequest request, IlpAgreement agreement) {

        Map<String, Object> from = new LinkedHashMap<>();
        from.put("fspId", request.getPayerFsp());

        Map<String, Object> to = new LinkedHashMap<>();
        to.put("fspId", request.getPayeeFsp());

        Map<String, Object> quote = new LinkedHashMap<>();
        quote.put("quoteId", agreement.quoteId());
        quote.put("transactionId", agreement.transactionId());
        quote.put("expiration", request.getExpiration());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("transferId", request.getTransferId());
        payload.put("transactionId", agreement.transactionId());
        payload.put("quoteId", agreement.quoteId());
        payload.put("quote", quote);

        // The current PPA mapper reads the FSP IDs from payload.from.fspId and payload.to.fspId.
        payload.put("from", from);
        payload.put("to", to);

        payload.put("amount", request.getAmount().getAmount());
        payload.put("currency", request.getAmount().getCurrency());
        payload.put("ilpPacket", request.getIlpPacket());
        payload.put("condition", request.getCondition());
        payload.put("expiration", request.getExpiration());

        payload.put("payerFsp", request.getPayerFsp());
        payload.put("payeeFsp", request.getPayeeFsp());
        payload.put("payer", agreement.payer());
        payload.put("payee", agreement.payee());
        payload.put("amountType", agreement.amountType());
        payload.put("transactionType", agreement.scenario());
        payload.put("subScenario", agreement.subScenario());
        payload.put("payeeReceiveAmount", agreement.payeeReceiveAmount());
        payload.put("note", agreement.note());
        payload.put("extensionList", request.getExtensionList());

        return payload;
    }

    public Map<String, Object> transferResponsePayload(PatchTransfersNatsMessage msg, String homeTransactionId) {

        TransfersIDPatchResponse patchResponse = msg.getResponse();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("transferId", msg.getTransferId());
        payload.put("payerFsp", msg.getPayerFsp());
        payload.put("payeeFsp", msg.getPayeeFsp());
        payload.put("completedTimestamp", patchResponse.getCompletedTimestamp());
        payload.put("homeTransactionId", homeTransactionId);
        payload.put("transferState", patchResponse.getTransferState());
        payload.put("extensionList", patchResponse.getExtensionList());
        return payload;
    }
}
