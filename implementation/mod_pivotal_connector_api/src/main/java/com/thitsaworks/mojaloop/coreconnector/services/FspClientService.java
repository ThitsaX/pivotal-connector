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
package com.thitsaworks.mojaloop.coreconnector.services;

import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.ConfirmationForTransfer;
import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.DoQuote;
import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.LookUp;
import com.thitsaworks.mojaloop.coreconnector.payload.fspclient.ReservationForTransfer;
import org.springframework.stereotype.Component;

@Component
public interface FspClientService {

    LookUp.Response doLookUp(LookUp.Request request);

    DoQuote.Response doQuote(DoQuote.Request request);

    ReservationForTransfer.Response doReservationForTransfer(ReservationForTransfer.Request request);

    ConfirmationForTransfer.Response doConfirmationForTransfer(ConfirmationForTransfer.Request request);

}
