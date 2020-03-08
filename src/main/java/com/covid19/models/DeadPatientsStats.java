package com.covid19.models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "dead_patients_stats")
public class DeadPatientsStats implements PatientsStats {

	@Column(name = "latest_death_count")
	private int latestCount;

	@Column(name = "death_count_history")
	private List<Integer> pastCounts;

	@Transient
	private int differenceSincePreviousDay;

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
		return PatientType.DEAD;
	}

}
