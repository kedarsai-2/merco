package com.mercotrace.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercotrace.IntegrationTest;
import com.mercotrace.domain.Cdn;
import com.mercotrace.repository.CdnRepository;
import com.mercotrace.repository.CdnTransferRepository;
import com.mercotrace.service.dto.CDNDTOs.CDNCreateDTO;
import com.mercotrace.service.dto.CDNDTOs.CDNLineItemInput;
import com.mercotrace.service.dto.CDNDTOs.ReceiveByPINDTO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Integration tests for {@link CDNResource}. Uses trader 101 (DefaultTraderContextServiceImpl).
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CDNResourceIT {

    private static final String BASE_URL = "/api/cdns";
    private static final long CONTEXT_TRADER_ID = 101L;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc restCdnMockMvc;

    @Autowired
    private CdnRepository cdnRepository;

    @Autowired
    private CdnTransferRepository cdnTransferRepository;

    private Cdn createdCdn;

    @BeforeEach
    void initTest() {
        createdCdn = null;
    }

    @AfterEach
    void cleanup() {
        if (createdCdn != null && createdCdn.getId() != null) {
            cdnRepository.deleteById(createdCdn.getId());
        }
    }

    @Test
    void createCdn_returns201AndBodyWithPin() throws Exception {
        CDNCreateDTO request = new CDNCreateDTO();
        request.setReceivingParty("Test Receiver");
        request.setSource("MANUAL");
        CDNLineItemInput item = new CDNLineItemInput();
        item.setLotName("LOT-IT-1");
        item.setQuantity(10);
        item.setVariant("Wheat");
        request.setItems(List.of(item));
        request.setAdvancePaid(BigDecimal.valueOf(500));

        MvcResult result = restCdnMockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.cdnNumber").value(org.hamcrest.Matchers.startsWith("CDN-")))
            .andExpect(jsonPath("$.pin").value(org.hamcrest.Matchers.hasLength(6)))
            .andExpect(jsonPath("$.pinUsed").value(false))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.items.length()").value(1))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        com.mercotrace.service.dto.CDNDTOs.CDNResponseDTO dto = objectMapper.readValue(body, com.mercotrace.service.dto.CDNDTOs.CDNResponseDTO.class);
        createdCdn = cdnRepository.findById(dto.getId()).orElse(null);
        assertThat(createdCdn).isNotNull();
        assertThat(createdCdn.getTraderId()).isEqualTo(CONTEXT_TRADER_ID);
    }

    @Test
    void listCdns_paginated() throws Exception {
        restCdnMockMvc.perform(get(BASE_URL)
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(header().exists("X-Total-Count"));
    }

    @Test
    void getCdnById_returns200WhenExists() throws Exception {
        CDNCreateDTO request = new CDNCreateDTO();
        request.setReceivingParty("Detail Receiver");
        request.setSource("DIRECT");
        CDNLineItemInput item = new CDNLineItemInput();
        item.setLotName("LOT-DETAIL");
        item.setQuantity(5);
        request.setItems(List.of(item));

        MvcResult createResult = restCdnMockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        com.mercotrace.service.dto.CDNDTOs.CDNResponseDTO createDto = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            com.mercotrace.service.dto.CDNDTOs.CDNResponseDTO.class
        );
        createdCdn = cdnRepository.findById(createDto.getId()).orElse(null);

        restCdnMockMvc.perform(get(BASE_URL + "/" + createDto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createDto.getId()))
            .andExpect(jsonPath("$.cdnNumber").value(createDto.getCdnNumber()))
            .andExpect(jsonPath("$.receivingParty").value("Detail Receiver"));
    }

    @Test
    void receiveByPin_success_whenValidPin() throws Exception {
        CDNCreateDTO request = new CDNCreateDTO();
        request.setReceivingParty("Receive Test");
        request.setSource("MANUAL");
        CDNLineItemInput item = new CDNLineItemInput();
        item.setLotName("LOT-RECV");
        item.setQuantity(1);
        request.setItems(List.of(item));

        MvcResult createResult = restCdnMockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        com.mercotrace.service.dto.CDNDTOs.CDNResponseDTO createDto = objectMapper.readValue(
            createResult.getResponse().getContentAsString(),
            com.mercotrace.service.dto.CDNDTOs.CDNResponseDTO.class
        );
        createdCdn = cdnRepository.findById(createDto.getId()).orElse(null);
        String pin = createDto.getPin();
        assertThat(pin).hasSize(6);

        ReceiveByPINDTO receiveRequest = new ReceiveByPINDTO();
        receiveRequest.setPin(pin);

        restCdnMockMvc.perform(post(BASE_URL + "/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiveRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("TRANSFERRED"))
            .andExpect(jsonPath("$.id").value(createDto.getId()));
    }

    @Test
    void receiveByPin_returns400_whenInvalidPin() throws Exception {
        ReceiveByPINDTO request = new ReceiveByPINDTO();
        request.setPin("INVALID");

        restCdnMockMvc.perform(post(BASE_URL + "/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createCdn_returns400_whenNoLotName() throws Exception {
        CDNCreateDTO request = new CDNCreateDTO();
        request.setReceivingParty("No Lot");
        request.setSource("MANUAL");
        CDNLineItemInput item = new CDNLineItemInput();
        item.setLotName("");
        item.setQuantity(5);
        request.setItems(List.of(item));

        restCdnMockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
