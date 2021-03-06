package com.covid19.models;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientsStats {

    PatientType getPatientType();

    int getLatestCount();

    void setLatestCount(final int latestCount);

    List<Integer> getPastCounts();

    void setPastCounts(final List<Integer> getpastCounts);

    int getDifferenceSincePreviousDay();

    void setDifferenceSincePreviousDay(final int differenceSincePreviousDay);

    LocalDateTime getUpdatedOn();

    void setUpdatedOn(final LocalDateTime updatedOn);

}
