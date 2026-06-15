package com.thitsaworks.mojaloop.coreconnector.payload.fspclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.coreconnector.component.mojaloop.ErrorInformationResponse;
import com.thitsaworks.mojaloop.coreconnector.component.mojaloop.FspParty;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Currency;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.GeoCode;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Money;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Party;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public class ReservationForTransfer {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request implements Serializable {

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

        @JsonProperty("from")
        private FspParty from;

        @JsonProperty("ilpPacket")
        private IlpPacket ilpPacket;

        @JsonProperty("note")
        private String note;

        @JsonProperty("quote")
        private Quote quote;

        @JsonProperty("quoteRequestExtensions")
        private ExtensionList quoteRequestExtensions;

        @JsonProperty("subScenario")
        private String subScenario;

        @JsonProperty("to")
        private FspParty to;

        @JsonProperty("transactionType")
        private String transactionType;

        @JsonProperty("transferId")
        private String transferId;

        @JsonProperty("transactionRequestId")
        private String transactionRequestId;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {

        @JsonProperty("homeTransactionId")
        private String homeTransactionId;

        @JsonProperty("fulfilment")
        private String fulfilment;

        @JsonProperty("error")
        private ErrorInformationResponse error;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Quote {

        @JsonProperty("quoteId")
        private String quoteId;

        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("transferAmount")
        private BigDecimal transferAmount;

        @JsonProperty("transferAmountCurrency")
        private String transferAmountCurrency;

        @JsonProperty("payeeReceiveAmount")
        private BigDecimal payeeReceiveAmount;

        @JsonProperty("payeeReceiveAmountCurrency")
        private String payeeReceiveAmountCurrency;

        @JsonProperty("payeeFspFeeAmount")
        private BigDecimal payeeFspFeeAmount;

        @JsonProperty("payeeFspFeeAmountCurrency")
        private String payeeFspFeeAmountCurrency;

        @JsonProperty("payeeFspCommissionAmount")
        private BigDecimal payeeFspCommissionAmount;

        @JsonProperty("payeeFspCommissionAmountCurrency")
        private String payeeFspCommissionAmountCurrency;

        @JsonProperty("expiration")
        private String expiration;

        @JsonProperty("geoCode")
        private GeoCode geoCode;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IlpPacket {

        @JsonProperty("data")
        private Data data;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        @JsonProperty("quoteId")
        private String quoteId;

        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("payer")
        private Party payer;

        @JsonProperty("payee")
        private Party payee;

        @JsonProperty("amount")
        private Money amount;

        @JsonProperty("transactionType")
        private TransactionType transactionType;

    }

}
