package com.thitsaworks.mojaloop.coreconnector;

import com.thitsaworks.mojaloop.coreconnector.component.ComponentConfiguration;
import com.thitsaworks.mojaloop.coreconnector.fspiop.model.Currency;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
@ComponentScan("com.thitsaworks.mojaloop.coreconnector")
@Import(ComponentConfiguration.class)
public class CoreConnectorConfiguration {

    @Bean
    public Settings connectorSettings() {

        return new Settings();
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer(
        Settings settings) {

        return factory -> factory.setPort(settings.getSdkConnectorPortNo());
    }

    @Bean
    public OkHttpClient sharedOkHttpClient() {

        return new OkHttpClient.Builder()
                   .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                   .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                   .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                   .build();
    }

    @Getter
    public static class Settings {

        private final String connectorId;

        private final List<Currency> supportedCurrencies;

        private final String ilpSecret;

        private final String natsUrl;

        private final String fspiopStreamName;

        private final String pivotalAuditStreamName;

        private final boolean connectorForcePatchError;

        private final String partiesUrl;

        private final String quotesUrl;

        private final String transfersUrl;

        private final String switchId;

        private final String backendEndpoint;

        private final int backendApiTimeoutMs;

        private final String backendAuthorization;

        private final String backendAccessCode;

        private final String backendPinCode;

        private final String isPrefix;

        private final BigDecimal minimumValue;

        private final String supportedCurrenciesList;

        private final String redisUrl;

        private final int redisTtlSeconds;

        private final BigDecimal transactionAmountLimit;

        private final int sdkConnectorPortNo;

        public Settings() {

            this.connectorId = prop("connectorId", "dfsp");
            this.supportedCurrenciesList = prop("supportedCurrencies", "GNF");
            this.supportedCurrencies = parseCurrencies(this.supportedCurrenciesList);
            this.ilpSecret = prop("connectorIlpSecret", "");

            this.natsUrl = prop("natsUrl", "nats://localhost:4222");
            this.fspiopStreamName = prop("fspiopStreamName", "PIVOTAL_FSPIOP");
            this.pivotalAuditStreamName = prop("pivotalAuditStreamName", "PIVOTAL_AUDIT");
            this.connectorForcePatchError = propBoolean("connectorForcePatchError", false);

            String outbound = prop("outboundEndpoint", "http://localhost:4001");
            this.partiesUrl = prop("fspiopPartiesUrl", outbound);
            this.quotesUrl = prop("fspiopQuotesUrl", outbound);
            this.transfersUrl = prop("fspiopTransfersUrl", outbound);
            this.switchId = prop("fspiopSwitchId", "hub");

            this.backendEndpoint = prop("backendEndpoint", "");
            this.backendApiTimeoutMs = propInt("backendApiTimeoutMs", 30_000);
            this.backendAuthorization = prop("backendAuthorization", prop("apikey", ""));
            this.backendAccessCode = prop("backendAccessCode", prop("accessCode", ""));
            this.backendPinCode = prop("backendPinCode", prop("pinCode", ""));
            this.isPrefix = prop("isPrefix", "false");
            this.minimumValue = propBigDecimal("minimumValue", new BigDecimal("0.01"));
            this.redisUrl = prop("redisUrl", "redis://localhost:7379");
            this.redisTtlSeconds = propInt("redisTtlSeconds", 1200);
            this.transactionAmountLimit = propBigDecimal("transactionAmountLimit", BigDecimal.ZERO);
            this.sdkConnectorPortNo = propInt("sdkConnectorPortNo", 8080);
        }

        private static String prop(String key, String def) {

            String v = System.getProperty(key);
            if (v != null && !v.isBlank()) {
                return v;
            }

            v = System.getProperty(toEnvironmentStyleKey(key));
            if (v != null && !v.isBlank()) {
                return v;
            }

            String normalizedKey = normalizePropertyKey(key);
            for (String propertyName : System.getProperties().stringPropertyNames()) {
                if (normalizePropertyKey(propertyName).equals(normalizedKey)) {
                    v = System.getProperty(propertyName);
                    if (v != null && !v.isBlank()) {
                        return v;
                    }
                }
            }
            return def;
        }

        private static int propInt(String key, int def) {

            try {
                return Integer.parseInt(prop(key, String.valueOf(def)));
            } catch (NumberFormatException e) {
                return def;
            }
        }

        private static boolean propBoolean(String key, boolean def) {

            String value = prop(key, String.valueOf(def)).trim();
            return "1".equals(value) || Boolean.parseBoolean(value);
        }

        private static BigDecimal propBigDecimal(String key, BigDecimal def) {

            try {
                return new BigDecimal(prop(key, def.toPlainString()));
            } catch (NumberFormatException e) {
                return def;
            }
        }

        private static List<Currency> parseCurrencies(String raw) {

            if (raw == null || raw.isBlank()) {
                return List.of();
            }

            return Arrays
                       .stream(raw.split(","))
                       .map(String::trim)
                       .filter(currency -> !currency.isBlank())
                       .map(currency -> Currency.fromValue(currency.toUpperCase(Locale.ROOT)))
                       .toList();
        }

        private static String toEnvironmentStyleKey(String key) {

            StringBuilder value = new StringBuilder();
            for (int i = 0; i < key.length(); i++) {
                char character = key.charAt(i);
                if (Character.isUpperCase(character) && i > 0) {
                    value.append('_');
                }
                value.append(Character.toUpperCase(character));
            }
            return value.toString();
        }

        private static String normalizePropertyKey(String key) {

            return key.replace("_", "")
                      .replace("-", "")
                      .toLowerCase(Locale.ROOT);
        }

    }

}


