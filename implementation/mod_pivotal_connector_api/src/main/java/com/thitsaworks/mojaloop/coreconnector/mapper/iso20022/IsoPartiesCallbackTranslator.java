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
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartiesTypeIDPutResponse;
import org.springframework.stereotype.Component;

@Component
public class IsoPartiesCallbackTranslator {

    private final ObjectMapper objectMapper;

    public IsoPartiesCallbackTranslator(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    public ObjectNode toSuccess(PartiesTypeIDPutResponse response, String source,
                                String destination) {

        if (response == null || response.getParty() == null) {

            throw new IllegalArgumentException(
                "Party response is required for ISO callback translation");
        }

        ObjectNode root = objectMapper.createObjectNode();
        putAssignment(root, source, destination);

        ObjectNode report = root.putObject("Rpt");
        report.put("OrgnlId", IsoCallbackSupport.messageId());
        report.put("Vrfctn", true);
        IsoCallbackSupport.putUpdatedParty(report, response.getParty());

        return root;
    }

    public ObjectNode toError(ErrorInformationResponse error, String source, String destination) {

        ObjectNode root = objectMapper.createObjectNode();
        putAssignment(root, source, destination);

        ObjectNode report = root.putObject("Rpt");
        report.put("OrgnlId", IsoCallbackSupport.messageId());
        report.put("Vrfctn", false);

        if (error != null && error.getErrorInformation() != null) {

            String code = IsoCallbackSupport.blankToNull(
                error.getErrorInformation().getErrorCode());

            if (code != null) {

                report.putObject("Rsn").put("Cd", code);
            }
        }

        return root;
    }

    private void putAssignment(ObjectNode root, String source, String destination) {

        ObjectNode assignment = root.putObject("Assgnmt");
        assignment.put("MsgId", IsoCallbackSupport.messageId());
        assignment.put("CreDtTm", IsoCallbackSupport.now());
        IsoCallbackSupport.putAgentId(assignment.putObject("Assgnr"), source);
        IsoCallbackSupport.putAgentId(assignment.putObject("Assgne"), destination);
    }

}
