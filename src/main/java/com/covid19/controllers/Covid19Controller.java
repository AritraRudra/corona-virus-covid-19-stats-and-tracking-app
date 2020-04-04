package com.covid19.controllers;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.covid19.models.LocationStats;
import com.covid19.models.PatientType;
import com.covid19.services.CovidDataService;

@Controller
public class Covid19Controller {

    private static final String UNDERSCORE = "_";

    @Autowired
    private CovidDataService csvService;

    @GetMapping("/")
    public String homePageInfo(final Model uiModel) throws IOException, InterruptedException {
        // TODO : Maybe use retry on few types of exceptions
        List<LocationStats> stats = csvService.fetchConfirmedInfected()
                .parallelStream().sorted().collect(Collectors.toList());
        fetchAndUpdateDataForUIModel(uiModel, stats, PatientType.INFECTED);
        stats = csvService.fetchConfirmedDeads();
        fetchAndUpdateDataForUIModel(uiModel, stats, PatientType.DEAD);
        stats = csvService.fetchConfirmedRecovered();
        fetchAndUpdateDataForUIModel(uiModel, stats, PatientType.RECOVERED);
        return "home";
    }

    @GetMapping("/infected")
    public String infectedInfo(final Model uiModel) throws IOException, InterruptedException {
        // TODO : Maybe use retry on few types of exceptions
        final List<LocationStats> stats = csvService.fetchConfirmedInfected();
        fetchAndUpdateDataForUIModel(uiModel, stats
                .parallelStream()
                .sorted(
                        Comparator.comparing(LocationStats::getInfectedPatientsStats))
                .collect(Collectors.toList()), PatientType.INFECTED);
        // return PatientType.INFECTED.name();
        System.out.println(PatientType.INFECTED);
        System.out.println(PatientType.INFECTED.name());
        System.out.println(PatientType.INFECTED.getValue());
        System.out.println(PatientType.INFECTED.toString());
        return PatientType.INFECTED.getValue();
    }

    @GetMapping("/dead")
    public String deathInfo(final Model uiModel) throws IOException, InterruptedException {
        // TODO : Maybe use retry on few types of exceptions
        final List<LocationStats> stats = csvService.fetchConfirmedDeads();

        fetchAndUpdateDataForUIModel(uiModel, stats.parallelStream()
                .sorted(Comparator.comparing(LocationStats::getDeadPatientsStats)).collect(Collectors.toList()),
                PatientType.DEAD);
        return PatientType.DEAD.getValue();
    }

    @GetMapping("/recovered")
    public String recoveredInfo(final Model uiModel) throws IOException, InterruptedException {
        // TODO : Maybe use retry on few types of exceptions
        final List<LocationStats> stats = csvService.fetchConfirmedRecovered();
        fetchAndUpdateDataForUIModel(uiModel, stats.parallelStream()
                .sorted(Comparator.comparing(LocationStats::getRecoveredPatientsStats)).collect(Collectors.toList()),
                PatientType.RECOVERED);
        return PatientType.RECOVERED.getValue();
    }

    private void fetchAndUpdateDataForUIModel(final Model uiModel, final List<LocationStats> stats,
            final PatientType patientType) {
        final int currentCount = csvService.getCurrentCountByPatientType(stats, patientType);
        final int newCount = csvService.getNewCountByPatientType(stats, patientType);
        uiModel.addAttribute(patientType + UNDERSCORE + "stats", stats);
        uiModel.addAttribute(patientType + UNDERSCORE + "current_count", currentCount);
        uiModel.addAttribute(patientType + UNDERSCORE + "new_count", newCount);
    }

}
