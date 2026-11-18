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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Currency;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.ErrorInformationResponse;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Money;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Party;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyComplexName;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.PartyIdInfo;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class IsoCallbackSupport {

    private static final char[] CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Map<String, String> TRANSFER_STATES = Map.of(
        "COMMITTED", "COMM",
        "RESERVED", "RESV", "RECEIVED", "RECV", "ABORTED", "ABOR");

    private IsoCallbackSupport() { }

    static String messageId() {

        byte[] bytes = new byte[16];
        long timestamp = System.currentTimeMillis();
        for (int i = 5; i >= 0; i--) {

            bytes[i] = (byte) timestamp;
            timestamp >>>= 8;
        }
        byte[] randomness = new byte[10];
        RANDOM.nextBytes(randomness);
        System.arraycopy(randomness, 0, bytes, 6, randomness.length);

        BigInteger value = new BigInteger(1, bytes);
        char[] encoded = new char[26];
        BigInteger base = BigInteger.valueOf(32);
        for (int i = encoded.length - 1; i >= 0; i--) {

            BigInteger[] quotientAndRemainder = value.divideAndRemainder(base);
            encoded[i] = CROCKFORD_BASE32[quotientAndRemainder[1].intValue()];
            value = quotientAndRemainder[0];
        }
        return new String(encoded);
    }

    static String now() {

        return DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    static String transferState(String fspiopState) {

        if (fspiopState == null || fspiopState.isBlank()) {

            return null;
        }

        String isoState = TRANSFER_STATES.get(fspiopState);

        if (isoState == null) {

            throw new IllegalArgumentException("Unknown FSPIOP transfer state: " + fspiopState);
        }

        return isoState;
    }

    static String errorDescription(String description) {

        if (description == null || description.isBlank()) {

            return null;
        }

        return description.length() <= 105 ? description : description.substring(0, 105);
    }

    private static void putPartyId(ObjectNode id, PartyIdInfo info) {

        String partyIdType = info.getPartyIdType().toString();

        boolean person = !"BUSINESS".equals(partyIdType) && !"ALIAS".equals(partyIdType) &&
                             !"DEVICE".equals(partyIdType);

        ObjectNode other = person ? id.putObject("PrvtId").putObject("Othr") :
                               id.putObject("OrgId").putObject("Othr");

        other.put("Id", info.getPartyIdentifier());
        other.putObject("SchmeNm").put("Prtry", partyIdType);

    }

    private static String delimitedName(Party party) {

        String first = null;
        String middle = null;
        String last = null;

        if (party.getPersonalInfo() != null && party.getPersonalInfo()
                                                    .getComplexName() != null) {

            PartyComplexName complexName = party.getPersonalInfo().getComplexName();
            first = blankToNull(complexName.getFirstName());
            middle = blankToNull(complexName.getMiddleName());
            last = blankToNull(complexName.getLastName());
        }

        String displayName = blankToNull(party.getName());

        if (first == null && middle == null && last == null && displayName == null) {

            return null;
        }

        return empty(first) + ";" + empty(middle) + ";" + empty(last) + ";" + empty(displayName);
    }

    private static void requireParty(Party party, String elementName) {

        if (party == null || party.getPartyIdInfo() == null ||
                party.getPartyIdInfo().getPartyIdType() == null ||
                blankToNull(party.getPartyIdInfo().getPartyIdentifier()) == null) {

            throw new IllegalArgumentException(
                "Party type and identifier are required for ISO callback translation: " +
                    elementName);
        }
    }

    static void putParty(ObjectNode parent, String elementName, Party party) {

        requireParty(party, elementName);

        ObjectNode isoParty = parent.putObject(elementName);
        putPartyId(isoParty.putObject("Id"), party.getPartyIdInfo());

        String name = delimitedName(party);

        if (name != null) {

            isoParty.put("Nm", name);
        }
    }

    static void putUpdatedParty(ObjectNode report, Party party) {

        requireParty(party, "Pty");

        ObjectNode updated = report.putObject("UpdtdPtyAndAcctId");
        putParty(updated, "Pty", party);
        putAgent(updated, "Agt", party, null);

        String currency = firstCurrency(party.getSupportedCurrencies());

        if (currency != null) {

            updated.putObject("Acct").put("Ccy", currency);
        }
    }

    static void putAgent(ObjectNode parent, String elementName, Party party, String fallbackFspId) {

        String subId = null;
        String fspId = blankToNull(fallbackFspId);

        if (party != null && party.getPartyIdInfo() != null) {

            subId = blankToNull(party.getPartyIdInfo().getPartySubIdOrType());
            String partyFspId = blankToNull(party.getPartyIdInfo().getFspId());
            if (partyFspId != null) {

                fspId = partyFspId;
            }
        }

        if (subId == null && fspId == null) {

            return;
        }

        ObjectNode institution = parent.putObject(elementName).putObject("FinInstnId");

        if (subId != null) {

            institution.putObject("ClrSysMmbId").put("MmbId", subId);
        }

        if (fspId != null) {

            institution.putObject("Othr").put("Id", fspId);
        }
    }

    static void putAgentId(ObjectNode parent, String fspId) {

        if (blankToNull(fspId) == null) {

            return;
        }

        parent.putObject("Agt").putObject("FinInstnId").putObject("Othr").put("Id", fspId);
    }

    static void putAmount(ObjectNode parent, String elementName, String valueElement,
                          Money amount) {

        if (amount == null || amount.getAmount() == null || amount.getCurrency() == null) {

            return;
        }

        ObjectNode isoAmount = parent.putObject(elementName);
        isoAmount.put("Ccy", amount.getCurrency().toString());
        isoAmount.put(valueElement, amount.getAmount());
    }

    static void putErrorReason(ObjectNode status, ErrorInformationResponse error) {

        if (error == null || error.getErrorInformation() == null) {

            return;
        }

        String code = blankToNull(error.getErrorInformation().getErrorCode());
        String description = errorDescription(error.getErrorInformation().getErrorDescription());

        if (code == null && description == null) {

            return;
        }

        ObjectNode reasonInfo = status.putObject("StsRsnInf");

        if (code != null) {

            reasonInfo.putObject("Rsn").put("Prtry", code);
        }

        if (description != null) {

            reasonInfo.put("AddtlInf", description);
        }
    }

    private static String firstCurrency(List<Currency> currencies) {

        if (currencies == null || currencies.isEmpty() || currencies.get(0) == null) {

            return null;
        }

        return currencies.get(0).toString();
    }

    private static String empty(String value) {

        return value == null ? "" : value;
    }

    static String blankToNull(String value) {

        return value == null || value.isBlank() ? null : value;
    }

}
