package com.thitsaworks.re21.component.security;

import com.google.common.io.BaseEncoding;
import com.thitsaworks.mojaloop.coreconnector.component.security.ThitsaconnectCrypto;
import com.thitsaworks.mojaloop.coreconnector.component.test.EnvAwareUnitTest;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class ThitsaConnectCryptoUnitTest extends EnvAwareUnitTest {

    @Test
    public void test_hmacsha256() {

        String method = "POST";
        String uri = "/secured/create_my_quiz_question_progress";
        String signatureOfPayload = DigestUtils.sha256Hex(
                        "{\"quiz_id\":\"378556788726857728\",\"question_info_list\":[{\"question_id\":\"378553885589012480\",\"choice_id_list\":[\"372805497862078464\",\"372805060282925056\"]}]}")
                .toUpperCase();
        String message = method + "|" + uri + "|" + signatureOfPayload;

        System.out.println("signatureOfPayload : " + signatureOfPayload);
        System.out.println("message : " + message);
        System.out.println("header : " + BaseEncoding.base16().encode(ThitsaconnectCrypto.hmacSha256(
                "5bea824f-8cb9-45b3-ae25-b83d3d137d9e".getBytes(StandardCharsets.UTF_8),
                message.getBytes(StandardCharsets.UTF_8))));
    }

}
