package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    Optional<Device> findByChipId(String chipId);
    List<Device> findByChipIdIn(List<String> chipIds);
}
