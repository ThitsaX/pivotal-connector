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
package com.thitsaworks.mojaloop.coreconnector.payload.fspclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransactionRequestState;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.AuthenticationType;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.GeoCode;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Money;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Party;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransactionType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class DoRequestToPay {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Request implements Serializable {

        @JsonProperty("transactionRequestId")
        private String transactionRequestId;

        @JsonProperty("payee")
        private Party payee;

        @JsonProperty("payer")
        private PartyIdInfo payer;

        @JsonProperty("amount")
        private Money amount;

        @JsonProperty("transactionType")
        private TransactionType transactionType;

        @JsonProperty("note")
        private String note;

        @JsonProperty("geoCode")
        private GeoCode geoCode;

        @JsonProperty("authenticationType")
        private AuthenticationType authenticationType;

        @JsonProperty("expiration")
        private String expiration;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

    }


    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Response implements Serializable {

        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("transactionRequestState")
        private TransactionRequestState transactionRequestState;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

        @JsonProperty("error")
        private ErrorInformationResponse error;
    }
}
