package com.covid19.models;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.covid19.repositories.converters.StringListConverter;

@Entity
@Table(name = "infected_patients_stats")
public class InfectedPatientsStats implements PatientsStats, Comparable<InfectedPatientsStats> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    private int id;

    @Column(name = "latest_infected_count")
    private int latestCount;

    @Column(name = "infected_count_history", length = 10000)
    @Convert(converter = StringListConverter.class)
    private List<Integer> pastCounts;

    @Column(name = "UpdatedOn")
    private LocalDateTime updatedOn;

    @Column(name = "Diff")
    private int differenceSincePreviousDay;

    public int getId() {
        return id;
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
        return pastCounts;
    }

    @Override
    public void setPastCounts(final List<Integer> pastCounts) {
        this.pastCounts = pastCounts;
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
        return PatientType.INFECTED;
    }

    @Override
    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    @Override
    public void setUpdatedOn(final LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("InfectedPatientsStats [latestCount=");
        builder.append(latestCount);
        builder.append(", pastCounts=");
        builder.append(pastCounts);
        builder.append(", updatedOn=");
        builder.append(updatedOn);
        builder.append(", differenceSincePreviousDay=");
        builder.append(differenceSincePreviousDay);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(final InfectedPatientsStats otherPatient) {
        if (otherPatient == null)
            return 1;
        return Integer.compare(otherPatient.latestCount, latestCount);
    }
}
