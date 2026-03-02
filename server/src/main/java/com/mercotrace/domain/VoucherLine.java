package com.mercotrace.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Single line of an accounting voucher (ledger, debit, credit).
 * Aligned with frontend VoucherLine in client/src/types/accounting.ts.
 */
@Entity
@Table(name = "voucher_line")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VoucherLine implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_header_id", nullable = false)
    private VoucherHeader voucherHeader;

    @NotNull
    @Column(name = "ledger_id", nullable = false)
    private Long ledgerId;

    @Column(name = "ledger_name", length = 150)
    private String ledgerName;

    @NotNull
    @Column(name = "debit", precision = 19, scale = 2, nullable = false)
    private BigDecimal debit = BigDecimal.ZERO;

    @NotNull
    @Column(name = "credit", precision = 19, scale = 2, nullable = false)
    private BigDecimal credit = BigDecimal.ZERO;

    @Column(name = "commodity_id")
    private Long commodityId;

    @Column(name = "commodity_name", length = 150)
    private String commodityName;

    @Column(name = "quantity", precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "rate", precision = 19, scale = 4)
    private BigDecimal rate;

    @Column(name = "lot_id")
    private Long lotId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VoucherHeader getVoucherHeader() {
        return voucherHeader;
    }

    public void setVoucherHeader(VoucherHeader voucherHeader) {
        this.voucherHeader = voucherHeader;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getLedgerName() {
        return ledgerName;
    }

    public void setLedgerName(String ledgerName) {
        this.ledgerName = ledgerName;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit != null ? debit : BigDecimal.ZERO;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit != null ? credit : BigDecimal.ZERO;
    }

    public Long getCommodityId() {
        return commodityId;
    }

    public void setCommodityId(Long commodityId) {
        this.commodityId = commodityId;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VoucherLine)) return false;
        return id != null && id.equals(((VoucherLine) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
