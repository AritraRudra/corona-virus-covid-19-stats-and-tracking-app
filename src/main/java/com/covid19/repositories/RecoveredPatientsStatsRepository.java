package com.covid19.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.covid19.models.RecoveredPatientsStats;

public interface RecoveredPatientsStatsRepository extends JpaRepository<RecoveredPatientsStats, Integer> {

}
