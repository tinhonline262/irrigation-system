package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.SoilSensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoilSensorReadingRepository extends JpaRepository<SoilSensorReading, String> {
}
