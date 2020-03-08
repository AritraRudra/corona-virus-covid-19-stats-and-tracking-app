package com.covid19.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

// https://stackoverflow.com/questions/13032948/how-to-create-and-handle-composite-primary-key-in-jpa
// https://stackoverflow.com/questions/3585034/how-to-map-a-composite-key-with-hibernate
// https://stackoverflow.com/questions/58527109/jpa-hibernate-composite-primary-key-with-foreign-key
// https://stackoverflow.com/questions/42693829/how-to-create-jpa-class-for-composite-primary-key
// https://stackoverflow.com/questions/40064122/jpa-hibernate-java-composite-primary-key-one-of-them-is-also-foreign-key
@Entity
// @Embeddable
@Table(name = "LocationStats")
public class LocationStats implements Serializable {

	private static final long serialVersionUID = -8015091260985221765L;

	@Column(name = "state")
	private String state;

	@Column(name = "region")
	private String region;

	// TODO : Fetch types to be updated
	private InfectedPatientsStats infectedPatientsStats;
	private DeadPatientsStats deadPatientsStats;
	private RecoveredPatientsStats recoveredPatientsStats;

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
		builder.append("]");
		return builder.toString();
	}

}
