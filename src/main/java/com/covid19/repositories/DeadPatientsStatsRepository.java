package com.covid19.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.covid19.models.DeadPatientsStats;

public interface DeadPatientsStatsRepository extends JpaRepository<DeadPatientsStats, Integer> {

}
