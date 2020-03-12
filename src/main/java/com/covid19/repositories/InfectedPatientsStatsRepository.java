package com.covid19.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.covid19.models.InfectedPatientsStats;

public interface InfectedPatientsStatsRepository extends JpaRepository<InfectedPatientsStats, Integer> {

}
