package com.thitsaworks.mojaloop.coreconnector.logging;

import com.thitsaworks.mojaloop.coreconnector.fspiop.model.QuotesPostRequest;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.TransfersPostRequest;
import com.thitsaworks.mojaloop.coreconnector.payload.nats.GetPartiesNatsMessage;
import com.thitsaworks.mojaloop.coreconnector.payload.nats.PatchTransfersNatsMessage;
import com.thitsaworks.mojaloop.coreconnector.payload.nats.PostQuotesNatsMessage;
import com.thitsaworks.mojaloop.coreconnector.payload.nats.PostTransfersNatsMessage;

import java.util.Collections;
import java.util.Map;

public final class MdcExtractors {

    private MdcExtractors() {
    }

    public static Map<String, String> postTransfers(PostTransfersNatsMessage msg) {
        TransfersPostRequest request = msg != null ? msg.getRequest() : null;
        return transferId(request != null ? request.getTransferId() : null);
    }

    public static Map<String, String> postQuotes(PostQuotesNatsMessage msg) {
        QuotesPostRequest request = msg != null ? msg.getRequest() : null;
        return transferId(request != null ? request.getTransactionId() : null);
    }

    public static Map<String, String> patchTransfers(PatchTransfersNatsMessage msg) {
        return transferId(msg != null ? msg.getTransferId() : null);
    }

    public static Map<String, String> getParties(GetPartiesNatsMessage msg) {
        if (msg == null || isBlank(msg.getPartyId())) {
            return Collections.emptyMap();
        }

        String subId = msg.getSubId();
        String idValue = isBlank(subId) ? msg.getPartyId() : msg.getPartyId() + " " + subId;

        return Collections.singletonMap(MdcContext.ID_VALUE, idValue);
    }

    private static Map<String, String> transferId(String transferId) {
        if (isBlank(transferId)) {
            return Collections.emptyMap();
        }

        return Collections.singletonMap(MdcContext.TRANSFER_ID, transferId);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}