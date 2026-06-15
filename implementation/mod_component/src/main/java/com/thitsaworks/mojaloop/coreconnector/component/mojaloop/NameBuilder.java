package com.thitsaworks.mojaloop.coreconnector.component.mojaloop;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor
public class NameBuilder {

    public static String buildFullName(String firstName, String middleName, String lastName) {

        return Stream.of(firstName, middleName, lastName)
                     .filter(Objects::nonNull)
                     .filter(name -> !name.isBlank())
                     .collect(Collectors.joining(" "));
    }

}