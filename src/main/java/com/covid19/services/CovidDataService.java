package com.covid19.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.covid19.models.LocationStats;
import com.covid19.models.PatientType;
import com.covid19.repositories.LocationStatsRepository;
import com.covid19.services.helpers.CovidDataServiceHelper;

@Service
public class CovidDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CovidDataService.class);

    private static String CONFIRMED_INFECTED_URI = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";

    private static String CONFIRMED_DEATHS_URI = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Deaths.csv";

    private static String CONFIRMED_RECOVERED_URI = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Recovered.csv";

    // http://www.cronmaker.com/ and https://www.freeformatter.com/cron-expression-generator-quartz.html
    // At second :00, at minute :30, at 02am and 14pm, of every day (different from linux cron)
    // https://stackoverflow.com/a/57426242/1679643
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html
    private static final String CRON_SCHEDULE = "0 30 2,14 * * *";

    @Autowired
    private LocationStatsRepository locationRepo;

    @Autowired
    private CovidDataServiceHelper covidHelper;

    // Schedule twice a day to update latest stats in DB.
    // https://dzone.com/articles/running-on-time-with-springs-scheduled-tasks
    // https://stackoverflow.com/questions/30887822/spring-cron-vs-normal-cron
    // @Scheduled(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week]")
    // @Scheduled(fixedDelay = 120000)
    @Scheduled(cron = CRON_SCHEDULE)
    public void scheduledDBUpdate() throws IOException, InterruptedException {
        LOGGER.info("Starting cron scheduled DB update at {}", LocalDateTime.now());
        covidHelper.fetchPrepareAndUpdateWholeDb();
        LOGGER.info("Scheduled DB updated at {}", LocalDateTime.now());
    }

    public List<LocationStats> fetchConfirmedInfected() {
        // Check in DB if data exists and last updated in less one day. If not then fetch, update and send.
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.INFECTED))
            return getDataFromDb();
        else
            covidHelper.triggerAsyncDbUpdate();
        return covidHelper.fetchFromUriByPatientType(CONFIRMED_INFECTED_URI, PatientType.INFECTED);
    }

    public List<LocationStats> fetchConfirmedDeads() {
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.DEAD))
            return getDataFromDb();
        else
            covidHelper.triggerAsyncDbUpdate();
        return covidHelper.fetchFromUriByPatientType(CONFIRMED_DEATHS_URI, PatientType.DEAD);
    }

    public List<LocationStats> fetchConfirmedRecovered() {
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.RECOVERED))
            return getDataFromDb();
        else
            covidHelper.triggerAsyncDbUpdate();
        return covidHelper.fetchFromUriByPatientType(CONFIRMED_RECOVERED_URI, PatientType.RECOVERED);
    }

    private List<LocationStats> getDataFromDb() {
        return locationRepo.findAll();
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
