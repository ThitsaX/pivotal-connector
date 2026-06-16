package com.thitsaworks.mojaloop.coreconnector.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import retrofit2.HttpException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BackendErrorSerializer {

    private static final int MAX_RESPONSE_BODY_LENGTH = 4_000;

    private final ObjectMapper objectMapper;

    public BackendErrorSerializer(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;
    }

    public String serialize(BackendErrorInput input) {

        try {
            return objectMapper.writeValueAsString(toPayload(input));
        } catch (JsonProcessingException e) {
            return "{\"message\":\"Failed to serialize backend error\"}";
        }
    }

    private Map<String, Object> toPayload(BackendErrorInput input) {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("source", input.source());
        payload.put("operation", input.operation());
        payload.put("message", toMessage(input.error()));

        if (input.error() instanceof HttpException httpException) {
            payload.put("httpStatus", httpException.code());
            String responseBody = responseBody(httpException);
            if (responseBody != null) {
                payload.put("responseBody", truncate(responseBody));
            }
        }

        if (input.error() instanceof SimulatedBackendException simulated) {
            payload.put("httpStatus", simulated.statusCode());
            payload.put("code", simulated.code());
            payload.put("responseBody", simulated.responseBody());
        }

        if (input.context() != null && !input.context()
                                             .isEmpty()) {
            payload.put("context", input.context());
        }

        payload.put("occurredAt",
                    Instant.now()
                           .toString());
        return payload;
    }

    private String toMessage(Throwable error) {

        if (error == null) {
            return "unknown error";
        }

        String message = error.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }

        return error.getClass()
                    .getSimpleName();
    }

    private String responseBody(HttpException error) {

        if (error.response() == null || error.response()
                                             .errorBody() == null) {
            return null;
        }

        try (ResponseBody body = error.response()
                                      .errorBody()) {
            return body.string();
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object truncate(String data) {

        return data.length() > MAX_RESPONSE_BODY_LENGTH ? data.substring(0, MAX_RESPONSE_BODY_LENGTH) +
                                                              "...[truncated]" : data;
    }

    public record BackendErrorInput(String source, String operation, Throwable error, Map<String, Object> context) { }

    public static class SimulatedBackendException extends RuntimeException {

        private final int statusCode;

        private final String code;

        private final Map<String, Object> responseBody;

        public SimulatedBackendException(int statusCode,
                                         String code,
                                         String message,
                                         Map<String, Object> responseBody) {

            super(message);
            this.statusCode = statusCode;
            this.code = code;
            this.responseBody = responseBody;
        }

        public int statusCode() {

            return statusCode;
        }

        public String code() {

            return code;
        }

        public Map<String, Object> responseBody() {

            return responseBody;
        }

    }

}
