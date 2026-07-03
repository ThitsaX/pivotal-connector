/*
 * Copyright (c) 2026 ThitsaWorks
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
package com.thitsaworks.mojaloop.coreconnector.mapper.nats;

import com.thitsaworks.mojaloop.coreconnector.component.mojaloop.FspParty;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Currency;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Extension;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Money;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Party;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyComplexName;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesIDPutResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesPostRequest;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransactionScenario;
import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.DoQuote;
import org.springframework.stereotype.Component;

@Component
public class PostQuoteMapper {

    public DoQuote.Request toPostQuoteRequest(QuotesPostRequest request) {

        Currency currency = request.getAmount().getCurrency();

        DoQuote.Request quoteRequestToFSP = new DoQuote.Request();
        quoteRequestToFSP.setPayer(toFspParty(request.getPayer()));
        quoteRequestToFSP.setPayee(toFspParty(request.getPayee()));
        quoteRequestToFSP.setAmountType(request.getAmountType());
        quoteRequestToFSP.setCurrency(currency);
        quoteRequestToFSP.setAmount(request.getAmount().getAmount());
        quoteRequestToFSP.setFeesAmount(
            request.getFees() != null ? request.getFees().getAmount() : "0");
        quoteRequestToFSP.setFeesCurrency(
            request.getFees() != null ? request.getFees().getCurrency() : currency);
        quoteRequestToFSP.setExpiration(request.getExpiration());
        quoteRequestToFSP.setExtensionList(request.getExtensionList());
        quoteRequestToFSP.setGeoCode(request.getGeoCode());
        quoteRequestToFSP.setNote(request.getNote());
        quoteRequestToFSP.setQuotedId(request.getQuoteId());
        quoteRequestToFSP.setTransactionId(request.getTransactionId());
        quoteRequestToFSP.setTransactionRequestId(request.getTransactionRequestId());

        quoteRequestToFSP.setTransactionType(request.getTransactionType().getScenario());
        quoteRequestToFSP.setSubScenario(request.getTransactionType().getSubScenario());

        return quoteRequestToFSP;
    }

    public QuotesIDPutResponse toPutQuoteResponse(QuotesPostRequest request,
                                                  DoQuote.Response quote,
                                                  String expiration,
                                                  String ilpPacket,
                                                  String condition) {

        Currency currency = request.getAmount().getCurrency();

        Money transferAmount = money(
            resolve(quote.getTransferAmount(), request.getAmount().getAmount()), currency);
        ExtensionList extensionList =quote.getExtensionList();
        Money payeeReceiveAmount = money(
            resolve(quote.getPayeeReceiveAmount(), request.getAmount().getAmount()),
            resolve(quote.getPayeeReceiveAmountCurrency(), currency));
        Money payeeFspFee = money(
            resolve(quote.getPayeeFspFeeAmount(), "0"),
            resolve(quote.getPayeeFspFeeAmountCurrency(), currency));
        Money payeeFspCommission = money(
            resolve(quote.getPayeeFspCommissionAmount(), "0"),
            resolve(quote.getPayeeFspCommissionAmountCurrency(), currency));

        return new QuotesIDPutResponse()
                   .transferAmount(transferAmount)
                   .payeeReceiveAmount(payeeReceiveAmount)
                   .payeeFspFee(payeeFspFee)
                   .payeeFspCommission(payeeFspCommission)
                   .expiration(expiration)
                   .geoCode(request.getGeoCode())
                   .ilpPacket(ilpPacket)
                   .condition(condition)
                   .extensionList(extensionList);
    }

    public IlpAgreement toIlpAgreement(QuotesPostRequest request,
                                       DoQuote.Response quote,
                                       long expireAtMs) {

        Currency currency = request.getAmount().getCurrency();

        Money transferAmount = money(
            resolve(quote.getTransferAmount(), request.getAmount().getAmount()), currency);
        Money payeeReceiveAmount = money(
            resolve(quote.getPayeeReceiveAmount(), request.getAmount().getAmount()),
            resolve(quote.getPayeeReceiveAmountCurrency(), currency));
        Money payeeFspFee = money(
            resolve(quote.getPayeeFspFeeAmount(), "0"),
            resolve(quote.getPayeeFspFeeAmountCurrency(), currency));
        Money payeeFspCommission = money(
            resolve(quote.getPayeeFspCommissionAmount(), "0"),
            resolve(quote.getPayeeFspCommissionAmountCurrency(), currency));

        TransactionScenario scenario =
            request.getTransactionType() != null ? request.getTransactionType().getScenario() :
                null;

        String subScenario =
            request.getTransactionType() != null ? request.getTransactionType().getSubScenario() :
                null;

        ExtensionList extensionList = quote.getExtensionList();

        return new IlpAgreement(
            request.getQuoteId(), request.getPayer().getPartyIdInfo(),
            request.getPayee().getPartyIdInfo(), request.getAmountType(), scenario, subScenario,
            request.getAmount(), payeeFspFee, payeeFspCommission, payeeReceiveAmount,
            transferAmount, extensionList, expireAtMs,request.getNote());
    }

    private FspParty toFspParty(Party party) {

        if (party == null || party.getPartyIdInfo() == null) {
            return null;
        }

        PartyIdInfo info = party.getPartyIdInfo();

        FspParty fspParty = new FspParty();
        fspParty.setIdType(info.getPartyIdType());
        fspParty.setIdValue(info.getPartyIdentifier());
        fspParty.setIdSubValue(
            info.getPartySubIdOrType() == null ? "" : info.getPartySubIdOrType());
        fspParty.setFspId(info.getFspId() == null ? "" : info.getFspId());
        fspParty.setDisplayName(party.getName());
        fspParty.setMerchantClassificationCode(party.getMerchantClassificationCode());

        PartyComplexName complexName = party.getPersonalInfo() != null
                                           ? party.getPersonalInfo().getComplexName()
                                           : null;
        fspParty.setFirstName(complexName == null ? "" : blankToEmpty(complexName.getFirstName()));
        fspParty.setMiddleName(complexName == null ? "" : blankToEmpty(complexName.getMiddleName()));
        fspParty.setLastName(complexName == null ? "" : blankToEmpty(complexName.getLastName()));

        if (info.getExtensionList() != null) {
            fspParty.setExtensionList(info.getExtensionList());
        }

        return fspParty;
    }

    private Money money(String amount, Currency currency) {

        return new Money().currency(currency).amount(amount);
    }

    private String resolve(String value, String fallback) {

        return value == null || value.isBlank() ? fallback : value;
    }

    private Currency resolve(Currency value, Currency fallback) {

        return value == null ? fallback : value;
    }

    private String blankToEmpty(String value) {
        return value == null || value.isBlank() ? "" : value;
    }

    public record IlpAgreement(String quoteId,
                               PartyIdInfo payer,
                               PartyIdInfo payee,
                               Object amountType,
                               TransactionScenario scenario,
                               String subScenario,
                               Money originalAmount,
                               Money payeeFspFee,
                               Money payeeFspCommission,
                               Money payeeReceiveAmount,
                               Money transferAmount,
                               ExtensionList extensionList,
                               long expireAt,
                               String note) { }

}