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
package com.thitsaworks.mojaloop.coreconnector.component.usecase;

import com.thitsaworks.mojaloop.coreconnector.component.exception.ThitsaConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOwnableUseCase<I, O> extends AbstractPrivateUseCase<I, O> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPrivateUseCase.class);

    @Override
    public O execute(I input) throws ThitsaConnectException {

        LOG.info("Check authorization for : id :[{}], name :[{}]", this.getId(), this.getName());

        if (!this.isAuthorized(UseCaseContext.get())) {

            LOG.info("User is NOT authorized for : id :[{}], name :[{}]", this.getId(), this.getName());

            throw new UnauthorizedActionException(new String[]{this.getName()});
        }

        if (!this.isOwned(UseCaseContext.get())) {

            LOG.info("User is NOT authorized for : id :[{}], name :[{}]", this.getId(), this.getName());

            throw new UnauthorizedActionException(new String[]{this.getName()});

        }

        return super.execute(input);
    }

    public abstract boolean isOwned(Object userDetails);

}
