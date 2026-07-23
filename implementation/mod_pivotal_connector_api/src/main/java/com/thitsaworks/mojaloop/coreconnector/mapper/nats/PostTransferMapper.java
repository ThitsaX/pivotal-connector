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
package com.thitsaworks.mojaloop.coreconnector.mapper.nats;

import com.thitsaworks.mojaloop.coreconnector.component.mojaloop.FspParty;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Money;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Party;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyComplexName;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransfersPostRequest;
import com.thitsaworks.mojaloop.coreconnector.listeners.pending_transfer_store.IlpAgreement;
import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.ReservationForTransfer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
public class PostTransferMapper {

    public ReservationForTransfer.Request transferMapper(IlpAgreement agreement, TransfersPostRequest transfersPostRequest){
        FspParty payer = toFspParty(agreement.payer());
        FspParty payee = toFspParty(agreement.payee());

        ReservationForTransfer.Request request = new ReservationForTransfer.Request();
        request.setAmount(transfersPostRequest.getAmount().getAmount());
        request.setAmountType(agreement.amountType());
        request.setCurrency(transfersPostRequest.getAmount().getCurrency());
        request.setFrom(payer);
        request.setNote(agreement.note());

        ReservationForTransfer.Quote quote = new ReservationForTransfer.Quote();
        quote.setQuoteId(agreement.quoteId());
        quote.setTransactionId(agreement.transactionId());
        if (transfersPostRequest.getAmount().getAmount() != null &&
                !transfersPostRequest.getAmount().getAmount().isBlank()) {
            quote.setPayeeReceiveAmount(new BigDecimal(agreement.payeeReceiveAmount().getAmount()));
            quote.setPayeeReceiveAmountCurrency(
                agreement.payeeReceiveAmount().getCurrency().toString());
            quote.setTransferAmount(new BigDecimal(agreement.transferAmount().getAmount()));
            quote.setTransferAmountCurrency(agreement.transferAmount().getCurrency().toString());
            setFeeAmounts(quote, agreement.payeeFspFee(), agreement.payeeFspCommission());
            quote.setExtensionList(agreement.extensionList());
        }
        request.setQuote(quote);
        request.setQuoteRequestExtensions(transfersPostRequest.getExtensionList());
        request.setSubScenario(agreement.subScenario());
        request.setTo(payee);
        request.setTransactionType(
            agreement.scenario() == null ? null : agreement.scenario().toString());
        request.setTransferId(transfersPostRequest.getTransferId());
        return  request;

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
        fspParty.setFspId(info.getFspId());
        fspParty.setDisplayName(party.getName());
        fspParty.setMerchantClassificationCode(party.getMerchantClassificationCode());
        fspParty.setExtensionList(info.getExtensionList());

        if (party.getPersonalInfo() != null) {
            String dateOfBirth = party.getPersonalInfo().getDateOfBirth();
            fspParty.setDateOfBirth(
                dateOfBirth == null || dateOfBirth.isBlank() ? null : dateOfBirth);

            PartyComplexName complexName = party.getPersonalInfo().getComplexName();
            if (complexName != null) {
                fspParty.setFirstName(blankToEmpty(complexName.getFirstName()));
                fspParty.setMiddleName(blankToEmpty(complexName.getMiddleName()));
                fspParty.setLastName(blankToEmpty(complexName.getLastName()));
            }
        }

        return fspParty;
    }

    private void setFeeAmounts(ReservationForTransfer.Quote quote,
                               Money payeeFspFee,
                               Money payeeFspCommission) {
        if (payeeFspFee != null) {
            quote.setPayeeFspFeeAmount(new BigDecimal(payeeFspFee.getAmount()));
            quote.setPayeeFspFeeAmountCurrency(payeeFspFee.getCurrency().toString());
        }

        if (payeeFspCommission != null) {
            quote.setPayeeFspCommissionAmount(new BigDecimal(payeeFspCommission.getAmount()));
            quote.setPayeeFspCommissionAmountCurrency(
                payeeFspCommission.getCurrency().toString());
        }
    }

    private String blankToEmpty(String value) {
        return value == null || value.isBlank() ? "" : value;
    }
}
