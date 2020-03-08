package com.covid19.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.covid19.models.DeadPatientsStats;
import com.covid19.models.InfectedPatientsStats;
import com.covid19.models.LocationStats;
import com.covid19.models.PatientType;
import com.covid19.models.PatientsStats;
import com.covid19.models.RecoveredPatientsStats;

@Service
public class CovidCsvService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CovidCsvService.class);

	private static String CONFIRMED_INFECTED_URI ="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
	private static String CONFIRMED_DEATHS_URI = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Deaths.csv";
	private static String CONFIRMED_RECOVERED_URI = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Recovered.csv";

	//@PostConstruct
	//Timer after one minute of startup and update DB. Fetch data from DB first if available.
	// Invocations before will also fetch and update DB.
	// Schedule twice a day to update latest stats in DB.
	public void fetchData() {
		try {
			fetchConfirmedInfected();
			fetchConfirmedDeads();
			fetchConfirmedRecovered();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public List<LocationStats> fetchConfirmedInfected() throws IOException, InterruptedException {
		final HttpClient httpClient = HttpClient.newHttpClient();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(CONFIRMED_INFECTED_URI))
				.build();
		final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return parseCSVResponse(new StringReader(response.body()), PatientType.INFECTED);
	}

	public List<LocationStats> fetchConfirmedDeads() throws IOException, InterruptedException {
		final HttpClient httpClient = HttpClient.newHttpClient();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(CONFIRMED_DEATHS_URI))
				.build();
		final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return parseCSVResponse(new StringReader(response.body()), PatientType.DEAD);
	}

	public List<LocationStats> fetchConfirmedRecovered() throws IOException, InterruptedException {
		final HttpClient httpClient = HttpClient.newHttpClient();
		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(CONFIRMED_RECOVERED_URI))
				.build();
		final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return parseCSVResponse(new StringReader(response.body()), PatientType.RECOVERED);
	}

	private List<LocationStats> parseCSVResponse(final StringReader stringReader, final PatientType patientType) throws IOException {
		final Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(stringReader);
		final List<LocationStats> statsList = new ArrayList<>();

		for (final CSVRecord record : records) {
			final LocationStats locationStats = new LocationStats();
			locationStats.setState(record.get("Province/State"));
			locationStats.setRegion(record.get("Country/Region"));

			final PatientsStats infectedPatientsStats = prepareStatsByPatientsType(record, PatientType.INFECTED);
			final PatientsStats deadPatientsStats = prepareStatsByPatientsType(record, PatientType.DEAD);
			final PatientsStats recoveredStats = prepareStatsByPatientsType(record, PatientType.RECOVERED);
			locationStats.setInfectedPatientsStats((InfectedPatientsStats) infectedPatientsStats);
			locationStats.setDeadPatientsStats((DeadPatientsStats) deadPatientsStats);
			locationStats.setRecoveredPatientsStats((RecoveredPatientsStats) recoveredStats);
			statsList.add(locationStats);
		}
		return statsList;
	}

	private PatientsStats prepareStatsByPatientsType(final CSVRecord record, final PatientType patientType) {
		final int indexOfStartingColumn = 5;
		// int lastColumn=(int) records.spliterator().getExactSizeIfKnown();
		final int indexOfLastColumn = record.size() - 1;

		@SuppressWarnings("preview")
		final PatientsStats patientsStats = switch (patientType) {
		case DEAD: {
			yield new DeadPatientsStats();
		}
		case INFECTED:
			yield new InfectedPatientsStats();
		case RECOVERED:
			yield new RecoveredPatientsStats();
		default:
			throw new IllegalArgumentException("Unexpected value: " + patientType);
		};

		final int latestCount = Integer.parseInt(record.get(indexOfLastColumn));
		final int differenceSincePreviousDay = latestCount - Integer.parseInt(record.get(indexOfLastColumn - 1));  // For newly infected locations, previous count will be 0.
		patientsStats.setLatestCount(latestCount);
		patientsStats.setDifferenceSincePreviousDay(differenceSincePreviousDay);
		final List<Integer> listOfDailyCount = new ArrayList<>();
		for (int i = indexOfStartingColumn; i <= indexOfLastColumn; i++) {
			listOfDailyCount.add(Integer.parseInt(record.get(i)));
		}
		patientsStats.setPastCounts(listOfDailyCount);
		return patientsStats;
	}
}
