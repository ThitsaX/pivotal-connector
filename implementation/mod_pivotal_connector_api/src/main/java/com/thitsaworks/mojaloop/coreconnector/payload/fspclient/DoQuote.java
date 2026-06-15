package com.thitsaworks.mojaloop.coreconnector.payload.fspclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.coreconnector.component.mojaloop.ErrorInformationResponse;
import com.thitsaworks.mojaloop.coreconnector.component.mojaloop.FspParty;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Currency;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.GeoCode;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransactionScenario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class DoQuote {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Request implements Serializable {

        @JsonProperty("homeR2PTransactionId")
        private String homeR2PTransactionId;

        @NotNull
        @JsonProperty("amount")
        private String amount;

        @NotNull
        @JsonProperty("amountType")
        private AmountType amountType;

        @NotNull
        @JsonProperty("currency")
        private Currency currency;

        @JsonProperty("expiration")
        private String expiration;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

        @NotNull
        @JsonProperty("feesAmount")
        private String feesAmount;

        @NotNull
        @JsonProperty("feesCurrency")
        private Currency feesCurrency;

        @NotNull
        @JsonProperty("from")
        private FspParty payer;

        @NotNull
        @JsonProperty("geoCode")
        private GeoCode geoCode;

        @NotNull
        @JsonProperty("to")
        private FspParty payee;

        @JsonProperty("note")
        private String note;

        @NotNull
        @JsonProperty("quotedId")
        private String quotedId;

        @NotNull
        @JsonProperty("subScenario")
        private String subScenario;

        @NotNull
        @JsonProperty("transactionId")
        private String transactionId;

        @NotNull
        @JsonProperty("transactionType")
        private TransactionScenario transactionType;

        @NotNull
        @JsonProperty("transactionRequestId")
        private String transactionRequestId;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Response implements Serializable {

        @JsonProperty("expiration")
        private String expiration;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

        @JsonProperty("geoCode")
        private GeoCode geoCode;

        @JsonProperty("payeeFspCommissionAmount")
        private String payeeFspCommissionAmount;

        @JsonProperty("payeeFspCommissionAmountCurrency")
        private Currency payeeFspCommissionAmountCurrency;

        @JsonProperty("payeeFspFeeAmount")
        private String payeeFspFeeAmount;

        @JsonProperty("payeeFspFeeAmountCurrency")
        private Currency payeeFspFeeAmountCurrency;

        @JsonProperty("payeeReceiveAmount")
        private String payeeReceiveAmount;

        @JsonProperty("payeeReceiveAmountCurrency")
        private Currency payeeReceiveAmountCurrency;

        @NotBlank
        @JsonProperty("quoteId")
        private String quoteId;

        @NotBlank
        @JsonProperty("transactionId")
        private String transactionId;

        @NotNull
        @JsonProperty("transferAmount")
        private String transferAmount;

        @NotNull
        @JsonProperty("transferAmountCurrency")
        private Currency transferAmountCurrency;

        @JsonProperty("supportedCurrencies")
        private List<String> supportedCurrencies;

        @JsonProperty("error")
        private ErrorInformationResponse error;

    }

}

