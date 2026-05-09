package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.AirSensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AirSensorReadingRepository extends JpaRepository<AirSensorReading, String> {

    @Modifying
    @Query("DELETE FROM AirSensorReading a WHERE a.recordedAt < :date")
    int deleteByCreatedAtBefore(@Param("date") LocalDateTime date);
}
