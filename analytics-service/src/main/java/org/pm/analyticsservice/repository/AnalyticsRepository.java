package org.pm.analyticsservice.repository;
import org.pm.analyticsservice.model.AnalyticsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsData, Long> {
    Optional<AnalyticsData> findByDate(LocalDate date);
    List<AnalyticsData> findByDateBetween(LocalDate fromDate, LocalDate toDate);
}
