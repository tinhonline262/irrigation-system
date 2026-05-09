package com.irrigation_system.iot.repository;

import com.irrigation_system.iot.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<PermissionEntity, String> {
    List<PermissionEntity> findAllByNameIn(List<String> names);
    boolean existsByName(String name);
}