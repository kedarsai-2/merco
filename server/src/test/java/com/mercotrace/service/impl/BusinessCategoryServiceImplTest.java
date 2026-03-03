package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.BusinessCategory;
import com.mercotrace.repository.BusinessCategoryRepository;
import com.mercotrace.service.dto.BusinessCategoryDTO;
import com.mercotrace.service.mapper.BusinessCategoryMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessCategoryServiceImplTest {

    @Mock
    private BusinessCategoryRepository businessCategoryRepository;

    @Mock
    private BusinessCategoryMapper businessCategoryMapper;

    private BusinessCategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BusinessCategoryServiceImpl(businessCategoryRepository, businessCategoryMapper);
    }

    @Test
    void saveDelegatesToRepositoryAndMapper() {
        BusinessCategoryDTO dto = new BusinessCategoryDTO();
        dto.setCategoryName("Grains");
        BusinessCategory entity = new BusinessCategory();
        when(businessCategoryMapper.toEntity(dto)).thenReturn(entity);
        when(businessCategoryRepository.save(any(BusinessCategory.class))).thenAnswer(inv -> {
            BusinessCategory c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        BusinessCategoryDTO savedDto = new BusinessCategoryDTO();
        savedDto.setId(1L);
        when(businessCategoryMapper.toDto(any(BusinessCategory.class))).thenReturn(savedDto);

        BusinessCategoryDTO result = service.save(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(businessCategoryRepository).save(any(BusinessCategory.class));
    }

    @Test
    void findOneReturnsEmptyWhenNotFound() {
        when(businessCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<BusinessCategoryDTO> result = service.findOne(999L);

        assertThat(result).isEmpty();
    }
}
