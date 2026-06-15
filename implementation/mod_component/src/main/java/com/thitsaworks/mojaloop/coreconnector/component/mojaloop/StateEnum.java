package com.thitsaworks.mojaloop.coreconnector.component.mojaloop;

public enum StateEnum {
    WAITING_FOR_ACTION("WAITING_FOR_ACTION"),
    WAITING_FOR_PARTY_ACCEPTANCE("WAITING_FOR_PARTY_ACCEPTANCE"),
    QUOTE_REQUEST_RECEIVED("QUOTE_REQUEST_RECEIVED"),
    WAITING_FOR_QUOTE_ACCEPTANCE("WAITING_FOR_QUOTE_ACCEPTANCE"),
    PREPARE_RECEIVED("PREPARE_RECEIVED"),
    ERROR_OCCURRED("ERROR_OCCURRED"),
    COMPLETED("COMPLETED"),
    ABORTED("ABORTED"),
    RESERVED("RESERVED");

    private final String value;

    StateEnum(String value) {

        this.value = value;
    }

    public String getValue() {

        return value;
    }
}
