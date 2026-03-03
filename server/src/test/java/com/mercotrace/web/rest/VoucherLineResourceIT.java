package com.mercotrace.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mercotrace.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link com.mercotrace.web.rest.VoucherLineResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class VoucherLineResourceIT {

    private static final String BASE_URL = "/api/voucher-lines";

    @Autowired
    private MockMvc restVoucherLineMockMvc;

    @Test
    @Transactional
    void getByDateRange() throws Exception {
        restVoucherLineMockMvc
            .perform(get(BASE_URL).param("dateFrom", "2025-01-01").param("dateTo", "2025-12-31"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
