package com.mercotrace.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercotrace.IntegrationTest;
import com.mercotrace.security.AuthoritiesConstants;
import com.mercotrace.service.dto.WeighingSessionCreateRequest;
import com.mercotrace.service.dto.WeighingSessionDTO;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link com.mercotrace.web.rest.WeighingSessionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.AUCTIONS_VIEW)
class WeighingSessionResourceIT {

    private static final String BASE_URL = "/api/weighing-sessions";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MockMvc restWeighingSessionMockMvc;

    @Test
    @Transactional
    void createWeighingSession() throws Exception {
        WeighingSessionCreateRequest request = new WeighingSessionCreateRequest();
        request.setSessionId("test-session-1");
        request.setLotId(100L);
        request.setBidNumber(1);
        request.setBuyerMark("M1");
        request.setTotalBags(5);
        request.setOriginalWeight(BigDecimal.valueOf(100));
        request.setNetWeight(BigDecimal.valueOf(98));

        MvcResult result = restWeighingSessionMockMvc
            .perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

        WeighingSessionDTO dto = om.readValue(result.getResponse().getContentAsString(), WeighingSessionDTO.class);
        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getSessionId()).isEqualTo("test-session-1");
        assertThat(dto.getBidNumber()).isEqualTo(1);
        assertThat(dto.getNetWeight()).isEqualByComparingTo(BigDecimal.valueOf(98));
    }

    @Test
    @Transactional
    void listWeighingSessions() throws Exception {
        restWeighingSessionMockMvc
            .perform(get(BASE_URL).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Transactional
    void getNonExistingWeighingSession() throws Exception {
        restWeighingSessionMockMvc
            .perform(get(BASE_URL + "/" + Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void getByBidNumberNonExisting() throws Exception {
        restWeighingSessionMockMvc
            .perform(get(BASE_URL + "/by-bid/99999"))
            .andExpect(status().isNotFound());
    }
}
