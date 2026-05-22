package com.hubdelivery.company.company.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hubdelivery.company.company.domain.exception.InvalidCompanyTypeException;

import java.util.Arrays;

public enum CompanyType {
    PRODUCER,
    RECEIVER;

    @JsonCreator
    public static CompanyType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(InvalidCompanyTypeException::new);
    }
}
