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

import com.thitsaworks.mojaloop.coreconnector.component.exception.ThitsaConnectException;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public abstract class AbstractPrivateUseCase<I, O> extends AbstractPublicUseCase<I, O> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPrivateUseCase.class);

    @Override
    public O execute(I input) throws ThitsaConnectException {

        LOG.info("Check authorization for : id :[{}], name :[{}]", this.getId(), this.getName());

        if (!this.isAuthorized(UseCaseContext.get())) {

            LOG.info("User is NOT authorized for : id :[{}], name :[{}]", this.getId(), this.getName());

            throw new UnauthorizedActionException(new String[]{this.getName()});
        }

        return super.execute(input);
    }

    public abstract boolean isAuthorized(Object userDetails);

    @NoArgsConstructor
    public static class UnauthorizedActionException extends ThitsaConnectException {

        protected UnauthorizedActionException(String[] params) {

            super(params);
        }

        @Override
        public String errorCode() {

            return "UNAUTHORIZED_ACTION";
        }

        @Override
        public String defaultErrorMessage() {

            return MessageFormat.format("You are not allowed to perform this action {0}.", this.params[0]);
        }

        @Override
        public boolean requireTranslation() {

            return true;
        }

        @Override
        public String paramDescription() {

            return "{0} : Action";
        }

    }

}
