package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.Role;
import com.mercotrace.repository.PermissionRepository;
import com.mercotrace.repository.RoleRepository;
import com.mercotrace.service.dto.RoleDTO;
import com.mercotrace.service.mapper.RoleMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionRepository permissionRepository;

    private RoleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RoleServiceImpl(roleRepository, roleMapper, permissionRepository);
    }

    @Test
    void saveDelegatesToRepositoryAndMapper() {
        RoleDTO dto = new RoleDTO();
        dto.setRoleName("ROLE_WRITER");
        Role entity = new Role();
        when(roleMapper.toEntity(dto)).thenReturn(entity);
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        RoleDTO savedDto = new RoleDTO();
        savedDto.setId(1L);
        when(roleMapper.toDto(any(Role.class))).thenReturn(savedDto);

        RoleDTO result = service.save(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void findOneReturnsEmptyWhenNotFound() {
        lenient().when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<RoleDTO> result = service.findOne(999L);

        assertThat(result).isEmpty();
    }
}
