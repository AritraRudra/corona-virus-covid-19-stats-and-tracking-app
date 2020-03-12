package com.covid19.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.covid19.models.DeadPatientsStats;
import com.covid19.models.InfectedPatientsStats;
import com.covid19.models.LocationStats;
import com.covid19.models.PatientType;
import com.covid19.models.PatientsStats;
import com.covid19.models.RecoveredPatientsStats;
import com.covid19.repositories.LocationStatsRepository;

@Service
public class CovidCsvService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CovidCsvService.class);

    private static String CONFIRMED_INFECTED_URI = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
    private static String CONFIRMED_DEATHS_URI = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Deaths.csv";
    private static String CONFIRMED_RECOVERED_URI = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Recovered.csv";

    @Autowired
    private LocationStatsRepository locationRepo;

    //@PostConstruct
    //Timer after one minute of startup and update DB. Fetch data from DB first if available.
    // Invocations before will also fetch and update DB.
    // Schedule twice a day to update latest stats in DB.
    public void fetchAndSaveStats() {
        LocationStats locationStats = null;
        try {
            locationStats = fetchAndPrepareAllData();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        locationRepo.save(locationStats);

    }

    private LocationStats fetchAndPrepareAllData() throws IOException, InterruptedException {
        final StringBuilder sb = new StringBuilder();
        final LocationStats locationStats = new LocationStats();
        final HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(CONFIRMED_INFECTED_URI)).build();
        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        sb.append(response.body());
        request = HttpRequest.newBuilder().uri(URI.create(CONFIRMED_DEATHS_URI)).build();
        sb.append(response.body());
        request = HttpRequest.newBuilder().uri(URI.create(CONFIRMED_RECOVERED_URI)).build();
        sb.append(response.body());

        return locationStats;
    }

    public List<LocationStats> fetchConfirmedInfected() throws IOException, InterruptedException {
        // Check in DB if data exists and last updated in less one day. If not then fetch, update and send.
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.INFECTED)) {
            return getDataFromDb();
        }
        final HttpClient httpClient = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(CONFIRMED_INFECTED_URI)).build();
        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseCSVResponse(new StringReader(response.body()), PatientType.INFECTED);
    }

    public List<LocationStats> fetchConfirmedDeads() throws IOException, InterruptedException {
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.DEAD)) {
            return getDataFromDb();
        }
        final HttpClient httpClient = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(CONFIRMED_DEATHS_URI)).build();
        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseCSVResponse(new StringReader(response.body()), PatientType.DEAD);
    }

    public List<LocationStats> fetchConfirmedRecovered() throws IOException, InterruptedException {
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.RECOVERED)) {
            return getDataFromDb();
        }
        final HttpClient httpClient = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(CONFIRMED_RECOVERED_URI)).build();
        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseCSVResponse(new StringReader(response.body()), PatientType.RECOVERED);
    }

    private List<LocationStats> parseCSVResponse(final StringReader stringReader, final PatientType patientType) throws IOException {
        final Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(stringReader);
        final List<LocationStats> statsList = new ArrayList<>();
        for (final CSVRecord record : records) {
            final LocationStats latestStats = prepareStats(record, patientType);
            locationRepo.save(latestStats);
            statsList.add(latestStats);
        }
        return statsList;
    }

    private LocationStats prepareStats(final CSVRecord record, final PatientType patientType) {
        final String state = record.get("Province/State");
        final String region = record.get("Country/Region");
        LocationStats locationStats = null;
        final List<LocationStats> tempList = locationRepo.findByStateAndRegion(state, region);
        if (tempList == null || tempList.isEmpty()) {
            locationStats = new LocationStats();
        } else {
            locationStats = (tempList.get(0) == null ? new LocationStats() : tempList.get(0));
        }
        locationStats.setState(state);
        locationStats.setRegion(region);
        updateLocationStatsByPatientsStats(record, locationStats, patientType);
        locationStats.setUpdatedOn(LocalDateTime.now());
        return locationStats;
    }

    private void updateLocationStatsByPatientsStats(final CSVRecord record, final LocationStats locationStats, final PatientType patientType) {
        PatientsStats patientsStats = null;
        switch (patientType) {
            case DEAD:
                patientsStats = prepareAndUpdatePatientsStats(record, new DeadPatientsStats());
                //locationStats.setInfectedPatientsStats(null);
                locationStats.setDeadPatientsStats((DeadPatientsStats) patientsStats);
                //locationStats.setRecoveredPatientsStats(null);
                break;
            case INFECTED:
                patientsStats = prepareAndUpdatePatientsStats(record, new InfectedPatientsStats());
                locationStats.setInfectedPatientsStats((InfectedPatientsStats) patientsStats);
                //locationStats.setDeadPatientsStats(null);
                //locationStats.setRecoveredPatientsStats(null);
                break;
            case RECOVERED:
                patientsStats = prepareAndUpdatePatientsStats(record, new RecoveredPatientsStats());
                //locationStats.setInfectedPatientsStats(null);
                //locationStats.setDeadPatientsStats(null);
                locationStats.setRecoveredPatientsStats((RecoveredPatientsStats) patientsStats);
                break;
            default:
                throw new IllegalArgumentException("Unexpected value: " + patientType);
        }
    }

    private PatientsStats prepareAndUpdatePatientsStats(final CSVRecord record, final PatientsStats patientsStats) {
        final int indexOfStartingColumn = 5;
        // int lastColumn=(int) records.spliterator().getExactSizeIfKnown();
        final int indexOfLastColumn = record.size() - 1;

        final int latestCount = Integer.parseInt(record.get(indexOfLastColumn));
        final int differenceSincePreviousDay = latestCount - Integer.parseInt(record.get(indexOfLastColumn - 1)); // For newly infected locations, previous count will be 0.
        patientsStats.setLatestCount(latestCount);
        patientsStats.setDifferenceSincePreviousDay(differenceSincePreviousDay);

        final List<Integer> listOfDailyCount = new ArrayList<>();
        for (int i = indexOfStartingColumn; i <= indexOfLastColumn; i++) {
            listOfDailyCount.add(Integer.parseInt(record.get(i)));
        }
        patientsStats.setPastCounts(listOfDailyCount);
        return patientsStats;
    }

    private List<LocationStats> getDataFromDb() {
        return setDifferencesSincePreviousDay(locationRepo.findAll());
    }

	private List<LocationStats> setDifferencesSincePreviousDay(List<LocationStats> allStats) {
		for (LocationStats eachLocationStats : allStats) {
			int previousIndex = eachLocationStats.getDeadPatientsStats().getPastCounts().size() - 2;
			int diff = eachLocationStats.getDeadPatientsStats().getLatestCount()
					- eachLocationStats.getDeadPatientsStats().getPastCounts().get(previousIndex);
			eachLocationStats.getDeadPatientsStats().setDifferenceSincePreviousDay(diff);

			previousIndex = eachLocationStats.getInfectedPatientsStats().getPastCounts().size() - 2;
			diff = eachLocationStats.getInfectedPatientsStats().getLatestCount()
					- eachLocationStats.getInfectedPatientsStats().getPastCounts().get(previousIndex);
			eachLocationStats.getInfectedPatientsStats().setDifferenceSincePreviousDay(diff);

			previousIndex = eachLocationStats.getRecoveredPatientsStats().getPastCounts().size() - 2;
			diff = eachLocationStats.getRecoveredPatientsStats().getLatestCount()
					- eachLocationStats.getRecoveredPatientsStats().getPastCounts().get(previousIndex);
			eachLocationStats.getRecoveredPatientsStats().setDifferenceSincePreviousDay(diff);
		}
		return allStats;
	}

	private boolean isLatestDataAvailable() {
        final LocalDateTime latestUpdatedOn = locationRepo.findLatestUpdatedTime();
        if (latestUpdatedOn == null) {
            return false;
        }
        final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        System.out.println("latestUpdatedOn: " + latestUpdatedOn + "yesterday: " + yesterday);
        return !latestUpdatedOn.isBefore(yesterday);
    }

    private boolean isLatestDataAvailableByPatientType(final PatientType patientType) {
        LocalDateTime latestUpdatedOn = null;
        switch (patientType) {
            case DEAD:
                latestUpdatedOn = locationRepo.findLatestUpdatedTimeOfDeadPatients();
                break;
            case INFECTED:
                latestUpdatedOn = locationRepo.findLatestUpdatedTimeOfInfectedPatients();
                break;
            case RECOVERED:
                latestUpdatedOn = locationRepo.findLatestUpdatedTimeOfRecoveredPatients();
                break;
            default:
                throw new IllegalArgumentException("Unexpected value: " + patientType);
        }

        if (latestUpdatedOn == null) {
            return false;
        }
        final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        System.out.println("latestUpdatedOn: " + latestUpdatedOn + "yesterday: " + yesterday);
        return !latestUpdatedOn.isBefore(yesterday);
    }

}
