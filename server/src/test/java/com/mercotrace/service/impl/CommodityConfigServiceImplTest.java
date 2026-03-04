package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mercotrace.repository.CommodityConfigRepository;
import com.mercotrace.repository.CommodityRepository;
import com.mercotrace.repository.DeductionRuleRepository;
import com.mercotrace.repository.DynamicChargeRepository;
import com.mercotrace.repository.HamaliSlabRepository;
import com.mercotrace.service.dto.FullCommodityConfigDTO;
import com.mercotrace.service.errors.BadRequestException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommodityConfigServiceImplTest {

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private CommodityConfigRepository commodityConfigRepository;

    @Mock
    private DeductionRuleRepository deductionRuleRepository;

    @Mock
    private HamaliSlabRepository hamaliSlabRepository;

    @Mock
    private DynamicChargeRepository dynamicChargeRepository;

    @Mock
    private EntityManager entityManager;

    private CommodityConfigServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CommodityConfigServiceImpl(
            commodityRepository,
            commodityConfigRepository,
            deductionRuleRepository,
            hamaliSlabRepository,
            dynamicChargeRepository
        );
        // Inject EntityManager via reflection if needed; for getFullConfig tests we don't need it
    }

    @Test
    void getFullConfigThrowsWhenCommodityNotFound() {
        when(commodityRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.getFullConfig(999L))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getFullConfigReturnsDtoWithDefaultConfigWhenNoConfigExists() {
        when(commodityRepository.existsById(1L)).thenReturn(true);
        when(commodityConfigRepository.findOneByCommodityId(1L)).thenReturn(Optional.empty());
        when(deductionRuleRepository.findAllByCommodityIdOrderByMinWeight(1L)).thenReturn(List.of());
        when(hamaliSlabRepository.findAllByCommodityIdOrderByThresholdWeight(1L)).thenReturn(List.of());
        when(dynamicChargeRepository.findAllByCommodityId(1L)).thenReturn(List.of());

        FullCommodityConfigDTO result = service.getFullConfig(1L);

        assertThat(result.getCommodityId()).isEqualTo(1L);
        assertThat(result.getConfig()).isNotNull();
        assertThat(result.getConfig().getCommodityId()).isEqualTo(1L);
        assertThat(result.getConfig().getRatePerUnit()).isEqualTo(0D);
        assertThat(result.getConfig().getGovtDeductionEnabled()).isFalse();
        assertThat(result.getDeductionRules()).isEmpty();
        assertThat(result.getHamaliSlabs()).isEmpty();
        assertThat(result.getDynamicCharges()).isEmpty();
    }

    @Test
    void getAllFullConfigsReturnsEmptyWhenNoCommodities() {
        when(commodityRepository.findAll()).thenReturn(List.of());

        List<FullCommodityConfigDTO> result = service.getAllFullConfigs();

        assertThat(result).isEmpty();
    }

    @Test
    void saveFullConfigThrowsWhenCommodityIdNull() {
        FullCommodityConfigDTO dto = new FullCommodityConfigDTO();
        dto.setCommodityId(null);

        assertThatThrownBy(() -> service.saveFullConfig(dto))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void saveFullConfigThrowsWhenCommodityNotFound() {
        FullCommodityConfigDTO dto = new FullCommodityConfigDTO();
        dto.setCommodityId(999L);
        when(commodityRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.saveFullConfig(dto))
            .isInstanceOf(BadRequestException.class);
    }
}
