/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.covid19.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.covid19.models.LocationStats;

public interface LocationStatsRepository extends JpaRepository<LocationStats, Integer> {

    @Query(value = "SELECT updated_on FROM LOCATION_STATS WHERE updated_on = (SELECT MAX(updated_on) from LOCATION_STATS)", nativeQuery = true)
    @Transactional(readOnly = true)
    LocalDateTime findLatestUpdatedTime();

    @Query(value = "SELECT updated_on FROM INFECTED_PATIENTS_STATS WHERE updated_on = (SELECT MAX(updated_on) from INFECTED_PATIENTS_STATS)", nativeQuery = true)
    @Transactional(readOnly = true)
    LocalDateTime findLatestUpdatedTimeOfInfectedPatients();

    @Query(value = "SELECT updated_on FROM DEAD_PATIENTS_STATS WHERE updated_on = (SELECT MAX(updated_on) from DEAD_PATIENTS_STATS)", nativeQuery = true)
    @Transactional(readOnly = true)
    LocalDateTime findLatestUpdatedTimeOfDeadPatients();

    @Query(value = "SELECT updated_on FROM RECOVERED_PATIENTS_STATS WHERE updated_on = (SELECT MAX(updated_on) from RECOVERED_PATIENTS_STATS)", nativeQuery = true)
    @Transactional(readOnly = true)
    LocalDateTime findLatestUpdatedTimeOfRecoveredPatients();

    @Transactional(readOnly = true)
    List<LocationStats> findByStateAndRegion(String state, String region);

}
