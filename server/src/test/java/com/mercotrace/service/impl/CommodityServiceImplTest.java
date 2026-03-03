package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.Commodity;
import com.mercotrace.repository.CommodityConfigRepository;
import com.mercotrace.repository.CommodityRepository;
import com.mercotrace.repository.DeductionRuleRepository;
import com.mercotrace.repository.DynamicChargeRepository;
import com.mercotrace.repository.HamaliSlabRepository;
import com.mercotrace.service.dto.CommodityDTO;
import com.mercotrace.service.mapper.CommodityMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommodityServiceImplTest {

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private CommodityMapper commodityMapper;

    @Mock
    private CommodityConfigRepository commodityConfigRepository;

    @Mock
    private DeductionRuleRepository deductionRuleRepository;

    @Mock
    private HamaliSlabRepository hamaliSlabRepository;

    @Mock
    private DynamicChargeRepository dynamicChargeRepository;

    private CommodityServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CommodityServiceImpl(
            commodityRepository,
            commodityMapper,
            commodityConfigRepository,
            deductionRuleRepository,
            hamaliSlabRepository,
            dynamicChargeRepository
        );
    }

    @Test
    void saveSetsCreatedAtWhenNull() {
        CommodityDTO dto = new CommodityDTO();
        dto.setCommodityName("Wheat");
        dto.setTraderId(1L);
        Commodity entity = new Commodity();
        when(commodityMapper.toEntity(dto)).thenReturn(entity);
        when(commodityRepository.save(any(Commodity.class))).thenAnswer(inv -> {
            Commodity c = inv.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(Instant.now());
            return c;
        });
        CommodityDTO savedDto = new CommodityDTO();
        savedDto.setId(1L);
        savedDto.setCommodityName("Wheat");
        when(commodityMapper.toDto(any(Commodity.class))).thenReturn(savedDto);

        CommodityDTO result = service.save(dto);

        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(commodityRepository).save(any(Commodity.class));
    }

    @Test
    void findOneReturnsEmptyWhenNotFound() {
        when(commodityRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<CommodityDTO> result = service.findOne(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findOneReturnsDtoWhenFound() {
        Commodity entity = new Commodity();
        entity.setId(1L);
        entity.setCommodityName("Rice");
        entity.setTraderId(1L);
        CommodityDTO dto = new CommodityDTO();
        dto.setId(1L);
        dto.setCommodityName("Rice");
        when(commodityRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(commodityMapper.toDto(entity)).thenReturn(dto);

        Optional<CommodityDTO> result = service.findOne(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCommodityName()).isEqualTo("Rice");
    }

    @Test
    void deleteRemovesConfigAndEntity() {
        service.delete(1L);

        verify(commodityConfigRepository).deleteByCommodityId(1L);
        verify(deductionRuleRepository).deleteByCommodityId(1L);
        verify(hamaliSlabRepository).deleteByCommodityId(1L);
        verify(dynamicChargeRepository).deleteByCommodityId(1L);
        verify(commodityRepository).deleteById(1L);
    }

    @Test
    void findAllByTraderDelegatesToRepository() {
        Commodity c = new Commodity();
        c.setId(1L);
        c.setCommodityName("Wheat");
        c.setTraderId(1L);
        when(commodityRepository.findAllByTraderId(1L)).thenReturn(List.of(c));
        CommodityDTO dto = new CommodityDTO();
        dto.setId(1L);
        when(commodityMapper.toDto(c)).thenReturn(dto);

        List<CommodityDTO> result = service.findAllByTrader(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
}
