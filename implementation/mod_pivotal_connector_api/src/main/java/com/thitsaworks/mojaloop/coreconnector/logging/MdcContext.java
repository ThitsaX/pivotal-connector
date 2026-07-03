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
package com.thitsaworks.mojaloop.coreconnector.logging;

import org.slf4j.MDC;

import java.util.Map;

public final class MdcContext {

    public static final String TRANSFER_ID = "transferId";

    public static final String ID_VALUE = "idValue";

    private MdcContext() {

    }

    public static Scope open(Map<String, String> values) {

        Map<String, String> previous = MDC.getCopyOfContextMap();

        if (values != null && !values.isEmpty()) {
            values.forEach(MdcContext::putIfPresent);
        }

        return () -> restore(previous);
    }

    private static void putIfPresent(String key, String value) {

        if (key != null && value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    private static void restore(Map<String, String> previous) {

        if (previous == null || previous.isEmpty()) {
            MDC.clear();
            return;
        }

        MDC.setContextMap(previous);
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {

        @Override
        void close();

    }

}