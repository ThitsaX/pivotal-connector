package com.thitsaworks.mojaloop.coreconnector.listeners.pending_transfer_store;

import com.thitsaworks.mojaloop.coreconnector.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Money;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransactionScenario;

public record IlpAgreement(String quoteId,
                           PartyIdInfo payer,
                           PartyIdInfo payee,
                           AmountType amountType,
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