package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.SoilSensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface SoilSensorReadingRepository extends JpaRepository<SoilSensorReading, String> {
    Optional<SoilSensorReading> findFirstByDeviceIdOrderByRecordedAtDesc(String deviceId);

    List<SoilSensorReading> findByDeviceIdAndRecordedAtBetweenOrderByRecordedAtAsc(String deviceId, Instant startDate, Instant endDate);

    @Query("SELECT s FROM SoilSensorReading s WHERE s.device.id = :deviceId AND s.recordedAt >= :startDate AND s.recordedAt < :endDate ORDER BY s.recordedAt ASC")
    List<SoilSensorReading> findRawHistory(@Param("deviceId") String deviceId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Query("SELECT coalesce(MIN(s.moisturePercent), 0), coalesce(MAX(s.moisturePercent), 0), coalesce(AVG(s.moisturePercent), 0) FROM SoilSensorReading s WHERE s.device.id = :deviceId AND s.recordedAt >= :startDate")
    Object[] getStatsByDeviceIdAndRecordedAtAfter(@Param("deviceId") String deviceId, @Param("startDate") Instant startDate);

     @Modifying
    @Query("DELETE FROM SoilSensorReading s WHERE s.recordedAt < :date")
    int deleteByCreatedAtBefore(@Param("date") LocalDateTime date);
}
