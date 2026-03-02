package com.mercotrace.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** DTO for Chart of Account (ledger). Aligned with frontend COALedger. */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChartOfAccountDTO implements Serializable {

    private Long id;
    private Long traderId;
    @NotBlank
    @Size(max = 150)
    private String ledgerName;
    @NotBlank
    @Size(max = 30)
    private String accountingClass;
    @NotBlank
    @Size(max = 30)
    private String classification;
    private Long parentControlId;
    private Long contactId;
    @NotNull
    private Boolean system = false;
    @NotNull
    private Boolean locked = false;
    @NotNull
    private BigDecimal openingBalance = BigDecimal.ZERO;
    @NotNull
    private BigDecimal currentBalance = BigDecimal.ZERO;
    private Instant createdAt;
    private String createdBy;
    private String lastModifiedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTraderId() { return traderId; }
    public void setTraderId(Long traderId) { this.traderId = traderId; }
    public String getLedgerName() { return ledgerName; }
    public void setLedgerName(String ledgerName) { this.ledgerName = ledgerName; }
    public String getAccountingClass() { return accountingClass; }
    public void setAccountingClass(String accountingClass) { this.accountingClass = accountingClass; }
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    public Long getParentControlId() { return parentControlId; }
    public void setParentControlId(Long parentControlId) { this.parentControlId = parentControlId; }
    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
    public Boolean getSystem() { return system; }
    public void setSystem(Boolean system) { this.system = system != null && system; }
    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked != null && locked; }
    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance != null ? openingBalance : BigDecimal.ZERO; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance != null ? currentBalance : BigDecimal.ZERO; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChartOfAccountDTO)) return false;
        ChartOfAccountDTO that = (ChartOfAccountDTO) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
