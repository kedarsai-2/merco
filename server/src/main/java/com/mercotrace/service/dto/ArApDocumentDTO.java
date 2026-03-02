package com.mercotrace.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mercotrace.domain.enumeration.ArApStatus;
import com.mercotrace.domain.enumeration.ArApType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/** DTO for AR/AP document. Aligned with frontend ARAPDocument (camelCase, IDs as string in JSON). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArApDocumentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String documentId;
    private String traderId;
    private String contactId;
    private String contactName;
    private String ledgerId;
    private ArApType type;
    private String referenceVoucherId;
    private String referenceNumber;
    private BigDecimal originalAmount;
    private BigDecimal outstandingBalance;
    private ArApStatus status;
    private LocalDate documentDate;
    private Instant createdAt;

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getTraderId() { return traderId; }
    public void setTraderId(String traderId) { this.traderId = traderId; }
    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getLedgerId() { return ledgerId; }
    public void setLedgerId(String ledgerId) { this.ledgerId = ledgerId; }
    public ArApType getType() { return type; }
    public void setType(ArApType type) { this.type = type; }
    public String getReferenceVoucherId() { return referenceVoucherId; }
    public void setReferenceVoucherId(String referenceVoucherId) { this.referenceVoucherId = referenceVoucherId; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }
    public ArApStatus getStatus() { return status; }
    public void setStatus(ArApStatus status) { this.status = status; }
    public LocalDate getDocumentDate() { return documentDate; }
    public void setDocumentDate(LocalDate documentDate) { this.documentDate = documentDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArApDocumentDTO)) return false;
        ArApDocumentDTO that = (ArApDocumentDTO) o;
        return Objects.equals(documentId, that.documentId);
    }

    @Override
    public int hashCode() { return Objects.hash(documentId); }
}
