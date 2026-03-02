package com.mercotrace.service.mapper;

import com.mercotrace.domain.Role;
import com.mercotrace.domain.User;
import com.mercotrace.domain.UserRole;
import com.mercotrace.service.dto.UserRoleDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for {@link UserRole} and its DTO {@link UserRoleDTO}.
 */
@Mapper(componentModel = "spring")
public interface UserRoleMapper extends EntityMapper<UserRoleDTO, UserRole> {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "assignedBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdDate")
    UserRoleDTO toDto(UserRole entity);

    @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId")
    @Mapping(target = "role", source = "roleId", qualifiedByName = "roleFromId")
    UserRole toEntity(UserRoleDTO dto);

    @Named("userFromId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    User toUser(Long id);

    @Named("roleFromId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    Role toRole(Long id);
}

