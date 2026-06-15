package com.thitsaworks.mojaloop.coreconnector.component.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor

public abstract class IgnorableException extends ThitsaConnectException {

    protected IgnorableException(String[] params) {

        super(params);
    }

}
