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
package com.thitsaworks.mojaloop.coreconnector.component.usecase;

import com.thitsaworks.mojaloop.coreconnector.component.exception.SystemProblemException;
import com.thitsaworks.mojaloop.coreconnector.component.exception.ThitsaConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPublicUseCase<I, O> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPublicUseCase.class);

    public O execute(I input) throws ThitsaConnectException {

        LOG.info("Invoking UseCase : id : [{}], name :[{}]", this.getId(), this.getName());

        O output = null;

        try {

            output = this.onExecute(input);

        } catch (ThitsaConnectException e) {

            LOG.error("(Re21Exception) Error : code: [{}], message :[{}]", e.errorCode(), e.defaultErrorMessage());
            LOG.error("Error details :", e);

            throw e;

        } catch (Exception e) {

            LOG.error("Error details :", e);

            throw new SystemProblemException(new String[]{e.getMessage()});
        }

        return output;
    }

    protected abstract O onExecute(I input) throws ThitsaConnectException;

    protected abstract String getName();

    protected abstract String getDescription();

    protected abstract String getScope();

    protected abstract String getId();

}