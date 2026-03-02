package com.mercotrace.service.mapper;

import com.mercotrace.domain.Permission;
import com.mercotrace.domain.Role;
import com.mercotrace.service.dto.PermissionDTO;
import com.mercotrace.service.dto.RoleDTO;
import com.mercotrace.service.dto.RoleDTO.ModulePermissionEntry;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Role} and its DTO {@link RoleDTO}.
 */
@Mapper(componentModel = "spring")
public interface RoleMapper extends EntityMapper<RoleDTO, Role> {
    @Mapping(target = "permissions", source = "permissions", qualifiedByName = "permissionIdSet")
    @Mapping(target = "modulePermissions", expression = "java(toModulePermissionsMap(s.getModulePermissions()))")
    RoleDTO toDto(Role s);

    @Mapping(target = "removePermission", ignore = true)
    @Mapping(target = "modulePermissions", expression = "java(toModulePermissionsJson(roleDTO.getModulePermissions()))")
    Role toEntity(RoleDTO roleDTO);

    @Named("permissionId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    PermissionDTO toDtoPermissionId(Permission permission);

    @Named("permissionIdSet")
    default Set<PermissionDTO> toDtoPermissionIdSet(Set<Permission> permission) {
        return permission.stream().map(this::toDtoPermissionId).collect(Collectors.toSet());
    }

    default Map<String, ModulePermissionEntry> toModulePermissionsMap(String json) {
        return ModulePermissionsJsonMapper.fromJson(json);
    }

    default String toModulePermissionsJson(Map<String, ModulePermissionEntry> value) {
        return ModulePermissionsJsonMapper.toJson(value);
    }
}
