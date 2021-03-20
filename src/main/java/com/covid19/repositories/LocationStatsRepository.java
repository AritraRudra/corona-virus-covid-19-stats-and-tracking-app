package com.covid19.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.covid19.models.LocationStats;

public interface LocationStatsRepository extends JpaRepository<LocationStats, Integer> {

    @Query(value = "SELECT MAX(updated_on) from LOCATION_STATS", nativeQuery = true)
    @Transactional(readOnly = true)
    LocalDateTime findLatestUpdatedTime();

    @Query(value = "SELECT MAX(updated_on) from INFECTED_PATIENTS_STATS", nativeQuery = true)
    @Transactional(readOnly = true)
    LocalDateTime findLatestUpdatedTimeOfInfectedPatients();

    @Query(value = "SELECT MAX(updated_on) from DEAD_PATIENTS_STATS", nativeQuery = true)
    @Transactional(readOnly = true)
    LocalDateTime findLatestUpdatedTimeOfDeadPatients();

    @Query(value = "SELECT MAX(updated_on) from RECOVERED_PATIENTS_STATS", nativeQuery = true)
    @Transactional(readOnly = true)
    LocalDateTime findLatestUpdatedTimeOfRecoveredPatients();

    @Transactional(readOnly = true)
    List<LocationStats> findByStateAndRegion(String state, String region);

}
