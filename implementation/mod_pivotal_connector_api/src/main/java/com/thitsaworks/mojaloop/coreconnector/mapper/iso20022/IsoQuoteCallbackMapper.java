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
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Money;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Party;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesPostRequest;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;

/** Maps the existing FSPIOP quote flow result to Mojaloop's ISO 20022 quote callback JSON. */
@Component
public class IsoQuoteCallbackMapper {

    private static final char[] CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ObjectMapper objectMapper;

    public IsoQuoteCallbackMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Creates the pacs.082.001.01 payload for PUT /quotes/{ID}. */
    public ObjectNode toSuccess(QuotesPostRequest request,
                                QuotesIDPutResponse response,
                                String quoteId,
                                String payerFsp,
                                String payeeFsp) {

        if (request == null || response == null) {
            throw new IllegalArgumentException("Quote request and response are required for ISO callback mapping");
        }

        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode groupHeader = root.putObject("GrpHdr");
        groupHeader.put("MsgId", newUlid());
        groupHeader.put("CreDtTm", Instant.now().toString());
        groupHeader.put("NbOfTxs", "1");
        groupHeader.putObject("SttlmInf").put("SttlmMtd", "CLRG");
        groupHeader.put("PmtInstrXpryDtTm", response.getExpiration());

        ObjectNode transaction = root.putObject("CdtTrfTxInf");
        ObjectNode paymentId = transaction.putObject("PmtId");
        paymentId.put("TxId", quoteId);
        if (request.getTransactionId() != null) {
            paymentId.put("EndToEndId", request.getTransactionId());
        }
        if (request.getTransactionRequestId() != null) {
            paymentId.put("InstrId", request.getTransactionRequestId());
        }

        addParty(transaction, "Dbtr", request.getPayer());
        addAgent(transaction, "DbtrAgt", payerFsp);
        addParty(transaction, "Cdtr", request.getPayee());
        addAgent(transaction, "CdtrAgt", payeeFsp);

        // Mojaloop's quote market practice uses CRED for the payee-side charge bearer.
        transaction.put("ChrgBr", "CRED");
        addAmount(transaction, "IntrBkSttlmAmt", "ActiveCurrencyAndAmount", response.getTransferAmount());
        addAmount(transaction, "InstdAmt", "ActiveOrHistoricCurrencyAndAmount", response.getPayeeReceiveAmount());

        if (response.getPayeeFspFee() != null) {
            ObjectNode charge = transaction.putObject("ChrgsInf");
            addAmount(charge, "Amt", "ActiveOrHistoricCurrencyAndAmount", response.getPayeeFspFee());
            addAgent(charge, "Agt", payeeFsp);
        }

        ObjectNode verification = transaction.putObject("VrfctnOfTerms");
        verification.put("IlpV4PrepPacket", response.getIlpPacket());
        return root;
    }

    /** Creates the pacs.002.001.15 payload for PUT /quotes/{ID}/error. */
    public ObjectNode toError(ErrorInformationResponse error) {

        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode groupHeader = root.putObject("GrpHdr");
        groupHeader.put("MsgId", newUlid());
        groupHeader.put("CreDtTm", Instant.now().toString());

        String code = "GENERIC_DOWNSTREAM_ERROR_PAYEE";
        if (error != null && error.getErrorInformation() != null &&
            error.getErrorInformation().getErrorCode() != null) {
            code = error.getErrorInformation().getErrorCode();
        }
        root.putObject("TxInfAndSts")
            .putObject("StsRsnInf")
            .putObject("Rsn")
            .put("Prtry", code);
        return root;
    }

    private void addParty(ObjectNode parent, String elementName, Party party) {

        if (party == null || party.getPartyIdInfo() == null) {
            throw new IllegalArgumentException("Quote party identity is required for ISO callback mapping: " + elementName);
        }
        var info = party.getPartyIdInfo();
        if (info.getPartyIdType() == null || info.getPartyIdentifier() == null) {
            throw new IllegalArgumentException("Quote party type and identifier are required for ISO callback mapping");
        }

        ObjectNode isoParty = parent.putObject(elementName);
        ObjectNode id = isoParty.putObject("Id");
        PartyIdType idType = info.getPartyIdType();
        ObjectNode other;
        if (idType == PartyIdType.BUSINESS || idType == PartyIdType.DEVICE) {
            other = id.putObject("OrgId").putObject("Othr");
        } else {
            other = id.putObject("PrvtId").putObject("Othr");
        }
        other.put("Id", info.getPartyIdentifier());
        other.putObject("SchmeNm").put("Prtry", idType.toString());
        if (party.getName() != null && !party.getName().isBlank()) {
            isoParty.put("Name", party.getName());
        }
    }

    private void addAgent(ObjectNode parent, String elementName, String fspId) {

        if (fspId == null || fspId.isBlank()) {
            throw new IllegalArgumentException("FSP identifier is required for ISO quote callback: " + elementName);
        }
        parent.putObject(elementName)
              .putObject("FinInstnId")
              .putObject("Othr")
              .put("Id", fspId);
    }

    private void addAmount(ObjectNode parent, String elementName, String valueElement, Money amount) {

        if (amount == null || amount.getAmount() == null || amount.getCurrency() == null) {
            throw new IllegalArgumentException("Quote amount is incomplete for ISO callback mapping: " + elementName);
        }
        ObjectNode isoAmount = parent.putObject(elementName);
        isoAmount.put("Ccy", amount.getCurrency().toString());
        isoAmount.put(valueElement, amount.getAmount());
    }

    private static String newUlid() {

        byte[] bytes = new byte[16];
        long timestamp = System.currentTimeMillis();
        for (int i = 5; i >= 0; i--) {
            bytes[i] = (byte) timestamp;
            timestamp >>>= 8;
        }
        byte[] randomness = new byte[10];
        RANDOM.nextBytes(randomness);
        System.arraycopy(randomness, 0, bytes, 6, randomness.length);

        BigInteger value = new BigInteger(1, bytes);
        char[] encoded = new char[26];
        BigInteger base = BigInteger.valueOf(32);
        for (int i = encoded.length - 1; i >= 0; i--) {
            BigInteger[] quotientAndRemainder = value.divideAndRemainder(base);
            encoded[i] = CROCKFORD_BASE32[quotientAndRemainder[1].intValue()];
            value = quotientAndRemainder[0];
        }
        return new String(encoded);
    }
}
