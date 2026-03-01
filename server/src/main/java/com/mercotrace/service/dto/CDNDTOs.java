package com.mercotrace.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * DTOs for CDN module (CDNPage.tsx). Source MANUAL → DIRECT; status ACTIVE → DISPATCHED.
 */
public final class CDNDTOs {

    private CDNDTOs() {}

    /** Line item – request and response. */
    public static class CDNLineItemDTO implements Serializable {
        private String id;
        private String lotName;
        private Integer quantity;
        private String variant;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getLotName() { return lotName; }
        public void setLotName(String lotName) { this.lotName = lotName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getVariant() { return variant; }
        public void setVariant(String variant) { this.variant = variant; }
    }

    /** Create request – aligned with CDNPage create form. */
    public static class CDNCreateDTO implements Serializable {

        @NotBlank(message = "receivingParty is required")
        @Size(max = 150)
        private String receivingParty;

        private Long receivingPartyId;

        @NotNull(message = "items are required")
        @Valid
        @Size(min = 1, message = "at least one item required")
        private List<CDNLineItemInput> items = new ArrayList<>();

        @Size(max = 200)
        private String freightFormula;

        @Size(max = 150)
        private String transporter;

        @Size(max = 150)
        private String driver;

        private BigDecimal advancePaid = BigDecimal.ZERO;

        @Size(max = 1000)
        private String remarks;

        /** Frontend: MANUAL, SALES_PAD, SELF_SALE, STOCK_PURCHASE. Backend maps MANUAL → DIRECT. */
        @NotBlank(message = "source is required")
        @Size(max = 50)
        private String source;

        public String getReceivingParty() { return receivingParty; }
        public void setReceivingParty(String receivingParty) { this.receivingParty = receivingParty; }
        public Long getReceivingPartyId() { return receivingPartyId; }
        public void setReceivingPartyId(Long receivingPartyId) { this.receivingPartyId = receivingPartyId; }
        public List<CDNLineItemInput> getItems() { return items; }
        public void setItems(List<CDNLineItemInput> items) { this.items = items; }
        public String getFreightFormula() { return freightFormula; }
        public void setFreightFormula(String freightFormula) { this.freightFormula = freightFormula; }
        public String getTransporter() { return transporter; }
        public void setTransporter(String transporter) { this.transporter = transporter; }
        public String getDriver() { return driver; }
        public void setDriver(String driver) { this.driver = driver; }
        public BigDecimal getAdvancePaid() { return advancePaid; }
        public void setAdvancePaid(BigDecimal advancePaid) { this.advancePaid = advancePaid; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }

    /** One line item in create request. */
    public static class CDNLineItemInput implements Serializable {
        @Size(max = 50)
        private String lotName;
        @NotNull
        @Min(0)
        private Integer quantity = 0;
        @Size(max = 100)
        private String variant;

        public String getLotName() { return lotName; }
        public void setLotName(String lotName) { this.lotName = lotName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getVariant() { return variant; }
        public void setVariant(String variant) { this.variant = variant; }
    }

    /** Response – single CDN (list row or detail). Frontend status: ACTIVE, TRANSFERRED, EXPIRED. */
    public static class CDNResponseDTO implements Serializable {
        private Long id;
        private String cdnNumber;
        private Instant date;
        private String dispatchingParty;
        private String receivingParty;
        private List<CDNLineItemDTO> items = new ArrayList<>();
        private String freightFormula;
        private String transporter;
        private String driver;
        private BigDecimal advancePaid = BigDecimal.ZERO;
        private String remarks;
        private String pin;
        private Boolean pinUsed;
        private Instant pinExpiresAt;
        private String source;
        private String status;
        private Instant createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getCdnNumber() { return cdnNumber; }
        public void setCdnNumber(String cdnNumber) { this.cdnNumber = cdnNumber; }
        public Instant getDate() { return date; }
        public void setDate(Instant date) { this.date = date; }
        public String getDispatchingParty() { return dispatchingParty; }
        public void setDispatchingParty(String dispatchingParty) { this.dispatchingParty = dispatchingParty; }
        public String getReceivingParty() { return receivingParty; }
        public void setReceivingParty(String receivingParty) { this.receivingParty = receivingParty; }
        public List<CDNLineItemDTO> getItems() { return items; }
        public void setItems(List<CDNLineItemDTO> items) { this.items = items; }
        public String getFreightFormula() { return freightFormula; }
        public void setFreightFormula(String freightFormula) { this.freightFormula = freightFormula; }
        public String getTransporter() { return transporter; }
        public void setTransporter(String transporter) { this.transporter = transporter; }
        public String getDriver() { return driver; }
        public void setDriver(String driver) { this.driver = driver; }
        public BigDecimal getAdvancePaid() { return advancePaid; }
        public void setAdvancePaid(BigDecimal advancePaid) { this.advancePaid = advancePaid; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public String getPin() { return pin; }
        public void setPin(String pin) { this.pin = pin; }
        public Boolean getPinUsed() { return pinUsed; }
        public void setPinUsed(Boolean pinUsed) { this.pinUsed = pinUsed; }
        public Instant getPinExpiresAt() { return pinExpiresAt; }
        public void setPinExpiresAt(Instant pinExpiresAt) { this.pinExpiresAt = pinExpiresAt; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    }

    /** Receive by PIN request. */
    public static class ReceiveByPINDTO implements Serializable {

        @NotBlank(message = "pin is required")
        @Size(min = 6, max = 6, message = "pin must be 6 characters")
        private String pin;

        public String getPin() { return pin; }
        public void setPin(String pin) { this.pin = pin; }
    }
}
