package com.thitsaworks.mojaloop.coreconnector.payload.nats;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetPartiesNatsMessage {

    private String correlationId;

    private String payerFsp;

    private String payeeFsp;

    private String partyIdType;

    private String partyId;

    private String subId;

}
