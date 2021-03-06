package com.covid19.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.covid19.constants.CovidConstants;
import com.covid19.models.LocationStats;
import com.covid19.models.PatientType;
import com.covid19.repositories.LocationStatsRepository;
import com.covid19.services.helpers.CovidDataServiceHandler;

@Service
public class CovidDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CovidDataService.class);

    // http://www.cronmaker.com/ and https://www.freeformatter.com/cron-expression-generator-quartz.html
    // At second :00, at minute :30, at 02am and 14pm, of every day (different from linux cron)
    // https://stackoverflow.com/a/57426242/1679643
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/support/CronSequenceGenerator.html
    private static final String CRON_SCHEDULE = "0 30 2,14 * * *";

    @Autowired
    private LocationStatsRepository locationRepo;

    @Autowired
    private CovidDataServiceHandler covidHandler;

    // Schedule twice a day to update latest stats in DB.
    // https://dzone.com/articles/running-on-time-with-springs-scheduled-tasks
    // https://stackoverflow.com/questions/30887822/spring-cron-vs-normal-cron
    // @Scheduled(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week]")
    // @Scheduled(fixedDelay = 120000)
    @Scheduled(cron = CRON_SCHEDULE)
    public void scheduledDBUpdate() throws IOException, InterruptedException {
        LOGGER.info("Starting cron scheduled DB update at {}", LocalDateTime.now());
        covidHandler.fetchPrepareAndUpdateWholeDb();
        LOGGER.info("Scheduled DB updated at {}", LocalDateTime.now());
    }

    public List<LocationStats> fetchConfirmedInfected() {
        // Check in DB if data exists and last updated in less than update duration. If not then fetch and send.
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.INFECTED))
            return getDataFromDb();
        else
            // TODO : for test and fast data update, remove this else case in prod
            covidHandler.triggerAsyncDbUpdate();
        return covidHandler.fetchFromUriByPatientType(CovidConstants.CONFIRMED_INFECTED_URI, PatientType.INFECTED);
    }

    public List<LocationStats> fetchConfirmedDeads() {
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.DEAD))
            return getDataFromDb();
        else
            // TODO : for test and fast data update, remove this else case in prod
            covidHandler.triggerAsyncDbUpdate();
        return covidHandler.fetchFromUriByPatientType(CovidConstants.CONFIRMED_DEATHS_URI, PatientType.DEAD);
    }

    public List<LocationStats> fetchConfirmedRecovered() {
        if (isLatestDataAvailable() && isLatestDataAvailableByPatientType(PatientType.RECOVERED))
            return getDataFromDb();
        else
            // TODO : for test and fast data update, remove this else case in prod
            covidHandler.triggerAsyncDbUpdate();
        return covidHandler.fetchFromUriByPatientType(CovidConstants.CONFIRMED_RECOVERED_URI, PatientType.RECOVERED);
    }

    private List<LocationStats> getDataFromDb() {
        return locationRepo.findAll();
    }

    private boolean isLatestDataAvailable() {
        final LocalDateTime latestUpdatedOn = locationRepo.findLatestUpdatedTime();
        if (latestUpdatedOn == null)
            return false;
        final LocalDateTime previousUpdatedTimeStamp = LocalDateTime.now().minusHours(12);
        LOGGER.debug("latestUpdatedOn: {}", latestUpdatedOn);
        return !latestUpdatedOn.isBefore(previousUpdatedTimeStamp);
    }

    private boolean isLatestDataAvailableByPatientType(final PatientType patientType) {
        LocalDateTime latestUpdatedOn = switch (patientType) {
            case DEAD -> latestUpdatedOn = locationRepo.findLatestUpdatedTimeOfDeadPatients();
            case INFECTED -> latestUpdatedOn = locationRepo.findLatestUpdatedTimeOfInfectedPatients();
            case RECOVERED -> latestUpdatedOn = locationRepo.findLatestUpdatedTimeOfRecoveredPatients();
            default -> throw new IllegalArgumentException("Unexpected value: " + patientType);
        };

        if (latestUpdatedOn == null)
            return false;
        final LocalDateTime previousUpdatedTimeStamp = LocalDateTime.now().minusHours(12);
        LOGGER.debug("latestUpdatedOn: {}", latestUpdatedOn);
        return !latestUpdatedOn.isBefore(previousUpdatedTimeStamp);
    }

    public int getCurrentCountByPatientType(final List<LocationStats> stats, final PatientType patientType) {
        return switch (patientType) {
            case DEAD -> stats.stream().mapToInt(patient -> patient.getDeadPatientsStats().getLatestCount()).sum();
            case INFECTED -> stats.stream().mapToInt(patient -> patient.getInfectedPatientsStats().getLatestCount())
            .sum();
            case RECOVERED -> stats.stream().mapToInt(patient -> patient.getRecoveredPatientsStats().getLatestCount())
            .sum();
            default -> throw new IllegalArgumentException("Unexpected value: " + patientType);
        };
    }

    public int getNewCountByPatientType(final List<LocationStats> stats, final PatientType patientType) {
        return switch (patientType) {
            case DEAD -> stats.stream().mapToInt(
                    patient -> (patient.getDeadPatientsStats().getLatestCount()
                            - patient.getDeadPatientsStats().getPastCounts()
                            .get(stats.get(0).getDeadPatientsStats().getPastCounts().size() - 2)))
            .sum();
            case INFECTED -> stats.stream().mapToInt(
                    patient -> (patient.getInfectedPatientsStats().getLatestCount()
                            - patient.getInfectedPatientsStats().getPastCounts()
                            .get(stats.get(0).getInfectedPatientsStats().getPastCounts().size() - 2)))
            .sum();
            case RECOVERED -> stats.stream()
            .mapToInt(patient -> (patient.getRecoveredPatientsStats().getLatestCount()
                    - patient.getRecoveredPatientsStats().getPastCounts()
                    .get(stats.get(0).getRecoveredPatientsStats().getPastCounts().size() - 2)))
            .sum();
            default -> throw new IllegalArgumentException("Unexpected value: " + patientType);
        };
    }

    public int getNewCountByPatientTypeOld(final List<LocationStats> stats, final PatientType patientType) {
        return switch (patientType) {
            case DEAD -> stats.stream()
                    .mapToInt(patient -> patient.getDeadPatientsStats().getDifferenceSincePreviousDay()).sum();
            case INFECTED -> stats.stream()
                    .mapToInt(patient -> patient.getInfectedPatientsStats().getDifferenceSincePreviousDay()).sum();
            case RECOVERED -> stats.stream()
                    .mapToInt(patient -> patient.getRecoveredPatientsStats().getDifferenceSincePreviousDay()).sum();
            default -> throw new IllegalArgumentException("Unexpected value: " + patientType);
        };
    }

}
