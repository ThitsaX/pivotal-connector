package com.thitsaworks.mojaloop.coreconnector.component.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMasker {

    public static String mask(Object obj, ObjectMapper objectMapper) {

        try {

            String json = objectMapper.writeValueAsString(obj);

                String sanitized = json;

                // Mask credentials in JSON request bodies. PIN values are always fully hidden.
                Pattern
                    jsonPattern =
                    Pattern.compile("(?i)(\"([^\"]+)\"\\s*:\\s*\")([^\"]*)(\")");
                Matcher jsonMatcher = jsonPattern.matcher(json);
                StringBuffer jsonResult = new StringBuffer();
                while (jsonMatcher.find()) {
                    String fieldName = jsonMatcher.group(2);
                    String value = jsonMatcher.group(3);
                    String masked = value;

                    if (fieldName != null && fieldName.matches("(?i)user|pwd")) {
                        masked = value.length() > 3
                                     ? "****" + value.substring(value.length() - 3)
                                     : "****";
                    } else if (fieldName != null && fieldName.matches("(?i)pincode|pinCode")) {
                        masked = "****";
                    }

                    jsonMatcher.appendReplacement(jsonResult, jsonMatcher.group(1) + masked + jsonMatcher.group(4));
                }
                jsonMatcher.appendTail(jsonResult);
                sanitized = jsonResult.toString();

                // Then process query parameters
                Pattern paramPattern = Pattern.compile("(?i)(auth:(?:user|pwd)=)([^&\\s]*)");
                Matcher paramMatcher = paramPattern.matcher(sanitized);
                StringBuffer paramResult = new StringBuffer();
                while (paramMatcher.find()) {
                    String value = paramMatcher.group(2);
                    String masked = value.length() > 3
                        ? "****" + value.substring(value.length() - 3)
                        : "****";
                    paramMatcher.appendReplacement(paramResult, "$1" + masked);
                }
                paramMatcher.appendTail(paramResult);
                sanitized = paramResult.toString();

            return sanitized;

        } catch (Exception e) {
            return "ERROR_SERIALIZING" + e.getMessage();
        }
    }

}
