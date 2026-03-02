package com.mercotrace.domain;

import com.mercotrace.domain.enumeration.ArApStatus;
import com.mercotrace.domain.enumeration.ArApType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * AR/AP document for aging reports. Aligned with frontend ARAPDocument.
 */
@Entity
@Table(name = "ar_ap_document")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ArApDocument extends AbstractAuditingEntity<Long> implements Serializable {

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @NotNull
    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10, nullable = false)
    private ArApType type;

    @NotNull
    @Column(name = "reference_voucher_id", nullable = false)
    private Long referenceVoucherId;

    @NotNull
    @jakarta.validation.constraints.Size(max = 50)
    @Column(name = "reference_number", length = 50, nullable = false)
    private String referenceNumber;

    @NotNull
    @Column(name = "original_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal originalAmount;

    @NotNull
    @Column(name = "outstanding_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal outstandingBalance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ArApStatus status;

    @NotNull
    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    @Override
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

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public ArApType getType() {
        return type;
    }

    public void setType(ArApType type) {
        this.type = type;
    }

    public Long getReferenceVoucherId() {
        return referenceVoucherId;
    }

    public void setReferenceVoucherId(Long referenceVoucherId) {
        this.referenceVoucherId = referenceVoucherId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public ArApStatus getStatus() {
        return status;
    }

    public void setStatus(ArApStatus status) {
        this.status = status;
    }

    public LocalDate getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(LocalDate documentDate) {
        this.documentDate = documentDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArApDocument)) return false;
        return id != null && id.equals(((ArApDocument) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
