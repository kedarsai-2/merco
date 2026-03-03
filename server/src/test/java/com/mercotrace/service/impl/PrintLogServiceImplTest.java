package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.PrintLog;
import com.mercotrace.repository.PrintLogRepository;
import com.mercotrace.service.TraderContextService;
import com.mercotrace.service.dto.PrintLogCreateRequest;
import com.mercotrace.service.dto.PrintLogDTO;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class PrintLogServiceImplTest {

    private static final long TRADER_ID = 1L;

    @Mock
    private PrintLogRepository printLogRepository;

    @Mock
    private TraderContextService traderContextService;

    private PrintLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PrintLogServiceImpl(printLogRepository, traderContextService);
    }

    @Test
    void createPersistsEntityWithTraderContext() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        PrintLogCreateRequest request = new PrintLogCreateRequest();
        request.setReferenceType("BILL");
        request.setReferenceId("100");
        request.setPrintType("A4_PORTRAIT");
        request.setPrintedAt(Instant.now());
        when(printLogRepository.save(any(PrintLog.class))).thenAnswer(inv -> {
            PrintLog e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        PrintLogDTO result = service.create(request);

        assertThat(result.getReferenceType()).isEqualTo("BILL");
        assertThat(result.getReferenceId()).isEqualTo("100");
        assertThat(result.getPrintType()).isEqualTo("A4_PORTRAIT");
        verify(printLogRepository).save(any(PrintLog.class));
    }

    @Test
    void createUsesNowWhenPrintedAtNull() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        PrintLogCreateRequest request = new PrintLogCreateRequest();
        request.setReferenceType("CDN");
        request.setReferenceId("1");
        request.setPrintType("THERMAL_80MM");
        when(printLogRepository.save(any(PrintLog.class))).thenAnswer(inv -> {
            PrintLog e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        PrintLogDTO result = service.create(request);

        assertThat(result.getPrintedAt()).isNotNull();
    }

    @Test
    void listDelegatesToRepository() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        PrintLog entity = new PrintLog();
        entity.setId(1L);
        entity.setTraderId(TRADER_ID);
        entity.setReferenceType("BILL");
        entity.setPrintedAt(Instant.now());
        when(printLogRepository.findAllByTraderIdOrderByPrintedAtDesc(TRADER_ID, PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(entity)));

        Page<PrintLogDTO> page = service.list(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getReferenceType()).isEqualTo("BILL");
    }
}
