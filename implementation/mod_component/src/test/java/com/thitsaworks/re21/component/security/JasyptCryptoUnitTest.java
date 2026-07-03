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
package com.thitsaworks.re21.component.security;

import com.thitsaworks.mojaloop.coreconnector.component.ComponentConfiguration;
import com.thitsaworks.mojaloop.coreconnector.component.security.JasyptCrypto;
import com.thitsaworks.mojaloop.coreconnector.component.test.EnvAwareUnitTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ComponentConfiguration.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JasyptCryptoUnitTest extends EnvAwareUnitTest {

    private static final Logger LOG = LoggerFactory.getLogger(JasyptCryptoUnitTest.class);

    @Autowired
    private JasyptCrypto jasyptCrypto;

    @Test
    public void encrypt(){

        LOG.info("{}",this.jasyptCrypto.encrypt("dev-only-token"));
    }
}
