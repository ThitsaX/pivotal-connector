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
package com.thitsaworks.mojaloop.coreconnector.component.exception;

import java.text.MessageFormat;

public class SystemProblemException extends ThitsaConnectException {

    public SystemProblemException() {

        super();
    }

    public SystemProblemException(String[] params) {

        super(params);
    }

    @Override
    public String errorCode() {

        return "SYSTEM_PROBLEM";
    }

    @Override
    public String defaultErrorMessage() {

        return MessageFormat.format("Something went wrong. Error : {0}", this.params[0]);
    }

    @Override
    public boolean requireTranslation() {

        return false;
    }

}
