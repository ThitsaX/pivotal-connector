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
