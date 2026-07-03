/*
 * Copyright (c) 2026 ThitsaWorks
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
package com.thitsaworks.mojaloop.coreconnector.component.spring;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.google.common.base.CaseFormat;
import com.thitsaworks.mojaloop.coreconnector.component.exception.ThitsaConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class DefaultSpringRestErrorResponseBuilder
        implements SpringRestServiceExceptionHandler.SpringRestErrorResponseBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSpringRestErrorResponseBuilder.class);

    @Override
    public SpringRestServiceExceptionHandler.SpringRestErrorResponse build(Exception exception) {

        LOG.error("Error : ", exception);

        Map<String, String> i18nErrorMessages = new HashMap<>();

        if ((exception instanceof MethodArgumentNotValidException)) {

            MethodArgumentNotValidException methodArgumentNotValidException =
                    (MethodArgumentNotValidException) exception;
            StringJoiner stringJoiner = new StringJoiner(",");

            List<FieldError> fieldErrors = methodArgumentNotValidException.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                stringJoiner.add(
                        "[ (" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldError.getField()) + ") : " +
                                fieldError.getDefaultMessage() + " ]");
            }

            String errorMessage = "Input error : " + stringJoiner.toString();

            i18nErrorMessages.put("en", errorMessage);

            return new SpringRestServiceExceptionHandler.SpringRestErrorResponse("INPUT_EXCEPTION", errorMessage,
                    i18nErrorMessages);

        } else if (exception instanceof HttpMessageNotReadableException) {

            InvalidFormatException invalidFormatException = (InvalidFormatException) exception.getCause();

            i18nErrorMessages.put("en", invalidFormatException.getMessage());

            return new SpringRestServiceExceptionHandler.SpringRestErrorResponse("INVALID_FORMAT",
                    exception.getMessage(), i18nErrorMessages);

        } else if (exception instanceof ThitsaConnectException) {

            ThitsaConnectException thitsaconnectException = (ThitsaConnectException) exception;

            i18nErrorMessages.put("en", thitsaconnectException.getMessage());

            return new SpringRestServiceExceptionHandler.SpringRestErrorResponse(thitsaconnectException.errorCode(),
                    thitsaconnectException.defaultErrorMessage(), i18nErrorMessages);

        } else if (exception.getCause() instanceof ThitsaConnectException) {

            ThitsaConnectException thitsaconnectException = (ThitsaConnectException) exception.getCause();

            i18nErrorMessages.put("en", thitsaconnectException.getMessage());

            return new SpringRestServiceExceptionHandler.SpringRestErrorResponse(thitsaconnectException.errorCode(),
                    thitsaconnectException.defaultErrorMessage(), i18nErrorMessages);

        } else {

            i18nErrorMessages.put("en", exception.getCause().getMessage());

            return new SpringRestServiceExceptionHandler.SpringRestErrorResponse("INTERNAL_SERVER_ERROR",
                    exception.getCause().getMessage(), i18nErrorMessages);
        }
    }

}