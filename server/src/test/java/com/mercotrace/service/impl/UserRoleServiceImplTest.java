package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.Role;
import com.mercotrace.domain.User;
import com.mercotrace.domain.UserRole;
import com.mercotrace.repository.RoleRepository;
import com.mercotrace.repository.UserRepository;
import com.mercotrace.repository.UserRoleRepository;
import com.mercotrace.service.dto.UserRoleDTO;
import com.mercotrace.service.mapper.UserRoleMapper;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    private static final long USER_ID = 10L;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleMapper userRoleMapper;

    private UserRoleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserRoleServiceImpl(userRoleRepository, userRepository, roleRepository, userRoleMapper);
    }

    @Test
    void findAllDelegatesToRepositoryAndMapper() {
        UserRole entity = new UserRole();
        entity.setId(1L);
        PageRequest pageable = PageRequest.of(0, 20);
        when(userRoleRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        UserRoleDTO dto = new UserRoleDTO();
        dto.setId(1L);
        when(userRoleMapper.toDto(entity)).thenReturn(dto);

        Page<UserRoleDTO> page = service.findAll(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(1L);
        verify(userRoleRepository).findAll(pageable);
        verify(userRoleMapper).toDto(entity);
    }

    @Test
    void findByUserIdDelegatesToRepositoryAndMapper() {
        UserRole entity = new UserRole();
        entity.setId(5L);
        when(userRoleRepository.findByUserId(USER_ID)).thenReturn(List.of(entity));
        UserRoleDTO dto = new UserRoleDTO();
        dto.setId(5L);
        when(userRoleMapper.toDto(entity)).thenReturn(dto);

        List<UserRoleDTO> result = service.findByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);
        verify(userRoleRepository).findByUserId(USER_ID);
        verify(userRoleMapper).toDto(entity);
    }

    @Test
    void setRolesForUserReplacesExistingMappings() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        Role role1 = new Role();
        role1.setId(101L);
        Role role2 = new Role();
        role2.setId(102L);
        when(roleRepository.findAllById(Set.of(101L, 102L))).thenReturn(List.of(role1, role2));

        service.setRolesForUser(USER_ID, Set.of(101L, 102L));

        verify(userRepository).findById(USER_ID);
        verify(userRoleRepository).deleteByUserId(USER_ID);
        verify(userRoleRepository, times(2)).save(any(UserRole.class));
    }

    @Test
    void setRolesForUserClearsMappingsWhenEmptyRoleSet() {
        User user = new User();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        service.setRolesForUser(USER_ID, Set.of());

        verify(userRepository).findById(USER_ID);
        verify(userRoleRepository).deleteByUserId(USER_ID);
        verify(userRoleRepository, times(0)).save(any(UserRole.class));
    }
}

