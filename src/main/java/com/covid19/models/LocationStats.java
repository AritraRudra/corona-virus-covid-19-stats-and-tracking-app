package com.covid19.models;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

// https://stackoverflow.com/questions/13032948/how-to-create-and-handle-composite-primary-key-in-jpa
// https://stackoverflow.com/questions/3585034/how-to-map-a-composite-key-with-hibernate
// https://stackoverflow.com/questions/58527109/jpa-hibernate-composite-primary-key-with-foreign-key
// https://stackoverflow.com/questions/42693829/how-to-create-jpa-class-for-composite-primary-key
// https://stackoverflow.com/questions/40064122/jpa-hibernate-java-composite-primary-key-one-of-them-is-also-foreign-key
@Entity
// @Embeddable
@Table(name = "LocationStats", uniqueConstraints = { @UniqueConstraint(columnNames = { "state", "region" }) })
public class LocationStats implements Comparable<LocationStats> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    private int id;

    @Column(name = "state")
    private String state;

    @Column(name = "region")
    private String region;

    @Column(name = "lat")
    private String latitude;

    @Column(name = "long")
    private String longitude;

    // TODO : Fetch types to be updated
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = InfectedPatientsStats.class)
    @JoinColumn(name = "fk_id_infected_patients_stats")
    private InfectedPatientsStats infectedPatientsStats;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = DeadPatientsStats.class)
    @JoinColumn(name = "fk_id_dead_patients_stats")
    private DeadPatientsStats deadPatientsStats;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = RecoveredPatientsStats.class)
    @JoinColumn(name = "fk_id_recovered_patients_stats", referencedColumnName = "id")
    private RecoveredPatientsStats recoveredPatientsStats;

    @Column(name = "UpdatedOn")
    private LocalDateTime updatedOn;

    public int getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(final String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(final String longitude) {
        this.longitude = longitude;
    }

    public InfectedPatientsStats getInfectedPatientsStats() {
        return infectedPatientsStats;
    }

    public void setInfectedPatientsStats(final InfectedPatientsStats infectedPatientsStats) {
        this.infectedPatientsStats = infectedPatientsStats;
    }

    public DeadPatientsStats getDeadPatientsStats() {
        return deadPatientsStats;
    }

    public void setDeadPatientsStats(final DeadPatientsStats deadPatientsStats) {
        this.deadPatientsStats = deadPatientsStats;
    }

    public RecoveredPatientsStats getRecoveredPatientsStats() {
        return recoveredPatientsStats;
    }

    public void setRecoveredPatientsStats(final RecoveredPatientsStats recoveredPatientsStats) {
        this.recoveredPatientsStats = recoveredPatientsStats;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(final LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("LocationStats [state=");
        builder.append(state);
        builder.append(", region=");
        builder.append(region);
        builder.append(", infectedPatientsStats=");
        builder.append(infectedPatientsStats);
        builder.append(", deadPatientsStats=");
        builder.append(deadPatientsStats);
        builder.append(", recoveredPatientsStats=");
        builder.append(recoveredPatientsStats);
        builder.append(", updatedOn=");
        builder.append(updatedOn);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int compareTo(final LocationStats otherLocation) {
        int cmp = 0;
        if ((cmp = state.compareTo(otherLocation.state)) == 0)
            return region.compareTo(region);
        return cmp;
    }

}
