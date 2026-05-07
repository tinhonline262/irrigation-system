package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.SensorDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorDataEntity, Long> {
}
