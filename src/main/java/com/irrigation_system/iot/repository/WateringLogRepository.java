package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.WateringLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface WateringLogRepository extends JpaRepository<WateringLog, String> {

    @Query("select coalesce(sum(w.waterAmountMl), 0) from WateringLog w where w.device.id = :deviceId and w.startedAt >= :startOfDay")
    Float sumWaterAmountMlByDeviceIdAndStartedAtAfter(@Param("deviceId") String deviceId,
                                                     @Param("startOfDay") Instant startOfDay);

    @Query(value = "select date(started_at) as log_date, coalesce(sum(water_amount_ml), 0) as total_water_amount_ml, count(*) as watering_count " +
            "from watering_log where device_id = :deviceId group by date(started_at) order by date(started_at) desc", nativeQuery = true)
    List<Object[]> findDailyWateringStatsByDeviceId(@Param("deviceId") String deviceId);

    @EntityGraph(attributePaths = {"device", "triggeredBy"})
    Optional<WateringLog> findFirstByDevice_IdAndTriggerTypeAndEndedAtIsNullOrderByStartedAtDesc(String deviceId, String triggerType);

    @EntityGraph(attributePaths = {"device", "triggeredBy"})
    Optional<WateringLog> findFirstByDevice_IdAndEndedAtIsNullOrderByStartedAtDesc(String deviceId);

    @Query("SELECT w FROM WateringLog w LEFT JOIN FETCH w.device LEFT JOIN FETCH w.triggeredBy WHERE w.device.id = :deviceId ORDER BY w.startedAt DESC")
    Stream<WateringLog> findByDevice_IdOrderByStartedAtDesc(@Param("deviceId") String deviceId);

    @EntityGraph(attributePaths = {"device", "triggeredBy"})
    Page<WateringLog> findByDevice_IdOrderByStartedAtDesc(String deviceId, Pageable pageable);
}
