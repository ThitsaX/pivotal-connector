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
package com.thitsaworks.mojaloop.coreconnector.ilp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.coreconnector.CoreConnectorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class IlpService {

    private static final Logger LOG = LoggerFactory.getLogger(IlpService.class);

    private static final Map<String, Integer> CURRENCY_SCALE = Map.ofEntries(Map.entry("USD", 2),
                                                                             Map.entry("EUR", 2),
                                                                             Map.entry("GBP", 2),
                                                                             Map.entry("AUD", 2),
                                                                             Map.entry("CAD", 2),
                                                                             Map.entry("CHF", 2),
                                                                             Map.entry("CNY", 2),
                                                                             Map.entry("HKD", 2),
                                                                             Map.entry("SGD", 2),
                                                                             Map.entry("SEK", 2),
                                                                             Map.entry("NOK", 2),
                                                                             Map.entry("DKK", 2),
                                                                             Map.entry("NZD", 2),
                                                                             Map.entry("MXN", 2),
                                                                             Map.entry("BRL", 2),
                                                                             Map.entry("ZAR", 2),
                                                                             Map.entry("KES", 2),
                                                                             Map.entry("GHS", 2),
                                                                             Map.entry("NGN", 2),
                                                                             Map.entry("LRD", 2),
                                                                             Map.entry("ZMW", 2),
                                                                             Map.entry("MWK", 2),
                                                                             Map.entry("TZS", 0),
                                                                             Map.entry("UGX", 0),
                                                                             Map.entry("XOF", 0),
                                                                             Map.entry("XAF", 0),
                                                                             Map.entry("JPY", 0),
                                                                             Map.entry("MMK", 0),
                                                                             Map.entry("RWF", 0),
                                                                             Map.entry("BIF", 0),
                                                                             Map.entry("GNF", 0));

    private static final long FIFTEEN_MINUTES_MS = 15L * 60 * 1000;

    private static final DateTimeFormatter EXPIRATION_FORMATTER = DateTimeFormatter.ofPattern(
                                                                                       "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                                                                   .withZone(ZoneOffset.UTC);

    private final CoreConnectorConfiguration.Settings config;

    private final ObjectMapper objectMapper;

    public IlpService(CoreConnectorConfiguration.Settings config, ObjectMapper objectMapper) {

        this.config = config;
        this.objectMapper = objectMapper;
    }

    public record PrepareResult(String base64PreparePacket, String base64Fulfillment, String base64Condition) { }

    public record FulfilResult(boolean valid, String base64Fulfillment) { }

    public record Expiration(String isoString, long epochMs) { }

    public record ParsedPrepare(long amountMinor,
                                String executionCondition,
                                Instant expiresAt,
                                String destination,
                                String data) { }

    public PrepareResult prepare(String connectorId, long amountMinor, Object agreement, long expireAtMs)
        throws Exception {

        String peer = "g." + connectorId;
        String data = objectMapper.writeValueAsString(agreement);

        byte[] preimage = derivePreimage(config.getIlpSecret(), amountMinor, peer, data);
        byte[] conditionBytes = sha256(preimage);

        byte[] packet = serializeIlpPrepare(amountMinor, conditionBytes, Instant.ofEpochMilli(expireAtMs), peer, data);

        return new PrepareResult(encodeUrlSafe(packet, true),
                                 encodeUrlSafe(preimage, false),
                                 encodeUrlSafe(conditionBytes, false));
    }

    public FulfilResult computeFulfilment(String connectorId,
                                          long amountMinor,
                                          String agreementJson,
                                          String condition,
                                          long lifetimeSeconds) throws Exception {

        if (lifetimeSeconds <= 0) {
            return new FulfilResult(false, null);
        }

        String peer = "g." + connectorId;
        byte[] preimage = derivePreimage(config.getIlpSecret(), amountMinor, peer, agreementJson);
        String derivedCondition = encodeUrlSafe(sha256(preimage), false);

        boolean valid = derivedCondition.equals(condition);
        return new FulfilResult(valid, valid ? encodeUrlSafe(preimage, false) : null);
    }

    public ParsedPrepare unwrap(String base64Packet) {

        byte[] packet = decodeUrlSafe(base64Packet);
        ByteBuffer buf = ByteBuffer.wrap(packet);

        byte type = buf.get();
        if (type != 0x0C) {
            throw new IllegalArgumentException("Unsupported ILP packet type: " + type);
        }

        int contentLength = readOerLength(buf);
        int contentStart = buf.position();

        long amountMinor = buf.getLong();

        byte[] timestampBytes = new byte[18];
        buf.get(timestampBytes);
        Instant expiresAt = parseIlpTimestamp(new String(timestampBytes, StandardCharsets.US_ASCII));

        byte[] conditionBytes = new byte[32];
        buf.get(conditionBytes);

        int destinationLength = readOerLength(buf);
        byte[] destinationBytes = new byte[destinationLength];
        buf.get(destinationBytes);

        int dataLength = readOerLength(buf);
        byte[] dataBytes = new byte[dataLength];
        buf.get(dataBytes);

        if (buf.position() - contentStart != contentLength) {
            throw new IllegalArgumentException("Invalid ILP prepare packet length");
        }

        return new ParsedPrepare(amountMinor,
                                 encodeUrlSafe(conditionBytes, false),
                                 expiresAt,
                                 new String(destinationBytes, StandardCharsets.US_ASCII),
                                 new String(dataBytes, StandardCharsets.UTF_8));
    }

    public <T> T parseAgreement(ParsedPrepare prepare, Class<T> clazz) {

        try {
            return objectMapper.readValue(prepare.data(), clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public long toMinorUnits(String amount, String currency) {

        int scale = CURRENCY_SCALE.getOrDefault(currency.toUpperCase(), 2);

        return new BigDecimal(amount).movePointRight(scale)
                                     .setScale(0, RoundingMode.HALF_UP)
                                     .longValueExact();
    }

    public Expiration buildExpiration() {

        return buildExpiration(FIFTEEN_MINUTES_MS);
    }

    public Expiration buildExpiration(long offsetMs) {

        long epochMs = System.currentTimeMillis() + offsetMs;
        LOG.info("EpochMs value : {}", epochMs);
        var expiration = new Expiration(EXPIRATION_FORMATTER.format(Instant.ofEpochMilli(epochMs)), epochMs);
        LOG.info("EpochMs value : {}", expiration);
        return expiration;
    }

    public long resolveLifetimeSeconds(String expiration) {

        try {
            long
                expiresAt =
                Instant.parse(expiration)
                       .toEpochMilli();
            return Math.max(1L, (expiresAt - System.currentTimeMillis()) / 1000);
        } catch (Exception e) {
            return FIFTEEN_MINUTES_MS / 1000;
        }
    }

    public Map<String, String> zeroMoney(String currency) {

        Map<String, String> money = new LinkedHashMap<>();
        money.put("currency", currency);
        money.put("amount", "0");
        return money;
    }

    public String computePayeeReceiveAmount(String amount, String fee) {

        return new BigDecimal(amount).subtract(new BigDecimal(fee))
                                     .setScale(4, RoundingMode.HALF_UP)
                                     .stripTrailingZeros()
                                     .toPlainString();
    }

    private byte[] derivePreimage(String ilpSecret, long amount, String destination, String data) throws Exception {

        String joined = ilpSecret + ":" + amount + ":" + destination + ":" + data;
        return sha256(joined.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] sha256(byte[] input) throws Exception {

        return MessageDigest.getInstance("SHA-256")
                            .digest(input);
    }

    private byte[] serializeIlpPrepare(long amountMinor,
                                       byte[] condition,
                                       Instant expiresAt,
                                       String destination,
                                       String data) {

        byte[] destinationBytes = destination.getBytes(StandardCharsets.US_ASCII);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] timestampBytes = formatIlpTimestamp(expiresAt).getBytes(StandardCharsets.US_ASCII);

        byte[] destinationLengthBytes = oerLength(destinationBytes.length);
        byte[] dataLengthBytes = oerLength(dataBytes.length);

        int
            contentLength =
            8 + timestampBytes.length + 32 + destinationLengthBytes.length + destinationBytes.length +
                dataLengthBytes.length + dataBytes.length;

        byte[] outerLengthBytes = oerLength(contentLength);

        ByteBuffer buf = ByteBuffer.allocate(1 + outerLengthBytes.length + contentLength);
        buf.put((byte) 0x0C);
        buf.put(outerLengthBytes);
        buf.putLong(amountMinor);
        buf.put(timestampBytes);
        buf.put(condition);
        buf.put(destinationLengthBytes);
        buf.put(destinationBytes);
        buf.put(dataLengthBytes);
        buf.put(dataBytes);

        if (buf.remaining() != 0) {
            throw new IllegalStateException("ILP packet serialization length mismatch");
        }

        return buf.array();
    }

    private byte[] oerLength(int len) {

        if (len < 128) {
            return new byte[]{(byte) len};
        }

        if (len < 256) {
            return new byte[]{
                (byte) 0x81, (byte) len};
        }

        if (len < 65536) {
            return new byte[]{
                (byte) 0x82, (byte) (len >> 8), (byte) (len & 0xFF)};
        }

        throw new IllegalArgumentException("OER length too large: " + len);
    }

    private int readOerLength(ByteBuffer buf) {

        int first = Byte.toUnsignedInt(buf.get());

        if (first < 128) {
            return first;
        }

        int byteCount = first & 0x7F;
        if (byteCount <= 0 || byteCount > 2) {
            throw new IllegalArgumentException("Unsupported OER length byte count: " + byteCount);
        }

        int value = 0;
        for (int i = 0; i < byteCount; i++) {
            value = (value << 8) | Byte.toUnsignedInt(buf.get());
        }

        return value;
    }

    private String formatIlpTimestamp(Instant instant) {

        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);

        return String.format("%04d%02d%02d%02d%02d%02d.%03d",
                             zdt.getYear(),
                             zdt.getMonthValue(),
                             zdt.getDayOfMonth(),
                             zdt.getHour(),
                             zdt.getMinute(),
                             zdt.getSecond(),
                             zdt.getNano() / 1_000_000);
    }

    private Instant parseIlpTimestamp(String value) {

        int year = Integer.parseInt(value.substring(0, 4));
        int month = Integer.parseInt(value.substring(4, 6));
        int day = Integer.parseInt(value.substring(6, 8));
        int hour = Integer.parseInt(value.substring(8, 10));
        int minute = Integer.parseInt(value.substring(10, 12));
        int second = Integer.parseInt(value.substring(12, 14));
        int millis = Integer.parseInt(value.substring(15, 18));

        return ZonedDateTime.of(year, month, day, hour, minute, second, millis * 1_000_000, ZoneOffset.UTC)
                            .toInstant();
    }

    private String encodeUrlSafe(byte[] data, boolean padding) {

        String
            encoded =
            Base64.getUrlEncoder()
                  .withoutPadding()
                  .encodeToString(data);

        if (!padding) {
            return encoded;
        }

        int padLength = (4 - encoded.length() % 4) % 4;
        return encoded + "=".repeat(padLength);
    }

    private byte[] decodeUrlSafe(String value) {

        int padLength = (4 - value.length() % 4) % 4;
        return Base64.getUrlDecoder()
                     .decode(value + "=".repeat(padLength));
    }

}
