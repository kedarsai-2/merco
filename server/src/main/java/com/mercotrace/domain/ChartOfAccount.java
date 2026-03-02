package com.mercotrace.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Chart of Accounts — ledger definition.
 * Aligned with frontend COALedger in client/src/types/accounting.ts.
 * Audit: created_by, created_date, last_modified_by, last_modified_date via AbstractAuditingEntity.
 */
@Entity
@Table(name = "chart_of_account")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChartOfAccount extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "trader_id", nullable = false)
    private Long traderId;

    @NotNull
    @Size(max = 150)
    @Column(name = "ledger_name", length = 150, nullable = false)
    private String ledgerName;

    @NotNull
    @Size(max = 30)
    @Column(name = "accounting_class", length = 30, nullable = false)
    private String accountingClass;

    @NotNull
    @Size(max = 30)
    @Column(name = "classification", length = 30, nullable = false)
    private String classification;

    @Column(name = "parent_control_id")
    private Long parentControlId;

    @Column(name = "contact_id")
    private Long contactId;

    @NotNull
    @Column(name = "is_system", nullable = false)
    private Boolean system = false;

    @NotNull
    @Column(name = "is_locked", nullable = false)
    private Boolean locked = false;

    @NotNull
    @Column(name = "opening_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @NotNull
    @Column(name = "current_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTraderId() {
        return traderId;
    }

    public void setTraderId(Long traderId) {
        this.traderId = traderId;
    }

    public String getLedgerName() {
        return ledgerName;
    }

    public void setLedgerName(String ledgerName) {
        this.ledgerName = ledgerName;
    }

    public String getAccountingClass() {
        return accountingClass;
    }

    public void setAccountingClass(String accountingClass) {
        this.accountingClass = accountingClass;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public Long getParentControlId() {
        return parentControlId;
    }

    public void setParentControlId(Long parentControlId) {
        this.parentControlId = parentControlId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public Boolean getSystem() {
        return system;
    }

    public void setSystem(Boolean system) {
        this.system = system;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance != null ? openingBalance : BigDecimal.ZERO;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance != null ? currentBalance : BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChartOfAccount)) return false;
        return id != null && id.equals(((ChartOfAccount) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ChartOfAccount{" +
            "id=" + id +
            ", traderId=" + traderId +
            ", ledgerName='" + ledgerName + '\'' +
            ", accountingClass='" + accountingClass + '\'' +
            ", classification='" + classification + '\'' +
            "}";
    }
}
