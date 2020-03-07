package com.covid19.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.covid19.model.LocationWiseStats;

@Service
public class CovidCsvService {
	
	Logger LOGGER = LoggerFactory.getLogger(CovidCsvService.class);

	private static String CONFIRMED_INFECTED_URI ="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
	private static String CONFIRMED_DEATHS_URI = "https://github.com/CSSEGISandData/COVID-19/raw/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Deaths.csv";
	private static String CONFIRMED_RECOVERED_URI = "https://github.com/CSSEGISandData/COVID-19/raw/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Recovered.csv";
	
	//@PostConstruct
	//Timer after one minute of startup and update DB. Fetch data from DB first if available.
	// Invocations before will also fetch and update DB.
	// Schedule twice a day to update latest stats in DB
	public void fetchData() {
		try {
			fetchConfirmedInfected();
		fetchConfirmedDeaths();
		fetchConfirmedRecovered();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Async
	public void fetchConfirmedInfected() throws IOException, InterruptedException {
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(CONFIRMED_INFECTED_URI))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(parseCSVResponse(new StringReader(response.body())));
	}
	
	@Async
	public void fetchConfirmedDeaths() throws IOException, InterruptedException {
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(CONFIRMED_DEATHS_URI))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.body());
	}
	
	@Async
	public void fetchConfirmedRecovered() throws IOException, InterruptedException {
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(CONFIRMED_RECOVERED_URI))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		System.out.println(response.body());
	}
	
	private List<LocationWiseStats> parseCSVResponse(StringReader stringReader) throws IOException{
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(stringReader);
		List<LocationWiseStats> statsList = new ArrayList<>();
		int startingColumn = 5;
		//int lastColumn=(int) records.spliterator().getExactSizeIfKnown();
		//System.out.println(lastColumn);
		for (CSVRecord record : records) {
			int indexOfLastColumn = record.size()-1;
			LocationWiseStats stats = new LocationWiseStats();
			stats.setState(record.get("Province/State"));
			stats.setRegion(record.get("Country/Region"));
			stats.setLatestCount(Integer.parseInt(record.get(indexOfLastColumn)));
			List<Integer> listOfDailyCount = new ArrayList<>();
			for(int i=startingColumn;i<=indexOfLastColumn;i++) {
				listOfDailyCount.add(Integer.parseInt(record.get(i)));
			}
			stats.setDailyCount(listOfDailyCount);
			statsList.add(stats);
		}
		return statsList;
		
	}
}
