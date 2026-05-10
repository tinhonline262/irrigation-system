package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.WateringLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    Optional<WateringLog> findFirstByDevice_IdAndTriggerTypeAndEndedAtIsNullOrderByStartedAtDesc(String deviceId, String triggerType);

    Optional<WateringLog> findFirstByDevice_IdAndEndedAtIsNullOrderByStartedAtDesc(String deviceId);

    Stream<WateringLog> findByDevice_IdOrderByStartedAtDesc(String deviceId);

    Page<WateringLog> findByDevice_IdOrderByStartedAtDesc(String deviceId, Pageable pageable);
}
