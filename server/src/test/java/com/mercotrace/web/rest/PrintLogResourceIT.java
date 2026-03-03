package com.mercotrace.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercotrace.IntegrationTest;
import com.mercotrace.security.AuthoritiesConstants;
import com.mercotrace.service.dto.PrintLogCreateRequest;
import com.mercotrace.service.dto.PrintLogDTO;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link com.mercotrace.web.rest.PrintLogResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.AUCTIONS_VIEW)
class PrintLogResourceIT {

    private static final String BASE_URL = "/api/print-logs";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc restPrintLogMockMvc;

    @Test
    @Transactional
    void createPrintLog() throws Exception {
        PrintLogCreateRequest request = new PrintLogCreateRequest();
        request.setReferenceType("BILL");
        request.setReferenceId("100");
        request.setPrintType("A4_PORTRAIT");
        request.setPrintedAt(Instant.now());

        MvcResult result = restPrintLogMockMvc
            .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

        PrintLogDTO dto = om.readValue(result.getResponse().getContentAsString(), PrintLogDTO.class);
        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getReferenceType()).isEqualTo("BILL");
        assertThat(dto.getReferenceId()).isEqualTo("100");
        assertThat(dto.getPrintType()).isEqualTo("A4_PORTRAIT");
    }

    @Test
    @Transactional
    void listPrintLogs() throws Exception {
        restPrintLogMockMvc
            .perform(get(BASE_URL).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
