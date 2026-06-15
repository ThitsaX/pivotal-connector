package com.thitsaworks.mojaloop.coreconnector.component.util;

import com.thitsaworks.mojaloop.coreconnector.component.exception.ThitsaConnectCustomException;
import com.thitsaworks.mojaloop.coreconnector.component.mojaloop.ErrorCode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class Utility {

    public static boolean isPhoneNumberMatch(String walletPhoneNumber, String mfiPhoneNumber) {

        return walletPhoneNumber.equals(mfiPhoneNumber) || walletPhoneNumber.substring(1)
                                                                            .equals(mfiPhoneNumber) ||
                   (walletPhoneNumber.length() >= 2 && walletPhoneNumber.substring(2)
                                                                        .equals(mfiPhoneNumber));
    }

    public static String stripMyanmarPhoneNumberCode(String number) {

        if (number.startsWith("+")) {
            number = number.substring(1);
        }

        if (number.startsWith("95")) {
            number = number.substring(2);
        }

        if (number.startsWith("9")) {
            number = "0" + number;
        }
        return number;
    }

    public static String stripTrailingZerosAfterDecimalPoint(String strNumber) {

        String resultNumber = strNumber;
        if (strNumber != null) {
            BigDecimal stripedVal = new BigDecimal(strNumber).stripTrailingZeros();
            resultNumber = stripedVal.toPlainString();
        }
        return resultNumber;
    }

    public String removePrefix(String idValue) {

        String removedIdValue;

        if (idValue != null) {
            removedIdValue = idValue.substring(3);
            return removedIdValue;

        } else {
            return idValue;
        }
    }

    public boolean validateDecimalAmount(BigDecimal amount, Integer decimalPlaces) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        if (amount.scale() > decimalPlaces) {
            return false;
        } else {
            return true;
        }
    }

    public void checkDecimalAmount(BigDecimal amount) throws ThitsaConnectCustomException {

        // No decimal part allowed
        if (amount.scale() > 0) {

            throw new ThitsaConnectCustomException(ErrorCode.ROUNDING_VALUE_ERROR.getStatusCode()
                                                                                 .toString() + ":" +
                                                       ErrorCode.ROUNDING_VALUE_ERROR.getDefaultMessage());
        }

    }

}
