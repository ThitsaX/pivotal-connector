package com.thitsaworks.mojaloop.coreconnector.component.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ThitsaConnectCustomException extends Exception {

    public ThitsaConnectCustomException(String message) {

        super(message);
    }

}
