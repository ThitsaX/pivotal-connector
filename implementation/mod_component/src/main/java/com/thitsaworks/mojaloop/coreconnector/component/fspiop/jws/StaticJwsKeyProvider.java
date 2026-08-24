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
package com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws;

import java.security.PrivateKey;

/**
 * A key provider backed by an already-loaded key.
 * <p>
 * <strong>For tests and local development only.</strong> There is no production path that supplies a
 * key this way — key material reaches a deployed connector from Vault, never from configuration.
 * Wiring this in a deployment would put a private key somewhere with no rotation story and no audit
 * trail; {@link VaultJwsKeyProvider} exists so that is never necessary.
 */
public class StaticJwsKeyProvider implements JwsKeyProvider {

    private final String fspId;

    private final PrivateKey signingKey;

    public StaticJwsKeyProvider(String fspId, PrivateKey signingKey) {

        this.fspId = fspId;
        this.signingKey = signingKey;
    }

    @Override
    public String fspId() {

        return this.fspId;
    }

    @Override
    public PrivateKey signingKey() {

        return this.signingKey;
    }

}
