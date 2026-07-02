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
