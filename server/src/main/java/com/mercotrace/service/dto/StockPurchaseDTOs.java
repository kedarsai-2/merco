package com.mercotrace.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * DTOs for the Stock Purchase module (StockPurchasePage.tsx).
 */
public final class StockPurchaseDTOs {

    private StockPurchaseDTOs() {}

    /** Line item in request or response. */
    public static class PurchaseLineItemDTO implements Serializable {
        private Long id;
        private String commodity;
        private Long commodityId;
        private Integer quantity;
        private BigDecimal rate;
        private BigDecimal amount;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCommodity() { return commodity; }
        public void setCommodity(String commodity) { this.commodity = commodity; }
        public Long getCommodityId() { return commodityId; }
        public void setCommodityId(Long commodityId) { this.commodityId = commodityId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    /** Charge in request or response. */
    public static class PurchaseChargeDTO implements Serializable {
        private Long id;
        private String name;
        private BigDecimal amount;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    /** Full purchase DTO (list item and detail). */
    public static class StockPurchaseDTO implements Serializable {
        private Long id;
        private Long vendorId;
        private String vendorName;
        private List<PurchaseLineItemDTO> items = new ArrayList<>();
        private List<PurchaseChargeDTO> charges = new ArrayList<>();
        private BigDecimal subtotal;
        private BigDecimal totalCharges;
        private BigDecimal grandTotal;
        private List<String> lotNumbers = new ArrayList<>();
        private Instant createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getVendorId() { return vendorId; }
        public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }
        public List<PurchaseLineItemDTO> getItems() { return items; }
        public void setItems(List<PurchaseLineItemDTO> items) { this.items = items; }
        public List<PurchaseChargeDTO> getCharges() { return charges; }
        public void setCharges(List<PurchaseChargeDTO> charges) { this.charges = charges; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
        public BigDecimal getTotalCharges() { return totalCharges; }
        public void setTotalCharges(BigDecimal totalCharges) { this.totalCharges = totalCharges; }
        public BigDecimal getGrandTotal() { return grandTotal; }
        public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
        public List<String> getLotNumbers() { return lotNumbers; }
        public void setLotNumbers(List<String> lotNumbers) { this.lotNumbers = lotNumbers; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    }

    /** Request body for POST create. */
    public static class CreateStockPurchaseRequestDTO implements Serializable {

        @NotNull(message = "vendorId is required")
        private Long vendorId;

        @NotNull(message = "items are required")
        @Valid
        @Size(min = 1, message = "at least one item required")
        private List<PurchaseLineItemInput> items = new ArrayList<>();

        private List<PurchaseChargeInput> charges = new ArrayList<>();

        public Long getVendorId() { return vendorId; }
        public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
        public List<PurchaseLineItemInput> getItems() { return items; }
        public void setItems(List<PurchaseLineItemInput> items) { this.items = items; }
        public List<PurchaseChargeInput> getCharges() { return charges; }
        public void setCharges(List<PurchaseChargeInput> charges) { this.charges = charges; }
    }

    /** One line item in create request. */
    public static class PurchaseLineItemInput implements Serializable {
        private Long commodityId;
        private String commodity;
        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0", message = "quantity must be >= 0")
        private Integer quantity;
        @NotNull(message = "rate is required")
        @DecimalMin(value = "0", message = "rate must be >= 0")
        private BigDecimal rate;
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0", message = "amount must be >= 0")
        private BigDecimal amount;

        public Long getCommodityId() { return commodityId; }
        public void setCommodityId(Long commodityId) { this.commodityId = commodityId; }
        public String getCommodity() { return commodity; }
        public void setCommodity(String commodity) { this.commodity = commodity; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    /** One charge in create request. */
    public static class PurchaseChargeInput implements Serializable {
        @Size(max = 150)
        private String name;
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0", message = "amount must be >= 0")
        private BigDecimal amount;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
