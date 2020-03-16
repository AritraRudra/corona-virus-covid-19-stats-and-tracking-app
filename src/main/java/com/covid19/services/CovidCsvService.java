package com.covid19.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
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

    private static final String PROVINCE_STATE = "Province/State";

    private static final String COUNTRY_REGION = "Country/Region";

    // http://www.cronmaker.com/ and https://www.freeformatter.com/cron-expression-generator-quartz.html
    // At second :00, at minute :30, at 02am and 14pm, of every day (different from linux cron)
    // https://stackoverflow.com/a/57426242/1679643
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html
    private static final String CRON_SCHEDULE = "0 30 2,14 * * *";

    @Autowired
    private LocationStatsRepository locationRepo;

    // Schedule twice a day to update latest stats in DB.
    // https://dzone.com/articles/running-on-time-with-springs-scheduled-tasks
    // https://stackoverflow.com/questions/30887822/spring-cron-vs-normal-cron
    // @Scheduled(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week]")
    // @Scheduled(fixedDelay = 120000)
    @Scheduled(cron = CRON_SCHEDULE)
    public void scheduledDBUpdate() throws IOException, InterruptedException {
        LOGGER.info("Starting cron scheduled DB update at {}", LocalDateTime.now());
        fetchPrepareAndUpdateWholeDb();
        LOGGER.info("Scheduled DB updated at {}", LocalDateTime.now());
    }

    private void fetchPrepareAndUpdateWholeDb() {
        final List<LocationStats> fetchedDataForInfected = fetchFromUriByPatientType(CONFIRMED_INFECTED_URI,
                PatientType.INFECTED);
        final List<LocationStats> fetchedDataForDead = fetchFromUriByPatientType(CONFIRMED_DEATHS_URI,
                PatientType.DEAD);
        final List<LocationStats> fetchedDataForRecovered = fetchFromUriByPatientType(CONFIRMED_RECOVERED_URI,
                PatientType.RECOVERED);
        final int maxSize = getMax(fetchedDataForInfected.size(), fetchedDataForDead.size(),
                fetchedDataForRecovered.size());
        for (int i = 0; i < maxSize; i++) {
            final String state = fetchedDataForInfected.get(i).getState();
            final String region = fetchedDataForInfected.get(i).getRegion();
            final LocationStats prepareStats = getPreviousData(state, region);
            prepareStats.setInfectedPatientsStats(fetchedDataForInfected.get(i).getInfectedPatientsStats());

            fetchedDataForDead.stream()
            .filter(deathStats -> state.equals(deathStats.getState()) && region.equals(deathStats.getRegion()))
            .findFirst()
            .ifPresent(matchedStats -> prepareStats.setDeadPatientsStats(matchedStats.getDeadPatientsStats()));

            fetchedDataForRecovered.stream()
            .filter(recoveredStats -> state.equals(recoveredStats.getState())
                    && region.equals(recoveredStats.getRegion())).findFirst()
            .ifPresent(matchedStats -> prepareStats.setRecoveredPatientsStats(matchedStats.getRecoveredPatientsStats()));

            prepareStats.setUpdatedOn(LocalDateTime.now());
            LOGGER.debug("Saving data as: {}", prepareStats);
            locationRepo.save(prepareStats);
        }
    }

    private LocationStats getPreviousData(final String state, final String region) {
        final LocationStats prepareStats;
        final List<LocationStats> dataFromDb = locationRepo.findByStateAndRegion(state, region);
        if (dataFromDb == null || dataFromDb.isEmpty())
            prepareStats = new LocationStats();
        else
            prepareStats = dataFromDb.get(0);

        prepareStats.setState(state);
        prepareStats.setRegion(region);
        return prepareStats;
    }

    @Async("tpDbTaskExecutor")
    public void triggerAsyncDbUpdate() {
        LOGGER.info("Starting async DB update at {}", LocalDateTime.now());
        fetchPrepareAndUpdateWholeDb();
        LOGGER.info("Async thread updated DB at {}", LocalDateTime.now());
    }

    private int getMax(final int i, final int j, final int k) {
        return i > j ? (i > k ? i : k) : (j > k ? j : k);
    }

    public List<LocationStats> fetchConfirmedInfected() {
        // Check in DB if data exists and last updated in less one day. If not then fetch, update and send.
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.INFECTED))
            return getDataFromDb();
        else
            triggerAsyncDbUpdate();
        return fetchFromUriByPatientType(CONFIRMED_INFECTED_URI, PatientType.INFECTED);
    }

    public List<LocationStats> fetchConfirmedDeads() {
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.DEAD))
            return getDataFromDb();
        else
            triggerAsyncDbUpdate();
        return fetchFromUriByPatientType(CONFIRMED_DEATHS_URI, PatientType.DEAD);
    }

    public List<LocationStats> fetchConfirmedRecovered() {
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.RECOVERED))
            return getDataFromDb();
        else
            triggerAsyncDbUpdate();
        return fetchFromUriByPatientType(CONFIRMED_RECOVERED_URI, PatientType.RECOVERED);
    }

    private List<LocationStats> fetchFromUriByPatientType(final String uri, final PatientType patientType) {
        final HttpClient httpClient = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();
        HttpResponse<String> response;
        // TODO : Maybe use retry on few types of exceptions
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseCSVResponse(new StringReader(response.body()), patientType);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Exception occurred : ", e);
            return Collections.emptyList();
        }
    }

    private List<LocationStats> parseCSVResponse(final StringReader stringReader, final PatientType patientType)
            throws IOException {
        final Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(stringReader);
        final List<LocationStats> statsList = new ArrayList<>();
        for (final CSVRecord record : records) {
            final LocationStats latestStats = prepareStats(record, patientType);
            statsList.add(latestStats);
        }
        return statsList;
    }

    private LocationStats prepareStats(final CSVRecord record, final PatientType patientType) {
        final String state = record.get(PROVINCE_STATE);
        final String region = record.get(COUNTRY_REGION);
        LocationStats locationStats = null;
        final List<LocationStats> tempList = locationRepo.findByStateAndRegion(state, region);
        if (tempList == null || tempList.isEmpty())
            locationStats = new LocationStats();
        else
            locationStats = (tempList.get(0) == null ? new LocationStats() : tempList.get(0));
        locationStats.setState(state);
        locationStats.setRegion(region);
        updateLocationStatsByPatientsStats(record, locationStats, patientType);
        locationStats.setUpdatedOn(LocalDateTime.now());
        return locationStats;
    }

    private void updateLocationStatsByPatientsStats(final CSVRecord record, final LocationStats locationStats,
            final PatientType patientType) {
        PatientsStats patientsStats = null;
        switch (patientType) {
            case DEAD :
                patientsStats = prepareAndUpdatePatientsStats(record, new DeadPatientsStats());
                locationStats.setDeadPatientsStats((DeadPatientsStats) patientsStats);
                break;
            case INFECTED :
                patientsStats = prepareAndUpdatePatientsStats(record, new InfectedPatientsStats());
                locationStats.setInfectedPatientsStats((InfectedPatientsStats) patientsStats);
                break;
            case RECOVERED :
                patientsStats = prepareAndUpdatePatientsStats(record, new RecoveredPatientsStats());
                locationStats.setRecoveredPatientsStats((RecoveredPatientsStats) patientsStats);
                break;
            default :
                throw new IllegalArgumentException("Unexpected value: " + patientType);
        }
    }

    private PatientsStats prepareAndUpdatePatientsStats(final CSVRecord record, final PatientsStats patientsStats) {
        final int indexOfStartingColumn = 5;
        // int lastColumn=(int) records.spliterator().getExactSizeIfKnown();
        final int indexOfLastColumn = record.size() - 1;

        final int latestCount = Integer.parseInt(record.get(indexOfLastColumn));
        int differenceSincePreviousDay = latestCount - Integer.parseInt(record.get(indexOfLastColumn - 1));
        // For newly infected locations, previous count will be 0.
        // For cured people and no newly infected, count will decrease from previous day.
        differenceSincePreviousDay = differenceSincePreviousDay < 0 ? 0 : differenceSincePreviousDay;
        patientsStats.setLatestCount(latestCount);
        patientsStats.setDifferenceSincePreviousDay(differenceSincePreviousDay);

        final List<Integer> listOfDailyCount = new ArrayList<>();
        for (int i = indexOfStartingColumn; i <= indexOfLastColumn; i++)
            listOfDailyCount.add(Integer.parseInt(record.get(i)));
        patientsStats.setPastCounts(listOfDailyCount);
        patientsStats.setUpdatedOn(LocalDateTime.now());
        return patientsStats;
    }

    private List<LocationStats> getDataFromDb() {
        // return setDifferencesSincePreviousDay(locationRepo.findAll());
        return locationRepo.findAll();
    }

    private List<LocationStats> setDifferencesSincePreviousDay(final List<LocationStats> allStats) {
        for (final LocationStats eachLocationStats : allStats) {
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
        if (latestUpdatedOn == null)
            return false;
        final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LOGGER.debug("latestUpdatedOn: {}, yesterday: {}", latestUpdatedOn, yesterday);
        return !latestUpdatedOn.isBefore(yesterday);
    }

    private boolean isLatestDataAvailableByPatientType(final PatientType patientType) {
        LocalDateTime latestUpdatedOn = null;
        switch (patientType) {
            case DEAD :
                latestUpdatedOn = locationRepo.findLatestUpdatedTimeOfDeadPatients();
                break;
            case INFECTED :
                latestUpdatedOn = locationRepo.findLatestUpdatedTimeOfInfectedPatients();
                break;
            case RECOVERED :
                latestUpdatedOn = locationRepo.findLatestUpdatedTimeOfRecoveredPatients();
                break;
            default :
                throw new IllegalArgumentException("Unexpected value: " + patientType);
        }

        if (latestUpdatedOn == null)
            return false;
        final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LOGGER.debug("latestUpdatedOn: {}, yesterday: {}", latestUpdatedOn, yesterday);
        return !latestUpdatedOn.isBefore(yesterday);
    }

    public int getCurrentCountByPatientType(final List<LocationStats> stats, final PatientType patientType) {
        int currentCount = 0;
        switch (patientType) {
            case DEAD :
                currentCount = stats.stream().mapToInt(patient -> patient.getDeadPatientsStats().getLatestCount())
                .sum();
                break;
            case INFECTED :
                currentCount = stats.stream().mapToInt(patient -> patient.getInfectedPatientsStats().getLatestCount())
                .sum();
                break;
            case RECOVERED :
                currentCount = stats.stream().mapToInt(patient -> patient.getRecoveredPatientsStats().getLatestCount())
                .sum();
                break;
            default :
                throw new IllegalArgumentException("Unexpected value: " + patientType);
        }
        return currentCount;
    }

    public int getNewCountByPatientType(final List<LocationStats> stats, final PatientType patientType) {
        int newCount = 0;
        switch (patientType) {
            case DEAD :
                int len = stats.get(0).getDeadPatientsStats().getPastCounts().size();
                newCount = stats.stream().mapToInt(patient -> (patient.getDeadPatientsStats().getLatestCount()
                        - patient.getDeadPatientsStats().getPastCounts().get(len - 2))).sum();
                break;
            case INFECTED :
                len = stats.get(0).getInfectedPatientsStats().getPastCounts().size();
                newCount = stats.stream().mapToInt(patient -> (patient.getInfectedPatientsStats().getLatestCount()
                        - patient.getInfectedPatientsStats().getPastCounts().get(len - 2))).sum();
                break;
            case RECOVERED :
                len = stats.get(0).getRecoveredPatientsStats().getPastCounts().size();
                newCount = stats.stream().mapToInt(patient -> (patient.getRecoveredPatientsStats().getLatestCount()
                        - patient.getRecoveredPatientsStats().getPastCounts().get(len - 2))).sum();
                break;
            default :
                throw new IllegalArgumentException("Unexpected value: " + patientType);
        }
        return newCount;
    }

    public int getNewCountByPatientTypeOld(final List<LocationStats> stats, final PatientType patientType) {
        int newCount = 0;
        switch (patientType) {
            case DEAD :
                newCount = stats.stream()
                .mapToInt(patient -> patient.getDeadPatientsStats().getDifferenceSincePreviousDay()).sum();
                break;
            case INFECTED :
                newCount = stats.stream()
                .mapToInt(patient -> patient.getInfectedPatientsStats().getDifferenceSincePreviousDay()).sum();
                break;
            case RECOVERED :
                newCount = stats.stream()
                .mapToInt(patient -> patient.getRecoveredPatientsStats().getDifferenceSincePreviousDay()).sum();
                break;
            default :
                throw new IllegalArgumentException("Unexpected value: " + patientType);
        }
        return newCount;
    }

}
