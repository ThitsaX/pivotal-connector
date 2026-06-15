package com.thitsaworks.mojaloop.coreconnector.component.test;

public class EnvAwareUnitTest {

    static {

        System.setProperty("JASYPT_PASSWORD", "password");

    }

}