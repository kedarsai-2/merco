package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.Permission;
import com.mercotrace.repository.PermissionRepository;
import com.mercotrace.service.dto.PermissionDTO;
import com.mercotrace.service.mapper.PermissionMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    private PermissionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PermissionServiceImpl(permissionRepository, permissionMapper);
    }

    @Test
    void saveDelegatesToRepositoryAndMapper() {
        PermissionDTO dto = new PermissionDTO();
        dto.setPermissionName("PERM_VIEW");
        Permission entity = new Permission();
        when(permissionMapper.toEntity(dto)).thenReturn(entity);
        when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> {
            Permission p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        PermissionDTO savedDto = new PermissionDTO();
        savedDto.setId(1L);
        when(permissionMapper.toDto(any(Permission.class))).thenReturn(savedDto);

        PermissionDTO result = service.save(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    void findOneReturnsEmptyWhenNotFound() {
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<PermissionDTO> result = service.findOne(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteCallsRepository() {
        service.delete(1L);
        verify(permissionRepository).deleteById(1L);
    }
}
