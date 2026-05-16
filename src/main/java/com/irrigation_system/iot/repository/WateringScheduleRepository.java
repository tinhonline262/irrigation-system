package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.WateringSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WateringScheduleRepository extends JpaRepository<WateringSchedule, String> {

    List<WateringSchedule> findByDevice_IdOrderByNextRunAtAsc(String deviceId);

    Optional<WateringSchedule> findByIdAndDevice_Id(String id, String deviceId);

    @EntityGraph(attributePaths = "device")
    List<WateringSchedule> findByEnabledTrueAndNextRunAtLessThanEqual(Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "device")
    Optional<WateringSchedule> findByIdForUpdate(String id);
}
