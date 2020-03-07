package com.covid19.model;

import java.util.List;

public class LocationWiseStats {

	private String state;
	private String region;
	private int latestCount;
	private List<Integer> dailyCount;

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
	
	public int getLatestCount() {
		return latestCount;
	}
	
	public void setLatestCount(int latestCount) {
		this.latestCount = latestCount;
	}

	public List<Integer> getDailyCount() {
		return dailyCount;
	}

	public void setDailyCount(List<Integer> dailyCount) {
		this.dailyCount = dailyCount;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LocationWiseStats [state=");
		builder.append(state);
		builder.append(", region=");
		builder.append(region);
		builder.append(", latestCount=");
		builder.append(latestCount);
		builder.append(", dailyCount=");
		builder.append(dailyCount);
		builder.append("]");
		return builder.toString();
	}
	
}
