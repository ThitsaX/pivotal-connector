package com.thitsaworks.mojaloop.coreconnector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(CoreConnectorConfiguration.class)
public class PivotalCoreConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PivotalCoreConnectorApplication.class, args);
    }
}
