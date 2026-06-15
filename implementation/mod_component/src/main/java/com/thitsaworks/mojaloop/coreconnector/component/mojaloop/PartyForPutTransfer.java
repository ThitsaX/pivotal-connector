package com.thitsaworks.mojaloop.coreconnector.component.mojaloop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyPersonalInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartyForPutTransfer implements Serializable {

    private PartyIdInfo partyIdInfo;

    private String merchantClassificationCode;

    private String name;

    private PartyPersonalInfo personalInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class PartyIdInfo implements Serializable {

        private PartyIdType partyIdType;

        private String partyIdentifier;

        private String partySubIdOrType;

        private String fspId;

        @JsonProperty("extensionList")
        private ExtensionList extensionList;

    }

}
