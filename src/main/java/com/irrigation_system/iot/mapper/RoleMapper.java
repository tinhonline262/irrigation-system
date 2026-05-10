package com.irrigation_system.iot.mapper;

import com.irrigation_system.iot.dto.RoleDTO;
import com.irrigation_system.iot.entity.PermissionEntity;
import com.irrigation_system.iot.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    default List<String> map(Set<RoleEntity> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());
    }

    @Mapping(source = "permissions", target = "permissions", qualifiedByName = "mapPermissions")
    RoleDTO mapToDTO(RoleEntity entity);

    @Named("mapPermissions")
    default List<String> mapPermissions(Set<PermissionEntity> permissions) {
        if (permissions == null) return List.of();
        return permissions.stream()
                .map(PermissionEntity::getName)
                .collect(Collectors.toList());
    }
}
