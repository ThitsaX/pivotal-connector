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
