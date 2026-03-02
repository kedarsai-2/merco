package com.mercotrace.web.rest;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercotrace.IntegrationTest;
import com.mercotrace.domain.ChartOfAccount;
import com.mercotrace.repository.ChartOfAccountRepository;
import com.mercotrace.service.dto.ChartOfAccountCreateRequest;
import com.mercotrace.service.dto.ChartOfAccountUpdateRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link ChartOfAccountResource}. Uses trader 101 (DefaultTraderContextServiceImpl).
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ChartOfAccountResourceIT {

    private static final String BASE_URL = "/api/chart-of-accounts";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc restChartOfAccountMockMvc;

    @Autowired
    private ChartOfAccountRepository chartOfAccountRepository;

    private ChartOfAccount createdLedger;

    @AfterEach
    void cleanup() {
        if (createdLedger != null && createdLedger.getId() != null) {
            chartOfAccountRepository.deleteById(createdLedger.getId());
        }
    }

    @Test
    @Transactional
    void getPageReturnsPaginatedList() throws Exception {
        restChartOfAccountMockMvc
            .perform(get(BASE_URL + "?page=0&size=10&sort=ledgerName,asc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().exists("X-Total-Count"))
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Transactional
    void createChartOfAccountReturns201() throws Exception {
        ChartOfAccountCreateRequest request = new ChartOfAccountCreateRequest();
        request.setLedgerName("IT Test Ledger");
        request.setClassification("EXPENSE");
        request.setOpeningBalance(BigDecimal.ZERO);

        String responseBody = restChartOfAccountMockMvc
            .perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString("/api/chart-of-accounts/")))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.ledgerName").value("IT Test Ledger"))
            .andExpect(jsonPath("$.accountingClass").value("EXPENSE"))
            .andExpect(jsonPath("$.classification").value("EXPENSE"))
            .andExpect(jsonPath("$.system").value(false))
            .andReturn()
            .getResponse()
            .getContentAsString();

        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(responseBody);
        Long id = node.get("id").longValue();
        createdLedger = chartOfAccountRepository.findById(id).orElse(null);
    }

    @Test
    @Transactional
    void getByIdReturnsLedger() throws Exception {
        ChartOfAccount entity = new ChartOfAccount();
        entity.setTraderId(101L);
        entity.setLedgerName("IT Get Ledger");
        entity.setAccountingClass("ASSET");
        entity.setClassification("BANK");
        entity.setSystem(false);
        entity.setLocked(false);
        entity.setOpeningBalance(BigDecimal.ZERO);
        entity.setCurrentBalance(BigDecimal.ZERO);
        entity.setCreatedBy("test");
        entity.setLastModifiedBy("test");
        entity = chartOfAccountRepository.save(entity);
        createdLedger = entity;

        restChartOfAccountMockMvc
            .perform(get(BASE_URL + "/" + entity.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(entity.getId()))
            .andExpect(jsonPath("$.ledgerName").value("IT Get Ledger"));
    }

    @Test
    @Transactional
    void updateChartOfAccountReturns200() throws Exception {
        ChartOfAccount entity = new ChartOfAccount();
        entity.setTraderId(101L);
        entity.setLedgerName("IT Update Ledger");
        entity.setAccountingClass("EXPENSE");
        entity.setClassification("EXPENSE");
        entity.setSystem(false);
        entity.setLocked(false);
        entity.setOpeningBalance(BigDecimal.ZERO);
        entity.setCurrentBalance(BigDecimal.ZERO);
        entity.setCreatedBy("test");
        entity.setLastModifiedBy("test");
        entity = chartOfAccountRepository.save(entity);
        createdLedger = entity;

        ChartOfAccountUpdateRequest request = new ChartOfAccountUpdateRequest();
        request.setLedgerName("IT Updated Name");
        request.setClassification("EXPENSE");
        request.setOpeningBalance(BigDecimal.ZERO);
        request.setCurrentBalance(BigDecimal.ZERO);

        restChartOfAccountMockMvc
            .perform(put(BASE_URL + "/" + entity.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ledgerName").value("IT Updated Name"));
    }

    @Test
    @Transactional
    void deleteChartOfAccountReturns204() throws Exception {
        ChartOfAccount entity = new ChartOfAccount();
        entity.setTraderId(101L);
        entity.setLedgerName("IT Delete Ledger");
        entity.setAccountingClass("INCOME");
        entity.setClassification("INCOME");
        entity.setSystem(false);
        entity.setLocked(false);
        entity.setOpeningBalance(BigDecimal.ZERO);
        entity.setCurrentBalance(BigDecimal.ZERO);
        entity.setCreatedBy("test");
        entity.setLastModifiedBy("test");
        entity = chartOfAccountRepository.save(entity);

        restChartOfAccountMockMvc
            .perform(delete(BASE_URL + "/" + entity.getId()))
            .andExpect(status().isNoContent());

        chartOfAccountRepository.findById(entity.getId()).ifPresent(chartOfAccountRepository::delete);
    }
}
