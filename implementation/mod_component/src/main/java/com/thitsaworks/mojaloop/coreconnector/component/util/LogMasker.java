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

package com.thitsaworks.mojaloop.coreconnector.component.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMasker {

    private static final Pattern JSON_FIELD_PATTERN = Pattern.compile(
        "(?i)(\"([^\"]+)\"\\s*:\\s*\")([^\"]*)(\")");

    private static final Pattern AUTH_PARAMETER_PATTERN = Pattern.compile(
        "(?i)(auth:(?:user|pwd)=)([^&\\s]*)");

    public static String mask(Object obj, ObjectMapper objectMapper) {

        return mask(obj, objectMapper, Set.of(), Set.of());
    }

    /**
     * Masks the built-in credential fields and any additional field names supplied by the caller.
     * Additional sensitive fields are matched case-insensitively and are always fully masked.
     */
    public static String mask(Object obj,
                              ObjectMapper objectMapper,
                              Set<String> additionalSensitiveFields) {

        return mask(obj, objectMapper, additionalSensitiveFields, Set.of());
    }

    /**
     * Masks the built-in credential fields and any additional field names supplied by the caller.
     * Fields in {@code fullyMaskedFields} are fully hidden, while fields in
     * {@code fieldsKeepingLastThree} retain their last three characters. If a field occurs in both
     * sets, full masking takes precedence.
     */
    public static String mask(Object obj,
                              ObjectMapper objectMapper,
                              Set<String> fullyMaskedFields,
                              Set<String> fieldsKeepingLastThree) {

        try {

            String json = objectMapper.writeValueAsString(obj);

            Set<String> fullMaskFields = fullyMaskedFields == null ? Set.of() : fullyMaskedFields;
            Set<String> partialMaskFields =
                fieldsKeepingLastThree == null ? Set.of() : fieldsKeepingLastThree;

            // Mask credentials and caller-configured sensitive fields in JSON request bodies.
            Matcher jsonMatcher = JSON_FIELD_PATTERN.matcher(json);
            StringBuffer jsonResult = new StringBuffer();
            while (jsonMatcher.find()) {
                String fieldName = jsonMatcher.group(2);
                String value = jsonMatcher.group(3);
                String masked = value;

                if (containsIgnoreCase(fullMaskFields, fieldName)) {
                    masked = "****";
                } else if (containsIgnoreCase(partialMaskFields, fieldName)) {
                    masked = maskKeepingLastThree(value);
                } else if (fieldName != null && fieldName.equalsIgnoreCase("username")) {
                    masked = maskUsername(value);
                } else if (fieldName != null && fieldName.matches("(?i)user|pwd")) {
                    masked = maskKeepingLastThree(value);
                } else if (fieldName != null && fieldName.matches("(?i)pincode|pinCode")) {
                    masked = "****";
                }

                jsonMatcher.appendReplacement(
                    jsonResult,
                    Matcher.quoteReplacement(jsonMatcher.group(1) + masked + jsonMatcher.group(4)));
            }
            jsonMatcher.appendTail(jsonResult);

            // Then process the existing auth query parameters.
            Matcher paramMatcher = AUTH_PARAMETER_PATTERN.matcher(jsonResult.toString());
            StringBuffer paramResult = new StringBuffer();
            while (paramMatcher.find()) {
                String masked = maskKeepingLastThree(paramMatcher.group(2));
                paramMatcher.appendReplacement(
                    paramResult,
                    Matcher.quoteReplacement(paramMatcher.group(1) + masked));
            }
            paramMatcher.appendTail(paramResult);

            return paramResult.toString();

        } catch (Exception e) {
            return "ERROR_SERIALIZING" + e.getMessage();
        }
    }

    private static boolean containsIgnoreCase(Set<String> values, String candidate) {

        return candidate != null && values
                                        .stream()
                                        .anyMatch(value -> value != null &&
                                                               value.equalsIgnoreCase(candidate));
    }

    private static String maskKeepingLastThree(String value) {

        return value.length() > 3 ? "****" + value.substring(value.length() - 3) : "****";
    }

    private static String maskUsername(String value) {

        if (value == null || value.isEmpty()) {
            return value;
        }

        if (value.length() <= 3) {
            return "*".repeat(value.length());
        }

        return "*".repeat(value.length() - 3) + value.substring(value.length() - 3);
    }

}
