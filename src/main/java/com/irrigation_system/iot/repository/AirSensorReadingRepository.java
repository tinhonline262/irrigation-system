package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.AirSensorReading;
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
public interface AirSensorReadingRepository extends JpaRepository<AirSensorReading, String> {
    Optional<AirSensorReading> findFirstByDeviceIdOrderByRecordedAtDesc(String deviceId);

    List<AirSensorReading> findByDeviceIdAndRecordedAtBetweenOrderByRecordedAtAsc(String deviceId, Instant startDate, Instant endDate);

    @Query("SELECT a FROM AirSensorReading a WHERE a.device.id = :deviceId AND a.recordedAt >= :startDate AND a.recordedAt < :endDate ORDER BY a.recordedAt ASC")
    List<AirSensorReading> findRawHistory(@Param("deviceId") String deviceId, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    @Modifying
    @Query("DELETE FROM AirSensorReading a WHERE a.recordedAt < :date")
    int deleteByCreatedAtBefore(@Param("date") LocalDateTime date);
}
