package com.thitsaworks.mojaloop.coreconnector.component.mojaloop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.component.retrofit.RetrofitRunner;
import com.thitsaworks.mojaloop.coreconnector.component.spring.SpringContext;
import okhttp3.ResponseBody;

import java.io.IOException;

public class CoreConnectorErrorDecoder implements RetrofitRunner.ErrorDecoder<CoreConnectorErrorResponse> {

    @Override
    public CoreConnectorErrorResponse decode(int status, ResponseBody errorResponseBody) {

        ObjectMapper objectMapper = SpringContext.getBean(ObjectMapper.class);

        try {

            return objectMapper.readValue(errorResponseBody.string(), CoreConnectorErrorResponse.class);

        } catch (IOException e) {

            return null;

        }

    }

}
