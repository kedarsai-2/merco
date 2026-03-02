package com.mercotrace.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Request body for updating a Chart of Account (ledger).
 * Only non-system ledgers; ledgerName must remain unique per trader.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChartOfAccountUpdateRequest implements Serializable {

    @NotBlank(message = "Ledger name is required")
    @Size(max = 150)
    private String ledgerName;

    @NotBlank
    @Size(max = 30)
    private String classification;

    private BigDecimal openingBalance;
    private BigDecimal currentBalance;

    private Long parentControlId;
    private Long contactId;

    private Boolean locked;

    public String getLedgerName() { return ledgerName; }
    public void setLedgerName(String ledgerName) { this.ledgerName = ledgerName; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public Long getParentControlId() { return parentControlId; }
    public void setParentControlId(Long parentControlId) { this.parentControlId = parentControlId; }

    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }

    public Boolean getLocked() { return locked; }
    public void setLocked(Boolean locked) { this.locked = locked; }
}
