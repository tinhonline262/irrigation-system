package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.WateringSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WateringScheduleRepository extends JpaRepository<WateringSchedule, String> {

    List<WateringSchedule> findByDevice_IdOrderByNextRunAtAsc(String deviceId);

    Optional<WateringSchedule> findByIdAndDevice_Id(String id, String deviceId);
}

