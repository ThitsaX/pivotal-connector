package com.thitsaworks.mojaloop.coreconnector.payload.nats;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesPostRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostQuotesNatsMessage {

    private String correlationId;

    private String payerFsp;

    private String payeeFsp;

    private QuotesPostRequest request;

}