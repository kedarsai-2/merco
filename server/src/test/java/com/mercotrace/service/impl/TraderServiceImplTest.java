package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.Trader;
import com.mercotrace.domain.enumeration.ApprovalStatus;
import com.mercotrace.domain.enumeration.BusinessMode;
import com.mercotrace.repository.TraderRepository;
import com.mercotrace.service.dto.TraderDTO;
import com.mercotrace.service.mapper.TraderMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraderServiceImplTest {

    @Mock
    private TraderRepository traderRepository;

    @Mock
    private TraderMapper traderMapper;

    private TraderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TraderServiceImpl(traderRepository, traderMapper);
    }

    @Test
    void saveDelegatesToRepositoryAndMapper() {
        TraderDTO dto = new TraderDTO();
        dto.setBusinessName("Test Trader");
        dto.setOwnerName("Owner");
        Trader entity = new Trader();
        when(traderMapper.toEntity(dto)).thenReturn(entity);
        when(traderRepository.save(any(Trader.class))).thenAnswer(inv -> {
            Trader t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });
        TraderDTO savedDto = new TraderDTO();
        savedDto.setId(1L);
        when(traderMapper.toDto(any(Trader.class))).thenReturn(savedDto);

        TraderDTO result = service.save(dto);

        assertThat(result.getId()).isEqualTo(1L);
        verify(traderRepository).save(any(Trader.class));
    }

    @Test
    void findOneReturnsEmptyWhenNotFound() {
        when(traderRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<TraderDTO> result = service.findOne(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteCallsRepository() {
        service.delete(1L);
        verify(traderRepository).deleteById(1L);
    }
}
