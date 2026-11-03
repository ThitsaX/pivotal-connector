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
package com.thitsaworks.mojaloop.coreconnector.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import com.thitsaworks.mojaloop.coreconnector.component.mojaloop.ErrorCode;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ErrorInformation;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransactionRequestsPostRequest;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransactionRequestsIDPutResponse;
import com.thitsaworks.mojaloop.coreconnector.logging.MdcExtractors;
import com.thitsaworks.mojaloop.coreconnector.nats.NatsPullListener;
import com.thitsaworks.mojaloop.coreconnector.nats.NatsService;
import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.DoRequestToPay;
import com.thitsaworks.mojaloop.coreconnector.payload.nats.PostTransactionRequestsNatsMessage;
import com.thitsaworks.mojaloop.coreconnector.services.FspClientService;
import io.nats.client.JetStreamManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class TransactionRequestsListener implements InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionRequestsListener.class);

    private final NatsService natsService;

    private final FspiopCallbackService callback;

    private final FspClientService fspClientService;

    private final CoreConnectorConfiguration.Settings config;

    private NatsPullListener<PostTransactionRequestsNatsMessage> listener;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransactionRequestsListener(NatsService natsService,
                                       FspiopCallbackService callback,
                                       FspClientService fspClientService,
                                       CoreConnectorConfiguration.Settings config) {

        this.natsService = natsService;
        this.callback = callback;
        this.fspClientService = fspClientService;
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        String connectorId = config.getConnectorId();
        String subject = natsService.getTransactionRequestsSubject();
        String durable = natsService.normalizeDurable(connectorId, "connector-consumer-post-transactionrequests");

        JetStreamManagement jsm = natsService.jetstreamManager();
        String stream = natsService.ensureStream(jsm, subject);
        natsService.ensureConsumer(jsm, stream, durable, subject);

        LOG.info("Listening on '{}'", subject);

        listener = new NatsPullListener<>(LOG,
            natsService,
            "PostTransactionRequests",
            PostTransactionRequestsNatsMessage.class,
            data -> handle(data, connectorId), MdcExtractors::postTransactionRequests);

        listener.start(subject, stream, durable, "transaction-requests-listener");

    }

    @Override
    public void destroy() {
        if (listener != null) {
            listener.stop();
        }
    }

    void handle(PostTransactionRequestsNatsMessage msg, String connectorId) throws Exception {
        if (msg == null || msg.getRequest() == null) {
            throw new IllegalArgumentException("Transaction request is missing");
        }

        String correlationId = msg.getCorrelationId();
        String payeeFsp = msg.getPayeeFsp();

        TransactionRequestsPostRequest request = msg.getRequest();

        String transactionRequestId = request.getTransactionRequestId();

        if (transactionRequestId == null || transactionRequestId.isBlank()
            || payeeFsp == null || payeeFsp.isBlank()) {
            throw new IllegalArgumentException("RTP request ID and return route are required");
        }

        LOG.info("Post transaction request info from inbound to Payee cc for transactionRequestId={} : {}",
            transactionRequestId , this.objectMapper.writeValueAsString(request));

        try {
            DoRequestToPay.Request requestToFsp = objectMapper.convertValue(request, DoRequestToPay.Request.class);
            DoRequestToPay.Response responseFromFsp = fspClientService.doRequestToPay(requestToFsp);

            if (responseFromFsp == null) {
                throw new PostTransactionRequestException(
                    String.valueOf(ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYER.getStatusCode()),
                    "No response from DFSP backend for transactionRequestId=" + transactionRequestId);
            }
            if (responseFromFsp.getError() != null && responseFromFsp.getError().getErrorInformation() != null) {
                var errorInformation = responseFromFsp.getError().getErrorInformation();
                throw new PostTransactionRequestException(errorInformation.getErrorCode(),
                                                          errorInformation.getErrorDescription());
            }
            if (responseFromFsp.getTransactionRequestState() == null) {
                throw new PostTransactionRequestException(
                    String.valueOf(ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYER.getStatusCode()),
                    "DFSP transaction request state is missing for transactionRequestId=" + transactionRequestId);
            }

            TransactionRequestsIDPutResponse response = new TransactionRequestsIDPutResponse()
                .transactionId(responseFromFsp.getTransactionId())
                .transactionRequestState(responseFromFsp.getTransactionRequestState())
                .extensionList(responseFromFsp.getExtensionList());

            callback.putTransactionRequests(config.getTransactionRequestsUrl(),
                                            correlationId,
                                            connectorId,
                                            payeeFsp,
                                            transactionRequestId,
                                            response);

            LOG.info("Post transaction request Response form Payee CC  transactionRequestId={}",
                transactionRequestId);

        } catch (Exception err) {
            LOG.error("Post transaction request failed transactionRequestId={} exceptionType={}",
                      transactionRequestId, err.getClass().getSimpleName());
            callback.putTransactionRequestsError(config.getTransactionRequestsUrl(),
                                                 correlationId,
                                                 connectorId,
                                                 payeeFsp,
                                                 transactionRequestId,
                                                 toErrorResponse(err, transactionRequestId));
        }
    }

    private ErrorInformationResponse toErrorResponse(Exception err, String idValue) throws JsonProcessingException {

        String errorCode = String.valueOf(ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYER.getStatusCode());
        String errorDescription = err.getMessage();

        if (err instanceof PostTransactionRequestException requestException) {
            errorCode = requestException.errorCode();
            errorDescription = requestException.getMessage();
        }

        ErrorInformation
            errorInformation =
            new ErrorInformation().errorCode(errorCode)
                                  .errorDescription(errorDescription);
        ErrorInformationResponse errorInformationResponse = new ErrorInformationResponse().errorInformation(
            errorInformation);
        LOG.error("Post Transaction Request error Response from Payer cc to HUB for idValue {} : {}",
                  idValue,
                  this.objectMapper.writeValueAsString(errorInformationResponse));

        return errorInformationResponse;
    }

    private static final class PostTransactionRequestException extends Exception {

        private final String errorCode;

        private PostTransactionRequestException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        private String errorCode() {
            return errorCode;
        }

    }
}
