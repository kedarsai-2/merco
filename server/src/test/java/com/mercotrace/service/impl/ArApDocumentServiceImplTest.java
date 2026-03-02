package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.ArApDocument;
import com.mercotrace.domain.Contact;
import com.mercotrace.domain.enumeration.ArApStatus;
import com.mercotrace.domain.enumeration.ArApType;
import com.mercotrace.repository.ArApDocumentRepository;
import com.mercotrace.service.TraderContextService;
import com.mercotrace.service.dto.ArApDocumentDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * Unit tests for {@link ArApDocumentServiceImpl}.
 * Financial Reports module: AR/AP documents for aging reports.
 */
@ExtendWith(MockitoExtension.class)
class ArApDocumentServiceImplTest {

    private static final long TRADER_ID = 101L;

    @Mock
    private ArApDocumentRepository arApDocumentRepository;

    @Mock
    private TraderContextService traderContextService;

    private ArApDocumentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ArApDocumentServiceImpl(arApDocumentRepository, traderContextService);
    }

    @Test
    void getPageDelegatesToRepositoryAndMapsToDto() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        Contact contact = new Contact();
        contact.setId(50L);
        contact.setName("Vijay Traders");
        ArApDocument doc = new ArApDocument();
        doc.setId(1L);
        doc.setTraderId(TRADER_ID);
        doc.setContact(contact);
        doc.setLedgerId(200L);
        doc.setType(ArApType.AR);
        doc.setReferenceVoucherId(10L);
        doc.setReferenceNumber("SB-001");
        doc.setOriginalAmount(BigDecimal.valueOf(50000));
        doc.setOutstandingBalance(BigDecimal.valueOf(25000));
        doc.setStatus(ArApStatus.OPEN);
        doc.setDocumentDate(LocalDate.of(2025, 1, 15));
        doc.setCreatedDate(Instant.now());
        PageRequest pageable = PageRequest.of(0, 100);
        when(arApDocumentRepository.findAllByTraderIdAndTypeAndStatus(
            eq(TRADER_ID), eq((ArApType) null), eq((ArApStatus) null), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(doc), pageable, 1));

        Page<ArApDocumentDTO> page = service.getPage(pageable, null, null);

        assertThat(page.getContent()).hasSize(1);
        ArApDocumentDTO dto = page.getContent().get(0);
        assertThat(dto.getDocumentId()).isEqualTo("1");
        assertThat(dto.getTraderId()).isEqualTo("101");
        assertThat(dto.getContactId()).isEqualTo("50");
        assertThat(dto.getContactName()).isEqualTo("Vijay Traders");
        assertThat(dto.getLedgerId()).isEqualTo("200");
        assertThat(dto.getType()).isEqualTo(ArApType.AR);
        assertThat(dto.getReferenceNumber()).isEqualTo("SB-001");
        assertThat(dto.getOutstandingBalance()).isEqualByComparingTo(BigDecimal.valueOf(25000));
        assertThat(dto.getStatus()).isEqualTo(ArApStatus.OPEN);
        assertThat(dto.getDocumentDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        verify(arApDocumentRepository).findAllByTraderIdAndTypeAndStatus(eq(TRADER_ID), eq((ArApType) null), eq((ArApStatus) null), eq(pageable));
    }

    @Test
    void getPageWithTypeAndStatusFiltersDelegatesCorrectly() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        PageRequest pageable = PageRequest.of(0, 20);
        when(arApDocumentRepository.findAllByTraderIdAndTypeAndStatus(
            eq(TRADER_ID), eq(ArApType.AP), eq(ArApStatus.OPEN), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ArApDocumentDTO> page = service.getPage(pageable, ArApType.AP, ArApStatus.OPEN);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
        verify(arApDocumentRepository).findAllByTraderIdAndTypeAndStatus(eq(TRADER_ID), eq(ArApType.AP), eq(ArApStatus.OPEN), eq(pageable));
    }

    @Test
    void getPageReturnsEmptyWhenNoDocuments() {
        when(traderContextService.getCurrentTraderId()).thenReturn(TRADER_ID);
        PageRequest pageable = PageRequest.of(0, 100);
        when(arApDocumentRepository.findAllByTraderIdAndTypeAndStatus(
            eq(TRADER_ID), eq((ArApType) null), eq((ArApStatus) null), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ArApDocumentDTO> page = service.getPage(pageable, null, null);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }
}
