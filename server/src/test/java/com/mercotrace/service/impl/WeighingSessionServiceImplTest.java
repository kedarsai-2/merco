package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercotrace.domain.WeighingSession;
import com.mercotrace.repository.WeighingSessionRepository;
import com.mercotrace.service.TraderContextService;
import com.mercotrace.service.dto.WeighingSessionCreateRequest;
import com.mercotrace.service.dto.WeighingSessionDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class WeighingSessionServiceImplTest {

    private static final long TRADER_ID = 1L;

    @Mock
    private WeighingSessionRepository weighingSessionRepository;

    @Mock
    private TraderContextService traderContextService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WeighingSessionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WeighingSessionServiceImpl(weighingSessionRepository, traderContextService, objectMapper);
    }

    @Test
    void createPersistsEntityWithTraderContext() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        WeighingSessionCreateRequest request = new WeighingSessionCreateRequest();
        request.setSessionId("s1");
        request.setLotId(10L);
        request.setBidNumber(1);
        request.setBuyerMark("M1");
        request.setTotalBags(5);
        request.setOriginalWeight(BigDecimal.valueOf(100));
        request.setNetWeight(BigDecimal.valueOf(98));
        when(weighingSessionRepository.save(any(WeighingSession.class))).thenAnswer(inv -> {
            WeighingSession e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        WeighingSessionDTO result = service.create(request);

        assertThat(result.getSessionId()).isEqualTo("s1");
        assertThat(result.getLotId()).isEqualTo(10L);
        assertThat(result.getBidNumber()).isEqualTo(1);
        assertThat(result.getNetWeight()).isEqualByComparingTo(BigDecimal.valueOf(98));
        verify(weighingSessionRepository).save(any(WeighingSession.class));
    }

    @Test
    void listDelegatesToRepository() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        WeighingSession entity = new WeighingSession();
        entity.setId(1L);
        entity.setTraderId(TRADER_ID);
        entity.setSessionId("s1");
        entity.setBidNumber(1);
        when(weighingSessionRepository.findAllByTraderIdOrderByCreatedDateDesc(TRADER_ID, PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(entity)));

        Page<WeighingSessionDTO> page = service.list(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getSessionId()).isEqualTo("s1");
    }

    @Test
    void getByIdReturnsEmptyWhenNotFound() {
        when(weighingSessionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<WeighingSessionDTO> result = service.getById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void getByBidNumberReturnsFirstMatchForTrader() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        WeighingSession entity = new WeighingSession();
        entity.setId(1L);
        entity.setTraderId(TRADER_ID);
        entity.setBidNumber(5);
        when(weighingSessionRepository.findAllByBidNumber(5)).thenReturn(List.of(entity));

        Optional<WeighingSessionDTO> result = service.getByBidNumber(5);

        assertThat(result).isPresent();
        assertThat(result.get().getBidNumber()).isEqualTo(5);
    }
}
