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

package com.thitsaworks.mojaloop.coreconnector.mapper.iso20022;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransfersIDPutResponse;
import org.springframework.stereotype.Component;

@Component
public class IsoTransfersCallbackTranslator {

    private final ObjectMapper objectMapper;

    public IsoTransfersCallbackTranslator(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    public ObjectNode toSuccess(TransfersIDPutResponse response) {

        if (response == null) {

            throw new IllegalArgumentException(
                "Transfer response is required for ISO callback translation");
        }

        ObjectNode root = objectMapper.createObjectNode();
        putGroupHeader(root);

        ObjectNode status = root.putObject("TxInfAndSts");

        if (response.getFulfilment() != null) {

            status.put("ExctnConf", response.getFulfilment());
        }

        if (response.getCompletedTimestamp() != null) {

            status.putObject("PrcgDt").put("DtTm", response.getCompletedTimestamp());
        }

        if (response.getTransferState() != null) {

            status.put(
                "TxSts", IsoCallbackSupport.transferState(response.getTransferState().toString()));
        }

        return root;
    }

    public ObjectNode toError(ErrorInformationResponse error) {

        ObjectNode root = objectMapper.createObjectNode();
        putGroupHeader(root);
        IsoCallbackSupport.putErrorReason(root.putObject("TxInfAndSts"), error);

        return root;
    }

    private void putGroupHeader(ObjectNode root) {

        ObjectNode groupHeader = root.putObject("GrpHdr");
        groupHeader.put("MsgId", IsoCallbackSupport.messageId());
        groupHeader.put("CreDtTm", IsoCallbackSupport.now());
    }

}
