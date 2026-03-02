package com.mercotrace.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Request body for creating a Chart of Account (ledger).
 * traderId is resolved by server (TraderContextService); accountingClass derived from classification.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChartOfAccountCreateRequest implements Serializable {

    @NotBlank(message = "Ledger name is required")
    @Size(max = 150)
    private String ledgerName;

    @NotBlank(message = "Classification is required")
    @Size(max = 30)
    private String classification;

    private BigDecimal openingBalance;

    private Long parentControlId;
    private Long contactId;

    public String getLedgerName() { return ledgerName; }
    public void setLedgerName(String ledgerName) { this.ledgerName = ledgerName; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public Long getParentControlId() { return parentControlId; }
    public void setParentControlId(Long parentControlId) { this.parentControlId = parentControlId; }

    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
}
