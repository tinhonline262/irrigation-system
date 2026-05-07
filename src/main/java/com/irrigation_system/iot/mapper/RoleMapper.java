package com.irrigation_system.iot.mapper;

import com.irrigation_system.iot.entity.RoleEntity;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface RoleMapper {

    default List<String> map(Set<RoleEntity> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());
    }
}
