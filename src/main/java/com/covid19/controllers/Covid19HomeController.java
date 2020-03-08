package com.covid19.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.covid19.models.LocationStats;
import com.covid19.models.PatientType;
import com.covid19.services.CovidCsvService;

@Controller
public class Covid19HomeController {

	private static final String UNDERSCORE = "_";

	@Autowired
	private CovidCsvService csvService;

	@GetMapping("/")
	public String homePageInfo(final Model uiModel) throws IOException, InterruptedException {
		// TODO : Maybe use retry on few types of exceptions
		List<LocationStats> stats = csvService.fetchConfirmedInfected();
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
		fetchAndUpdateDataForUIModel(uiModel, stats, PatientType.INFECTED);
		return PatientType.INFECTED.name();
	}

	@GetMapping("/dead")
	public String deathInfo(final Model uiModel) throws IOException, InterruptedException {
		// TODO : Maybe use retry on few types of exceptions
		final List<LocationStats> stats = csvService.fetchConfirmedDeads();
		fetchAndUpdateDataForUIModel(uiModel, stats, PatientType.DEAD);
		return PatientType.DEAD.name();
	}

	@GetMapping("/recovered")
	public String recoveredInfo(final Model uiModel) throws IOException, InterruptedException {
		// TODO : Maybe use retry on few types of exceptions
		final List<LocationStats> stats = csvService.fetchConfirmedRecovered();
		fetchAndUpdateDataForUIModel(uiModel, stats, PatientType.RECOVERED);
		return PatientType.RECOVERED.name();
	}

	private void fetchAndUpdateDataForUIModel(final Model uiModel, final List<LocationStats> stats, final PatientType patientType) {
		final int currentCount = getCurrentCountByPatientType(stats, patientType);
		final int newCount = getNewCountByPatientType(stats, patientType);

		uiModel.addAttribute(patientType + UNDERSCORE + "stats", stats);
		uiModel.addAttribute(patientType + UNDERSCORE + "current_count", currentCount);
		uiModel.addAttribute(patientType + UNDERSCORE + "new_count", newCount);
	}

	@SuppressWarnings("preview")
	private int getCurrentCountByPatientType(final List<LocationStats> stats, final PatientType patientType) {
		return switch (patientType) {
		case DEAD -> stats.stream().mapToInt(patient -> patient.getDeadPatientsStats().getLatestCount()).sum();
		case INFECTED -> stats.stream().mapToInt(patient -> patient.getInfectedPatientsStats().getLatestCount()).sum();
		case RECOVERED -> stats.stream().mapToInt(patient -> patient.getRecoveredPatientsStats().getLatestCount()).sum();
		default -> throw new IllegalArgumentException("Unexpected value: " + patientType);
		};
	}

	@SuppressWarnings("preview")
	private int getNewCountByPatientType(final List<LocationStats> stats, final PatientType patientType) {
		return switch (patientType) {
		case DEAD -> stats.stream().mapToInt(newPatient -> newPatient.getDeadPatientsStats().getDifferenceSincePreviousDay()).sum();
		case INFECTED -> stats.stream().mapToInt(newPatient -> newPatient.getInfectedPatientsStats().getDifferenceSincePreviousDay()).sum();
		case RECOVERED -> stats.stream().mapToInt(newPatient -> newPatient.getRecoveredPatientsStats().getDifferenceSincePreviousDay()).sum();
		default -> throw new IllegalArgumentException("Unexpected value: " + patientType);
		};
	}
}
