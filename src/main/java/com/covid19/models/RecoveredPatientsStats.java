package com.covid19.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "RecoveredPatientsStats")
public class RecoveredPatientsStats implements PatientsStats {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    private int id;

    @Column(name = "latest_recovered_count")
    private int latestCount;

    /**
     * This stores recovered count from previous days as a comma separated String in DB which is transformed and used as List in service.
     */
    @Column(name = "daily_recovered_count", length = 10000)
    private String pastCountsAsString;

    @Transient
    private int differenceSincePreviousDay;

    public int getId() {
        return this.id;
    }

    @Override
    public int getLatestCount() {
        return latestCount;
    }

    @Override
    public void setLatestCount(final int latestCount) {
        this.latestCount = latestCount;
    }

    @Override
    public List<Integer> getPastCounts() {
        return Stream.of(this.pastCountsAsString.split(",")).peek(s -> s.trim()).map(Integer::parseInt).collect(Collectors.toList());
    }

    @Override
    public void setPastCounts(final List<Integer> pastCounts) {
        this.pastCountsAsString = pastCounts.toString();
    }

    @Override
    public int getDifferenceSincePreviousDay() {
        return differenceSincePreviousDay;
    }

    @Override
    public void setDifferenceSincePreviousDay(final int differenceSincePreviousDay) {
        this.differenceSincePreviousDay = differenceSincePreviousDay;
    }

    @Override
    public PatientType getPatientType() {
        return PatientType.RECOVERED;
    }

    @Override
    public String toString() {
        return "RecoveredPatientsStats [id=" + id + ", latestCount=" + latestCount + ", pastCountsAsString=" + pastCountsAsString + ", differenceSincePreviousDay=" + differenceSincePreviousDay + "]";
    }

}
