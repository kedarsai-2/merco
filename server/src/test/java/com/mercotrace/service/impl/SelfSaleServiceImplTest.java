package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.Lot;
import com.mercotrace.repository.*;
import com.mercotrace.service.TraderContextService;
import com.mercotrace.service.dto.SelfSaleDTOs.ClosureDTO;
import com.mercotrace.service.dto.SelfSaleDTOs.CreateClosureRequestDTO;
import com.mercotrace.service.dto.SelfSaleDTOs.OpenLotDTO;
import com.mercotrace.service.mapper.SelfSaleClosureMapper;
import java.math.BigDecimal;
import java.util.Collections;
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
class SelfSaleServiceImplTest {

    private static final long TRADER_ID = 101L;

    @Mock
    private TraderContextService traderContextService;

    @Mock
    private SelfSaleClosureRepository selfSaleClosureRepository;

    @Mock
    private LotRepository lotRepository;

    @Mock
    private SellerInVehicleRepository sellerInVehicleRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private CommodityRepository commodityRepository;

    @Mock
    private SelfSaleClosureMapper selfSaleClosureMapper;

    private SelfSaleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SelfSaleServiceImpl(
            traderContextService,
            selfSaleClosureRepository,
            lotRepository,
            sellerInVehicleRepository,
            vehicleRepository,
            contactRepository,
            commodityRepository,
            selfSaleClosureMapper
        );
    }

    @Test
    void getOpenLotsReturnsEmptyPageWhenNoLots() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        when(selfSaleClosureRepository.findClosedLotIdsByTraderId(TRADER_ID)).thenReturn(Set.of());
        when(lotRepository.findAllByTraderId(TRADER_ID, PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of()));

        Page<OpenLotDTO> result = service.getOpenLots(PageRequest.of(0, 10), null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void getClosuresReturnsEmptyPageWhenNone() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        when(selfSaleClosureRepository.findByTraderIdAndIsDeletedFalse(TRADER_ID, PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of()));

        Page<ClosureDTO> result = service.getClosures(PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void createClosureThrowsWhenLotNotFound() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        when(lotRepository.findById(999L)).thenReturn(Optional.empty());

        CreateClosureRequestDTO request = new CreateClosureRequestDTO();
        request.setLotId(999L);
        request.setRate(BigDecimal.TEN);

        assertThatThrownBy(() -> service.createClosure(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Lot not found");
    }

    @Test
    void createClosureThrowsWhenLotAlreadyClosed() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        Lot lot = new Lot();
        lot.setId(1L);
        lot.setSellerVehicleId(10L);
        lot.setBagCount(5);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(selfSaleClosureRepository.existsByLotIdAndTraderIdAndIsDeletedFalse(1L, TRADER_ID)).thenReturn(true);

        CreateClosureRequestDTO request = new CreateClosureRequestDTO();
        request.setLotId(1L);
        request.setRate(BigDecimal.TEN);

        assertThatThrownBy(() -> service.createClosure(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already closed");
    }
}
