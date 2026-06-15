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
