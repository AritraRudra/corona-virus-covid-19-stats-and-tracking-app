package com.covid19.models;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.covid19.repositories.converters.StringListConverter;

@Entity
@Table(name = "RecoveredPatientsStats")
public class RecoveredPatientsStats implements PatientsStats, Comparable<RecoveredPatientsStats> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    private int id;

    @Column(name = "latest_recovered_count")
    private int latestCount;

    /**
     * This stores recovered count from previous days as a comma separated String in DB which is transformed and used as List in service.
     */
    @Column(name = "daily_recovered_count", length = 10000)
    @Convert(converter = StringListConverter.class)
    private List<Integer> pastCounts;

    @Column(name = "UpdatedOn")
    private LocalDateTime updatedOn;

    @Transient
    private int differenceSincePreviousDay;

    @PostLoad
    private void calculateAndUpdateDifferenceSincePreviousDay() {
        differenceSincePreviousDay = latestCount - pastCounts.get(pastCounts.size() - 2);
        differenceSincePreviousDay = differenceSincePreviousDay < 0 ? 0 : differenceSincePreviousDay;
    }

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
        calculateAndUpdateDifferenceSincePreviousDay();
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
        builder.append("RecoveredPatientsStats [latestCount=");
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
    public int compareTo(final RecoveredPatientsStats otherPatient) {
        if (otherPatient == null)
            return 1;
        return Integer.compare(otherPatient.latestCount, latestCount);
    }

}
