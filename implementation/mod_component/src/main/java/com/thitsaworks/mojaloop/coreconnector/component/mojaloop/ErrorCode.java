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
package com.thitsaworks.mojaloop.coreconnector.component.mojaloop;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public enum ErrorCode {

    COMMUNICATION_ERROR(1000, 500, "Generic communication error."),
    DESTINATION_COMMUNICATION_ERROR(1001,
                                    403,
                                    "Destination of the request failed to be reached. This usually indicates that a Peer FSP failed to respond from an intermediate entity."),
    GENERIC_SERVER_ERROR(2000,
                         500,
                         "Generic server error to be used in order not to disclose information that may be considered private."),
    INTERNAL_SERVER_ERROR(2001,
                          500,
                          "Generic unexpected exception. This usually indicates a bug or unhandled error case."),
    UNSUPPORTED_SERVICE(2002, 501, "Service requested is not supported by the server."),
    SERVICE_UNAVAILABLE(2003,
                        503,
                        "Service requested is currently unavailable on the server. This could be because maintenance is taking place, or because of a temporary failure."),
    SERVER_TIMED_OUT(2004, 504, "Timeout has occurred, meaning the next Party in the chain did not send a callback in time. This could be because a timeout is set too low or because something took longer than expected."),
    SERVER_BUSY(2005, 503, "Server is rejecting requests due to overloading. Try again later."),
    GENERIC_CLIENT_ERROR(3000,
                         500,
                         "Generic client error, used in order not to disclose information that may be considered private."),
    UNACCEPTABLE_VERSION(3001, 406, "Client requested to use a protocol version which is not supported by the server."),
    UNKNOWN_URI(3002, 400, "Provided URI was unknown to the server."),
    ADD_PARTY_INFORMATION_ERROR(3003, 500, "Error occurred while adding or updating information regarding a Party."),
    DUPLICATE_REFERENCE_ID(3041, 400, "Duplicate reference ID."),
    GENERIC_VALIDATION_ERROR(3100,
                             400,
                             "Generic validation error to be used in order not to disclose information that may be considered private."),
    MALFORMED_SYNTAX(3101,
                     400,
                     "Format of the parameter is not valid. For example, amount set to 5.ABC. The error description field should specify which information element is erroneous."),
    MISSING_MANDATORY_ELEMENT(3102, 400, "Mandatory element in the data model was missing."),
    MISSING_MANDATORY_SYNTAX(3103, 400, "Number of elements of an array exceeds the maximum number allowed."),
    TOO_MANY_ELEMENT(3104, 400, "Size of the payload exceeds the maximum size."),
    INVALID_SIGNATURE(3105,
                      500,
                      "Some parameters have changed in the message, making the signature invalid. This may indicate that the message may have been modified maliciously."),
    MODIFIED_REQUEST(3106, 500,
                     "Request with the same ID has previously been processed in which the parameters are not the same."),
    MISSING_MANDATORY_EXTENSION_PARAMETER(3107, 400, "Scheme-mandatory extension parameter was missing."),
    GENERIC_ID_NOT_FOUND(3200, 500, "Generic ID error provided by the client."),
    DESTINATION_FSP_ERROR(3201, 500, "Destination FSP does not exist or cannot be found."),
    PAYER_FSP_ID_NOT_FOUND(3202, 500, "Provided Payer FSP ID not found."),
    PAYEE_FSP_ID_NOT_FOUND(3203, 500, "Provided Payee FSP ID not found."),
    PARTY_NOT_FOUND(3204,
                    404,
                    "Party with the provided identifier, identifier type, and optional sub id or type was not found."),
    QUOTE_ID_NOT_FOUND(3205, 404, "Provided Quote ID was not found on the server."),
    TRANSACTION_REQUEST_ID_NOT_FOUND(3206, 404, "Provided Transaction Request ID was not found on the server."),
    TRANSACTION_ID_NOT_FOUND(3207, 404, "Provided Transaction ID was not found on the server."),
    TRANSFER_ID_NOT_FOUND(3208, 404, "Provided Transfer ID not found on the server."),
    BULK_QUOTE_ID_NOT_FOUND(3209, 404, "Provided Bulk Quote ID was not found on the server."),
    BULK_TRANSFER_ID_NOT_FOUND(3210, 404, "Provided Bulk Transfer ID was not found on the server."),
    PHONE_NUMBER_MISMATCH(3241,
                          500,
                          "There was an error with your account number and phone number combination. Please contact your DFSP to verify the numbers."),
    INACTIVE_ACCOUNT(3242, 404, "Account is not active."),
    GENERIC_EXPIRED_ERROR(3300,
                          500,
                          "Generic expired object error, to be used in order not to disclose information that may be considered private."),
    TRANSACTION_REQUEST_EXPIRED(3301, 500, "Client requested to use a transaction request that has already expired."),
    QUOTE_EXPIRED(3302, 500, "Client requested to use a quote that has already expired."),
    TRANSFER_EXPIRED(3303, 500, "Client requested to use a transfer that has already expired."),
    GENERIC_DOWNSTREAM_ERROR_PAYER(4000, 500, "Generic error related to the Payer or Payer FSP. Used for protecting information that may be considered private."),
    INSUFFICIENT_LIQUIDITY(4001, 500, "Payer FSP has insufficient liquidity to perform the transfer"),
    GENERIC_PAYER_REJECTION(4100, 500, "Payer or Payer FSP rejected the request."),
    PAYER_REJECTED_TRANSACTION_REQUEST(4101, 500, "Payer rejected the transaction request from the Payee."),
    PAYER_FSP_UNSUPPORTED_TRANSACTION_TYPE(4102, 500,
                                           "Payer FSP does not support or rejected the requested transaction type."),
    PAYER_UNSUPPORTED_CURRENCY(4103, 500, "Payer does not have an account which supports the requested currency."),
    PAYER_LIMIT_ERROR(4200, 500, "Generic limit error, for example, the Payer is making more payments per day or per month than they are allowed to, or is making a payment which is larger than the allowed maximum per transaction."),
    PAYER_PERMISSION_ERROR(4300,
                           500,
                           "Generic permission error, the Payer or Payer FSP does not have the access rights to perform the service."),
    GENERIC_PAYER_BLOCKED_ERROR(4400, 500,
                                "Generic Payer blocked error; the Payer is blocked or has failed regulatory screenings."),
    GENERIC_DOWNSTREAM_ERROR_PAYEE(5000, 500, "Generic error due to the Payee or Payee FSP, to be used in order not to disclose information that may be considered private."),
    PAYEE_FSP_INSUFFICIENT_LIQUIDITY(5001, 500, "Payee FSP has insufficient liquidity to perform the transfer."),
    GENERIC_PAYEE_REJECTION(5100, 500, "Payee or Payee FSP rejected the request."),

    PAYEE_REJECTED_REQUEST(5101,
                           500,
                           "Payee does not want to proceed with the financial transaction after receiving a quote."),
    PAYMENT_TYPE_NOT_FOUND(5102, 500, "Payee FSP does not support or rejected the requested transaction type."),
    PAYEE_FSP_REJECTED_QUOTE(5103,
                             500,
                             "Payee FSP does not want to proceed with the financial transaction after receiving a quote."),
    PAYEE_REJECTED_TRANSACTION(5104, 500, "Payee rejected the financial transaction."),
    PAYEE_FSP_REJECTED_TRANSACTION(5105, 500, "Payee FSP rejected the financial transaction."),
    PAYEE_UNSUPPORTED_CURRENCY(5106, 500, "Payee does not have an account that supports the requested currency."),
    PAYEE_LIMIT_ERROR(5200, 500, "Generic limit error, for example, the Payee is receiving more payments per day or per month than they are allowed to, or is receiving a payment that is larger than the allowed maximum per transaction."),
    ROUNDING_VALUE_ERROR(5241, 500, "Amount is invalid. Please enter the format specified by the service provider."),
    PAYEE_PERMISSION_ERROR(5300,
                           500,
                           "Generic permission error, the Payee or Payee FSP does not have the access rights to perform the service."),
    GENERIC_PAYEE_BLOCKED_ERROR(5400,
                                500,
                                "Generic Payee Blocked error, the Payee is blocked or has failed regulatory screenings.");

    private static final Logger LOG = LoggerFactory.getLogger(ErrorCode.class);

    private final Integer statusCode;

    private final Integer httpResponseCode;

    private final String defaultMessage;

    public static JSONObject errorMessageJsonObject;

    public static String endUserFriendlyMessage = "";

    public static String localeMessage = "";

    public static String defaultLanguage = "en_US";

    public static String defaultCode = "ml_code_5000";

    private ErrorCode(Integer statusCode, Integer httpResponseCode, String defaultMessage) {

        this.statusCode = statusCode;
        this.httpResponseCode = httpResponseCode;
        this.defaultMessage = defaultMessage;
    }

    public Integer getStatusCode() {

        return statusCode;
    }

    public Integer getHttpResponseCode() {

        return httpResponseCode;
    }

    public String getDefaultMessage() {

        return defaultMessage;
    }

    //
    public static String getErrorResponse(ErrorCode ec, String message) {

        if (message == null || message.trim().isEmpty()) {
            message = ec.getDefaultMessage();
        }

        return "{" + "\"errorCode\": " + ec.getHttpResponseCode() + ", " + "\"errorInformation\": {" +
            "\"statusCode\": " + ec.getStatusCode() + ", " + "\"description\": \"" + message + "\"}}";

    }

    public static String getMojaloopErrorResponseByStatusCode(String statusCode, String locale) {

        if (errorMessageJsonObject == null) {

            try {

                InputStream inputStream =
                    ErrorCode.class.getClassLoader().getResourceAsStream("error_message_language.json");

                LOG.info("InputStream of Json Language File: " + inputStream);

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferReader = new BufferedReader(inputStreamReader);

                JSONParser jsonParser = new JSONParser();
                JSONObject parsedObject = (JSONObject) jsonParser.parse(bufferReader);

                errorMessageJsonObject = (JSONObject) parsedObject.get("language");

                LOG.info("errorMessageJsonObject length in Locale Json File:" + errorMessageJsonObject.size());

                bufferReader.close();

            } catch (Exception e) {

                LOG.error("Exception in getting locale error message.");
                LOG.error("errorMessageJsonObject in catch: " + errorMessageJsonObject);

                return "{" + "\"errorInformation\": {" + "\"statusCode\": " +
                    ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYER.getStatusCode() + ", " + "\"description\": \"" +
                    ErrorCode.GENERIC_DOWNSTREAM_ERROR_PAYER.getDefaultMessage() + "\", " +
                    "\"descriptionLocale\": \"\"}}";
            }
        }

        String strStatusCode = statusCode;
        String appendedStatusCode = "ml_code_" + statusCode;
        LOG.info("appendedStatusCode: " + appendedStatusCode);

        if (errorMessageJsonObject.containsKey(defaultLanguage)) {

            JSONObject enJsonObject = (JSONObject) errorMessageJsonObject.get(defaultLanguage);

            if (enJsonObject.containsKey(appendedStatusCode)) {

                endUserFriendlyMessage = (String) enJsonObject.get(appendedStatusCode);

            } else {

                strStatusCode = "5000";
                endUserFriendlyMessage = (String) enJsonObject.get(defaultCode);
            }
        }

        if (locale != null && errorMessageJsonObject.containsKey(locale)) {

            JSONObject localeJsonObject = (JSONObject) errorMessageJsonObject.get(locale);

            if (localeJsonObject.containsKey(appendedStatusCode)) {

                localeMessage = (String) localeJsonObject.get(appendedStatusCode);

                if (!Objects.equals(strStatusCode, statusCode)) {

                    strStatusCode = statusCode;
                    endUserFriendlyMessage = "";
                }

            } else {

                if (!Objects.equals(strStatusCode, statusCode)) {

                    localeMessage = (String) localeJsonObject.get(defaultCode);
                } else {

                    localeMessage = endUserFriendlyMessage;
                }
            }
        } else {

            localeMessage = endUserFriendlyMessage;
        }

        return "{" + "\"errorInformation\": {" + "\"statusCode\": " + strStatusCode + ", " + "\"description\": \"" +
            endUserFriendlyMessage + "\", " + "\"descriptionLocale\": \"" + localeMessage + "\"}}";
    }

    public static String getErrorResponse(ErrorCode ec) {

        return getErrorResponse(ec, null);
    }
}
