package com.mercotrace.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercotrace.IntegrationTest;
import com.mercotrace.domain.Commodity;
import com.mercotrace.domain.Contact;
import com.mercotrace.domain.StockPurchase;
import com.mercotrace.repository.CommodityRepository;
import com.mercotrace.repository.ContactRepository;
import com.mercotrace.repository.StockPurchaseRepository;
import com.mercotrace.service.dto.StockPurchaseDTOs.CreateStockPurchaseRequestDTO;
import com.mercotrace.service.dto.StockPurchaseDTOs.PurchaseChargeInput;
import com.mercotrace.service.dto.StockPurchaseDTOs.PurchaseLineItemInput;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link StockPurchaseResource} REST controller.
 * Uses trader 101 (DefaultTraderContextServiceImpl); test data must belong to trader 101.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class StockPurchaseResourceIT {

    private static final String BASE_URL = "/api/stock-purchases";
    private static final long CONTEXT_TRADER_ID = 101L;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc restStockPurchaseMockMvc;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private CommodityRepository commodityRepository;

    @Autowired
    private StockPurchaseRepository stockPurchaseRepository;

    private Contact vendor;
    private Commodity commodity;

    @BeforeEach
    void initTest() {
        vendor = new Contact();
        vendor.setTraderId(CONTEXT_TRADER_ID);
        vendor.setName("Stock Purchase Test Vendor");
        vendor.setPhone("9876543210");
        vendor.setMark("SP");
        vendor.setOpeningBalance(BigDecimal.ZERO);
        vendor.setCurrentBalance(BigDecimal.ZERO);
        vendor.setCreatedAt(Instant.now());
        vendor = contactRepository.saveAndFlush(vendor);

        commodity = new Commodity();
        commodity.setTraderId(CONTEXT_TRADER_ID);
        commodity.setCommodityName("Wheat");
        commodity.setCreatedAt(Instant.now());
        commodity = commodityRepository.saveAndFlush(commodity);
    }

    @AfterEach
    void cleanup() {
        if (vendor != null && vendor.getId() != null) {
            List<StockPurchase> purchases = stockPurchaseRepository.findAll().stream()
                .filter(sp -> sp.getVendorId().equals(vendor.getId()))
                .toList();
            purchases.forEach(stockPurchaseRepository::delete);
            contactRepository.deleteById(vendor.getId());
        }
        if (commodity != null && commodity.getId() != null) {
            commodityRepository.deleteById(commodity.getId());
        }
    }

    @Test
    @Transactional
    void getPurchasesReturnsPaginatedList() throws Exception {
        restStockPurchaseMockMvc
            .perform(get(BASE_URL + "?page=0&size=10&sort=createdDate,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().exists("X-Total-Count"))
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Transactional
    void createStockPurchaseWithValidPayloadReturns201() throws Exception {
        CreateStockPurchaseRequestDTO request = new CreateStockPurchaseRequestDTO();
        request.setVendorId(vendor.getId());
        PurchaseLineItemInput item = new PurchaseLineItemInput();
        item.setCommodityId(commodity.getId());
        item.setCommodity("Wheat");
        item.setQuantity(10);
        item.setRate(new BigDecimal("50.00"));
        item.setAmount(new BigDecimal("500.00"));
        request.setItems(List.of(item));

        String responseBody = restStockPurchaseMockMvc
            .perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
            )
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", containsString(BASE_URL + "/")))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.vendorId").value(vendor.getId().intValue()))
            .andExpect(jsonPath("$.vendorName").value("Stock Purchase Test Vendor"))
            .andExpect(jsonPath("$.subtotal").value(500.0))
            .andExpect(jsonPath("$.items", hasSize(1)))
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long createdId = objectMapper.readTree(responseBody).get("id").asLong();
        assertThat(createdId).isNotNull();
    }

    @Test
    @Transactional
    void getPurchaseByIdReturns200WhenExists() throws Exception {
        CreateStockPurchaseRequestDTO request = new CreateStockPurchaseRequestDTO();
        request.setVendorId(vendor.getId());
        PurchaseLineItemInput item = new PurchaseLineItemInput();
        item.setCommodityId(commodity.getId());
        item.setQuantity(5);
        item.setRate(new BigDecimal("100.00"));
        item.setAmount(new BigDecimal("500.00"));
        request.setItems(List.of(item));

        String createResponse = restStockPurchaseMockMvc
            .perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
            )
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        restStockPurchaseMockMvc
            .perform(get(BASE_URL + "/" + id))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(id.intValue()))
            .andExpect(jsonPath("$.vendorId").value(vendor.getId().intValue()))
            .andExpect(jsonPath("$.vendorName").value("Stock Purchase Test Vendor"))
            .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    @Transactional
    void getPurchaseByIdReturns400WhenNotFound() throws Exception {
        restStockPurchaseMockMvc
            .perform(get(BASE_URL + "/999999"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void createStockPurchaseWithInvalidVendorReturns400() throws Exception {
        CreateStockPurchaseRequestDTO request = new CreateStockPurchaseRequestDTO();
        request.setVendorId(999999L);
        PurchaseLineItemInput item = new PurchaseLineItemInput();
        item.setCommodityId(commodity.getId());
        item.setQuantity(1);
        item.setRate(BigDecimal.ONE);
        item.setAmount(BigDecimal.ONE);
        request.setItems(List.of(item));

        restStockPurchaseMockMvc
            .perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void createStockPurchaseWithEmptyItemsReturns400() throws Exception {
        CreateStockPurchaseRequestDTO request = new CreateStockPurchaseRequestDTO();
        request.setVendorId(vendor.getId());
        request.setItems(List.of());

        restStockPurchaseMockMvc
            .perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void createStockPurchaseWithAllZeroAmountItemsReturns400() throws Exception {
        CreateStockPurchaseRequestDTO request = new CreateStockPurchaseRequestDTO();
        request.setVendorId(vendor.getId());
        PurchaseLineItemInput item = new PurchaseLineItemInput();
        item.setCommodityId(commodity.getId());
        item.setQuantity(0);
        item.setRate(BigDecimal.ZERO);
        item.setAmount(BigDecimal.ZERO);
        request.setItems(List.of(item));

        restStockPurchaseMockMvc
            .perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    void createStockPurchaseWithChargesReturnsGrandTotal() throws Exception {
        CreateStockPurchaseRequestDTO request = new CreateStockPurchaseRequestDTO();
        request.setVendorId(vendor.getId());
        PurchaseLineItemInput item = new PurchaseLineItemInput();
        item.setCommodityId(commodity.getId());
        item.setQuantity(10);
        item.setRate(new BigDecimal("50.00"));
        item.setAmount(new BigDecimal("500.00"));
        request.setItems(List.of(item));
        PurchaseChargeInput charge = new PurchaseChargeInput();
        charge.setName("Freight");
        charge.setAmount(new BigDecimal("50.00"));
        request.setCharges(List.of(charge));

        restStockPurchaseMockMvc
            .perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.subtotal").value(500.0))
            .andExpect(jsonPath("$.totalCharges").value(50.0))
            .andExpect(jsonPath("$.grandTotal").value(550.0));
    }
}
