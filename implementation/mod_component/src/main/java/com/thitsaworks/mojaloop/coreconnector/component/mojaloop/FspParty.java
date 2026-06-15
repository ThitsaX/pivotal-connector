package com.thitsaworks.mojaloop.coreconnector.component.mojaloop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Extension;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransactionInitiatorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FspParty implements Serializable {

    @JsonProperty("type")
    private TransactionInitiatorType type;

    @NotNull
    @JsonProperty("idType")
    private PartyIdType idType;

    @NotBlank
    @JsonProperty("idValue")
    private String idValue;

    @NotNull
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

    @NotBlank
    @JsonProperty("fspId")
    private String fspId;

    @JsonProperty("extensionList")
    private List<Extension> extensionList;

}
