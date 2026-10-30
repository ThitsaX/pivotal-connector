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

package com.thitsaworks.mojaloop.coreconnector.mapper.iso20022;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesPostRequest;
import org.springframework.stereotype.Component;

@Component
public class IsoQuoteCallbackTranslator {

    private final ObjectMapper objectMapper;

    public IsoQuoteCallbackTranslator(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    public ObjectNode toSuccess(QuotesPostRequest request,
                                QuotesIDPutResponse response,
                                String quoteId,
                                String payerFsp,
                                String payeeFsp) {

        if (request == null || response == null) {

            throw new IllegalArgumentException(
                "Quote request and response are required for ISO callback translation");
        }

        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode groupHeader = root.putObject("GrpHdr");
        groupHeader.put("MsgId", IsoCallbackSupport.messageId());
        groupHeader.put("CreDtTm", IsoCallbackSupport.now());
        groupHeader.put("NbOfTxs", "1");
        groupHeader.putObject("SttlmInf").put("SttlmMtd", "CLRG");
        if (response.getExpiration() != null) {

            groupHeader.put("PmtInstrXpryDtTm", response.getExpiration());
        }

        ObjectNode transaction = root.putObject("CdtTrfTxInf");
        if (IsoCallbackSupport.blankToNull(quoteId) != null) {

            transaction.putObject("PmtId").put("TxId", quoteId);
        }

        IsoCallbackSupport.putParty(transaction, "Dbtr", request.getPayer());
        IsoCallbackSupport.putAgent(transaction, "DbtrAgt", request.getPayer(), payerFsp);
        IsoCallbackSupport.putParty(transaction, "Cdtr", request.getPayee());
        IsoCallbackSupport.putAgent(transaction, "CdtrAgt", request.getPayee(), payeeFsp);
        transaction.put("ChrgBr", "SHAR");

        IsoCallbackSupport.putAmount(transaction,
                                     "IntrBkSttlmAmt",
                                     "ActiveCurrencyAndAmount",
                                     response.getTransferAmount());
        IsoCallbackSupport.putAmount(transaction,
                                     "InstdAmt",
                                     "ActiveOrHistoricCurrencyAndAmount",
                                     response.getPayeeReceiveAmount());

        if (response.getPayeeFspFee() != null) {

            ObjectNode charges = transaction.putObject("ChrgsInf");
            IsoCallbackSupport.putAmount(charges,
                                         "Amt",
                                         "ActiveOrHistoricCurrencyAndAmount",
                                         response.getPayeeFspFee());
            IsoCallbackSupport.putAgent(charges, "Agt", request.getPayee(), payeeFsp);
        }

        if (response.getIlpPacket() != null) {

            transaction.putObject("VrfctnOfTerms").put("IlpV4PrepPacket", response.getIlpPacket());
        }

        return root;
    }

    public ObjectNode toError(ErrorInformationResponse error) {

        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode groupHeader = root.putObject("GrpHdr");
        groupHeader.put("MsgId", IsoCallbackSupport.messageId());
        groupHeader.put("CreDtTm", IsoCallbackSupport.now());

        IsoCallbackSupport.putErrorReason(root.putObject("TxInfAndSts"), error);

        return root;
    }

}
