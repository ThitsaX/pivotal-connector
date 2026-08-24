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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class FspiopUriUnitTest {

    @Test
    public void shouldStripSchemeHostAndBasePath() {

        assertEquals("/quotes/abc-123", FspiopUri.extract("https://hub.example.com/quotes/abc-123"));
        assertEquals("/parties/MSISDN/959123456",
                     FspiopUri.extract("https://gateway.example.com/fspiop/v2/parties/MSISDN/959123456"));
    }

    @Test
    public void shouldStripQueryAndFragment() {

        assertEquals("/quotes/abc-123", FspiopUri.extract("https://hub.example.com/quotes/abc-123?trace=1#frag"));
    }

    @Test
    public void shouldAcceptBarePathAndNormaliseLeadingSlash() {

        assertEquals("/transfers/t-1", FspiopUri.extract("/transfers/t-1"));
        assertEquals("/transfers/t-1", FspiopUri.extract("transfers/t-1"));
    }

    @Test
    public void shouldKeepTrailingSubResource() {

        assertEquals("/transfers/t-1/error", FspiopUri.extract("https://hub.example.com/transfers/t-1/error"));
    }

    @Test
    public void shouldResolveCollectionPathWithoutId() {

        assertEquals("/quotes", FspiopUri.extract("https://hub.example.com/quotes"));
    }

    @Test
    public void shouldMatchResourceNamesCaseSensitively() {

        assertEquals("/fxQuotes/fx-1", FspiopUri.extract("https://hub.example.com/fxQuotes/fx-1"));
        assertThrows(IllegalArgumentException.class, () -> FspiopUri.extract("https://hub.example.com/Quotes/abc"));
    }

    @Test
    public void shouldThrowRatherThanDegradeOnUnknownResource() {

        assertThrows(IllegalArgumentException.class, () -> FspiopUri.extract("https://hub.example.com/healthz"));
    }

    @Test
    public void shouldNotMatchResourceNameAsBareSubstring() {

        assertThrows(IllegalArgumentException.class, () -> FspiopUri.extract("https://hub.example.com/myquotes/abc"));
    }

    @Test
    public void shouldRejectEmptyUrl() {

        assertThrows(IllegalArgumentException.class, () -> FspiopUri.extract(""));
        assertThrows(IllegalArgumentException.class, () -> FspiopUri.extract(null));
    }

}
