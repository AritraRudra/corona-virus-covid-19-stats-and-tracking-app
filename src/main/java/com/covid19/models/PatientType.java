package com.covid19.models;

public enum PatientType {
	INFECTED("infected"), DEAD("dead"), RECOVERED("recovered");

	final String name;

	PatientType(final String type) {
		name = type;
	}
}
