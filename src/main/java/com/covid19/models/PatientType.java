package com.covid19.models;

public enum PatientType {
    INFECTED("infected"), DEAD("dead"), RECOVERED("recovered");

    private final String value;

    PatientType(final String type) {
        value = type;
    }

    public String getValue() {
        return value;
    }
}
