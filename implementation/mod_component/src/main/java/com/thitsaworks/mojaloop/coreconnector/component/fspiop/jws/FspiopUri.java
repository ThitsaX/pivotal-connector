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
package com.thitsaworks.mojaloop.coreconnector.component.fspiop.jws;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derives the {@code FSPIOP-URI} value that goes into the JWS protected header.
 * <p>
 * {@code FSPIOP-URI} is the <em>resource path</em>, not the request URL: scheme, host, port and any
 * base-path prefix are stripped, so the value is stable no matter how the peer's base URL is
 * configured. That matters here more than on the monorepo side, because each callback target comes
 * from a different setting — {@code fspiopPartiesUrl}, {@code fspiopQuotesUrl},
 * {@code fspiopTransfersUrl} — pointing at three different Hub services.
 *
 * <pre>
 *   https://hub.example.com/quotes/abc-123          -&gt;  /quotes/abc-123
 *   http://moja-quoting-service.mojaloop/quotes/1   -&gt;  /quotes/1
 * </pre>
 * <p>
 * An unrecognised path <strong>throws</strong>. It is never degraded to the raw path: a degraded
 * value is signed and sent successfully today, because no peer verifies yet, and surfaces months
 * later in production as a signature mismatch several layers from its cause.
 * <p>
 * This class must stay behaviourally identical to {@code fspiop-uri.ts} in the Pivotal monorepo.
 * The shared conformance vectors are what enforce that.
 */
public final class FspiopUri {

    /**
     * Resource names as they appear in the FSPIOP API paths, matched case-sensitively.
     * <p>
     * Adding an FSPIOP resource to the platform means adding it here, or signing throws for that
     * path. That is the intended failure mode.
     */
    public static final List<String> RESOURCES = List.of(
        "participants",
        "parties",
        "quotes",
        "transfers",
        "transactionRequests",
        "authorizations",
        "bulkQuotes",
        "bulkTransfers",
        "fxQuotes",
        "fxTransfers",
        "services",
        "transactions");

    private static final Pattern PATTERN = Pattern.compile("/(?:" + String.join("|", RESOURCES) + ")(?:/|$)");

    private static final String SCHEME_SEPARATOR = "://";

    private FspiopUri() {

    }

    /**
     * @param requestUrl a full URL or a bare path; both are accepted
     * @return the resource path, always starting with {@code /}, without query or fragment
     * @throws IllegalArgumentException if the path contains no known FSPIOP resource name
     */
    public static String extract(String requestUrl) {

        if (requestUrl == null || requestUrl.isBlank()) {
            throw new IllegalArgumentException("Cannot derive FSPIOP-URI: the request URL is empty.");
        }

        String path = toPath(requestUrl);
        Matcher matcher = PATTERN.matcher(path);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                "Cannot derive FSPIOP-URI from '" + requestUrl + "': the path contains no known FSPIOP "
                    + "resource name. Known resources: " + String.join(", ", RESOURCES) + ". "
                    + "Add the resource to FspiopUri.RESOURCES if this path is legitimate.");
        }

        return path.substring(matcher.start());
    }

    /**
     * Strips scheme, authority, query and fragment, leaving a path that always starts with
     * {@code /}. Works for absolute URLs and bare paths alike, without requiring a base URL.
     */
    private static String toPath(String requestUrl) {

        String withoutFragment = requestUrl.split("#", 2)[0];
        String withoutQuery = withoutFragment.split("\\?", 2)[0];

        int schemeSeparator = withoutQuery.indexOf(SCHEME_SEPARATOR);

        if (schemeSeparator < 0) {
            return withoutQuery.startsWith("/") ? withoutQuery : "/" + withoutQuery;
        }

        String afterScheme = withoutQuery.substring(schemeSeparator + SCHEME_SEPARATOR.length());
        int pathStart = afterScheme.indexOf('/');

        return pathStart < 0 ? "/" : afterScheme.substring(pathStart);
    }

}
