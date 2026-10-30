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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Currency;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Money;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartiesTypeIDPutResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Party;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyComplexName;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyPersonalInfo;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesPostRequest;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransferState;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransfersIDPutResponse;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class IsoCallbackTranslatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final IsoPartiesCallbackTranslator parties = new IsoPartiesCallbackTranslator(
        objectMapper);

    private final IsoQuoteCallbackTranslator quotes = new IsoQuoteCallbackTranslator(objectMapper);

    private final IsoTransfersCallbackTranslator transfers = new IsoTransfersCallbackTranslator(
        objectMapper);

    @Test
    public void partiesSuccessFollowsDiscoveryReversePut() {

        PartiesTypeIDPutResponse response = new PartiesTypeIDPutResponse().party(
            person("24555500001", "Ada", "Lovelace", "payeefsp"));

        JsonNode root = parties.toSuccess(response, "payeefsp", "payerfsp");

        assertTrue(root.at("/Assgnmt/MsgId").asText().matches("[0-9A-HJKMNP-TV-Z]{26}"));
        assertEquals("payeefsp", root.at("/Assgnmt/Assgnr/Agt/FinInstnId/Othr/Id").asText());
        assertEquals("payerfsp", root.at("/Assgnmt/Assgne/Agt/FinInstnId/Othr/Id").asText());
        assertTrue(root.at("/Rpt/Vrfctn").asBoolean());
        assertEquals(
            "MSISDN", root.at("/Rpt/UpdtdPtyAndAcctId/Pty/Id/PrvtId/Othr/SchmeNm/Prtry").asText());
        assertEquals(
            "24555500001", root.at("/Rpt/UpdtdPtyAndAcctId/Pty/Id/PrvtId/Othr/Id").asText());
        assertTrue(root.at("/Rpt/UpdtdPtyAndAcctId/Pty/Id/OrgId").isMissingNode());
        assertEquals(
            "Ada;;Lovelace;Ada Lovelace", root.at("/Rpt/UpdtdPtyAndAcctId/Pty/Nm").asText());
        assertEquals("payeefsp", root.at("/Rpt/UpdtdPtyAndAcctId/Agt/FinInstnId/Othr/Id").asText());
        assertEquals("USD", root.at("/Rpt/UpdtdPtyAndAcctId/Acct/Ccy").asText());
    }

    @Test
    public void partiesErrorFollowsDiscoveryReversePutError() {

        JsonNode root = parties.toError(error("3204", "Party not found"), "payeefsp", "payerfsp");

        assertFalse(root.at("/Rpt/Vrfctn").asBoolean());
        assertEquals("3204", root.at("/Rpt/Rsn/Cd").asText());
        assertEquals("payeefsp", root.at("/Assgnmt/Assgnr/Agt/FinInstnId/Othr/Id").asText());
    }

    @Test
    public void quoteSuccessFollowsQuotesReversePut() {

        QuotesPostRequest request = new QuotesPostRequest()
                                        .payer(person("24555500001", "Ada", "Lovelace", "payerfsp"))
                                        .payee(business("merchant-1", "Market"));
        QuotesIDPutResponse response = new QuotesIDPutResponse()
                                           .expiration("2026-10-29T04:00:00.000Z")
                                           .transferAmount(money("10.00"))
                                           .payeeReceiveAmount(money("9.50"))
                                           .payeeFspFee(money("0.50"))
                                           .ilpPacket("prepare-packet");

        JsonNode root = quotes.toSuccess(request, response, "quote-1", "payerfsp", "payeefsp");

        assertEquals("1", root.at("/GrpHdr/NbOfTxs").asText());
        assertEquals("CLRG", root.at("/GrpHdr/SttlmInf/SttlmMtd").asText());
        assertEquals("2026-10-29T04:00:00.000Z", root.at("/GrpHdr/PmtInstrXpryDtTm").asText());
        assertEquals("quote-1", root.at("/CdtTrfTxInf/PmtId/TxId").asText());
        assertEquals("24555500001", root.at("/CdtTrfTxInf/Dbtr/Id/PrvtId/Othr/Id").asText());
        assertEquals("payerfsp", root.at("/CdtTrfTxInf/DbtrAgt/FinInstnId/Othr/Id").asText());
        assertEquals("merchant-1", root.at("/CdtTrfTxInf/Cdtr/Id/OrgId/Othr/Id").asText());
        assertEquals("BUSINESS", root.at("/CdtTrfTxInf/Cdtr/Id/OrgId/Othr/SchmeNm/Prtry").asText());
        assertEquals(";;;Market", root.at("/CdtTrfTxInf/Cdtr/Nm").asText());
        assertEquals("payeefsp", root.at("/CdtTrfTxInf/CdtrAgt/FinInstnId/Othr/Id").asText());
        assertEquals("SHAR", root.at("/CdtTrfTxInf/ChrgBr").asText());
        assertEquals("USD", root.at("/CdtTrfTxInf/IntrBkSttlmAmt/Ccy").asText());
        assertEquals(
            "10.00", root.at("/CdtTrfTxInf/IntrBkSttlmAmt/ActiveCurrencyAndAmount").asText());
        assertEquals(
            "9.50", root.at("/CdtTrfTxInf/InstdAmt/ActiveOrHistoricCurrencyAndAmount").asText());
        assertEquals(
            "0.50",
            root.at("/CdtTrfTxInf/ChrgsInf/Amt/ActiveOrHistoricCurrencyAndAmount").asText());
        assertEquals("payeefsp", root.at("/CdtTrfTxInf/ChrgsInf/Agt/FinInstnId/Othr/Id").asText());
        assertEquals(
            "prepare-packet", root.at("/CdtTrfTxInf/VrfctnOfTerms/IlpV4PrepPacket").asText());
    }

    @Test
    public void quoteSuccessRequiresTheOriginalQuoteRequest() {

        assertThrows(
            IllegalArgumentException.class,
            () -> quotes.toSuccess(
                null, new QuotesIDPutResponse(), "quote-1", "payerfsp",
                "payeefsp"));
    }

    @Test
    public void quoteErrorFollowsQuotesReversePutError() {

        JsonNode root = quotes.toError(error("5100", "x".repeat(120)));

        assertEquals("5100", root.at("/TxInfAndSts/StsRsnInf/Rsn/Prtry").asText());
        assertEquals(105, root.at("/TxInfAndSts/StsRsnInf/AddtlInf").asText().length());
    }

    @Test
    public void transferSuccessFollowsTransfersReversePut() {

        TransfersIDPutResponse response = new TransfersIDPutResponse()
                                              .fulfilment("fulfilment")
                                              .completedTimestamp("2026-10-29T04:01:00.000Z")
                                              .transferState(TransferState.RESERVED);

        JsonNode root = transfers.toSuccess(response);

        assertEquals("fulfilment", root.at("/TxInfAndSts/ExctnConf").asText());
        assertEquals("2026-10-29T04:01:00.000Z", root.at("/TxInfAndSts/PrcgDt/DtTm").asText());
        assertEquals("RESV", root.at("/TxInfAndSts/TxSts").asText());
    }

    @Test
    public void transferErrorFollowsTransfersReversePutError() {

        JsonNode root = transfers.toError(error("4001", "Payer FSP insufficient liquidity"));

        assertEquals("4001", root.at("/TxInfAndSts/StsRsnInf/Rsn/Prtry").asText());
        assertEquals(
            "Payer FSP insufficient liquidity",
            root.at("/TxInfAndSts/StsRsnInf/AddtlInf").asText());
    }

    private static Party person(String identifier, String firstName, String lastName,
                                String fspId) {

        return new Party()
                   .partyIdInfo(new PartyIdInfo()
                                    .partyIdType(PartyIdType.MSISDN)
                                    .partyIdentifier(identifier)
                                    .fspId(fspId))
                   .name(firstName + " " + lastName)
                   .personalInfo(new PartyPersonalInfo().complexName(
                       new PartyComplexName().firstName(firstName).lastName(lastName)))
                   .supportedCurrencies(List.of(Currency.USD));
    }

    private static Party business(String identifier, String name) {

        return new Party()
                   .partyIdInfo(new PartyIdInfo()
                                    .partyIdType(PartyIdType.BUSINESS)
                                    .partyIdentifier(identifier)
                                    .fspId("payeefsp"))
                   .name(name);
    }

    private static Money money(String amount) {

        return new Money().amount(amount).currency(Currency.USD);
    }

    private static ErrorInformationResponse error(String code, String description) {

        return new ErrorInformationResponse().errorInformation(
            new ErrorInformation().errorCode(code).errorDescription(description));
    }

}
