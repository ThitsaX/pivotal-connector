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
import com.thitsaworks.mojaloop.coreconnector.component.mojaloop.ErrorInformationResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Currency;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Extension;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdType;
import lombok.*;

import java.io.Serializable;
import java.util.List;

public class LookUp {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request implements Serializable {

        @JsonProperty("idType")
        private PartyIdType idType;

        @JsonProperty("idValue")
        private String idValue;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response implements Serializable {

        @JsonProperty("type")
        private String type;

        @JsonProperty("idType")
        private PartyIdType idType;

        @JsonProperty("idValue")
        private String idValue;

        @JsonProperty("idSubValue")
        private String idSubValue;

        @JsonProperty("displayName")
        private String displayName;

        @JsonProperty("firstName")
        private String firstName;

        @JsonProperty("middleName")
        private String middleName;

        @JsonProperty("lastName")
        private String lastName;

        @JsonProperty("dateOfBirth")
        private String dateOfBirth;

        @JsonProperty("merchantClassificationCode")
        private String merchantClassificationCode;

        @JsonProperty("fspId")
        private String fspId;

        private ExtensionList extensionList;

        @JsonProperty("error")
        private ErrorInformationResponse error;

        private List<Currency> supportedCurrencies;

    }

}
