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
 * Supplies the FSPIOP JWS signing key for the one tenant this connector fronts.
 * <p>
 * A connector <em>is</em> exactly one participant, so this is deliberately not a lookup keyed by
 * {@code fspId} — unlike web-outbound, which signs as whichever tenant is the payer and must read
 * every key. That asymmetry is the point: a compromised connector can sign as one DFSP.
 * <p>
 * The interface is the seam between the signing contract and key custody, which differ by deployment
 * profile:
 *
 * <ul>
 *   <li><strong>KMS-backed</strong> — the private key PEM is read from Vault KV and held in process
 *       memory; isolation comes from per-tenant Vault path policy.</li>
 *   <li><strong>HSM-backed</strong> — the implementation returns a handle to a non-exportable
 *       CloudHSM key, and no key material is present.</li>
 * </ul>
 * <p>
 * Implementations read once at startup and cache. Vault is never on the signing path: it can be down
 * and payments continue.
 */
public interface JwsKeyProvider {

    /** The participant this connector signs as. */
    String fspId();

    /**
     * @return the signing key, or {@code null} when signing is not configured — callers pass the
     *     request through unsigned rather than failing, which is what makes rollout a data change
     *     rather than a redeploy
     */
    PrivateKey signingKey();

    /** Re-reads the underlying source. Called on a rotation nudge, never per request. */
    default void refresh() {

    }

}
