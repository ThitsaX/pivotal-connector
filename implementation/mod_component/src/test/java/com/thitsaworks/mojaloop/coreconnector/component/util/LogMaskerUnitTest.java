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
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class LogMaskerUnitTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldPreserveExistingMaskingRules() {

        Map<String, String> value = new LinkedHashMap<>();
        value.put("username", "exampleUser");
        value.put("user", "exampleUser");
        value.put("pwd", "secretPassword");
        value.put("pinCode", "1234");

        assertEquals(
            "{\"username\":\"********ser\",\"user\":\"****ser\",\"pwd\":\"****ord\",\"pinCode\":\"****\"}",
            LogMasker.mask(value, objectMapper));
    }

    @Test
    public void shouldFullyMaskAdditionalSensitiveFieldsCaseInsensitively() {

        Map<String, String> value = new LinkedHashMap<>();
        value.put("accessToken", "token-value");
        value.put("safe", "visible");

        assertEquals(
            "{\"accessToken\":\"****\",\"safe\":\"visible\"}",
            LogMasker.mask(value, objectMapper, Set.of("ACCESStoken")));
    }

    @Test
    public void shouldKeepLastThreeForConfiguredSensitiveFieldsCaseInsensitively() {

        Map<String, String> value = new LinkedHashMap<>();
        value.put("accountNumber", "123456789");
        value.put("safe", "visible");

        assertEquals(
            "{\"accountNumber\":\"****789\",\"safe\":\"visible\"}",
            LogMasker.mask(value, objectMapper, Set.of(), Set.of("ACCOUNTnumber")));
    }

    @Test
    public void shouldPreferFullMaskWhenFieldUsesBothOptions() {

        assertEquals(
            "{\"secret\":\"****\"}",
            LogMasker.mask(
                Map.of("secret", "secretValue"), objectMapper, Set.of("secret"), Set.of("secret")));
    }

    @Test
    public void shouldTreatNullSensitiveFieldsAsEmptyForBackwardCompatibility() {

        assertEquals(
            "{\"safe\":\"visible\"}",
            LogMasker.mask(Map.of("safe", "visible"), objectMapper, null));
    }

    @Test
    public void shouldPreserveExistingAuthQueryParameterMasking() {

        assertEquals(
            "\"auth:user=****ser&auth:pwd=****rd\"",
            LogMasker.mask("auth:user=exampleUser&auth:pwd=secretPassword", objectMapper));
    }

    @Test
    public void shouldSafelyMaskReplacementCharacters() {

        assertEquals(
            "{\"secret\":\"****\"}",
            LogMasker.mask(Map.of("secret", "$1\\value"), objectMapper, Set.of("secret")));
    }

}
