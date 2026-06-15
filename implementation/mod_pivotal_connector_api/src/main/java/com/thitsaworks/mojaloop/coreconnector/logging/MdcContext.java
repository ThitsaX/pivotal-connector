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