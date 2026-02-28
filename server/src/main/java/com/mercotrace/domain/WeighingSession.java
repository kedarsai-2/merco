package com.mercotrace.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Post-auction weighing session for a single bid.
 * Aligned with WeighingPage.tsx mkt_weighing_sessions and BillingPage usage.
 */
@Entity
@Table(name = "weighing_session")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class WeighingSession extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "trader_id")
    private Long traderId;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(name = "bid_number", nullable = false)
    private Integer bidNumber;

    @Column(name = "buyer_mark", length = 50)
    private String buyerMark;

    @Column(name = "buyer_name", length = 200)
    private String buyerName;

    @Column(name = "seller_name", length = 200)
    private String sellerName;

    @Column(name = "lot_name")
    private String lotName;

    @Column(name = "total_bags", nullable = false)
    private Integer totalBags;

    @Column(name = "original_weight", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalWeight;

    @Column(name = "net_weight", nullable = false, precision = 19, scale = 4)
    private BigDecimal netWeight;

    @Column(name = "manual_entry", nullable = false)
    private Boolean manualEntry = false;

    @Column(name = "deductions", nullable = false, precision = 19, scale = 4)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "govt_deduction_applied", nullable = false)
    private Boolean govtDeductionApplied = false;

    @Column(name = "round_off_applied", nullable = false)
    private Boolean roundOffApplied = false;

    @Column(name = "bag_weights_json", columnDefinition = "text")
    private String bagWeightsJson;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public Integer getBidNumber() {
        return bidNumber;
    }

    public void setBidNumber(Integer bidNumber) {
        this.bidNumber = bidNumber;
    }

    public String getBuyerMark() {
        return buyerMark;
    }

    public void setBuyerMark(String buyerMark) {
        this.buyerMark = buyerMark;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getLotName() {
        return lotName;
    }

    public void setLotName(String lotName) {
        this.lotName = lotName;
    }

    public Integer getTotalBags() {
        return totalBags;
    }

    public void setTotalBags(Integer totalBags) {
        this.totalBags = totalBags;
    }

    public BigDecimal getOriginalWeight() {
        return originalWeight;
    }

    public void setOriginalWeight(BigDecimal originalWeight) {
        this.originalWeight = originalWeight;
    }

    public BigDecimal getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(BigDecimal netWeight) {
        this.netWeight = netWeight;
    }

    public Boolean getManualEntry() {
        return manualEntry;
    }

    public void setManualEntry(Boolean manualEntry) {
        this.manualEntry = manualEntry;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public Boolean getGovtDeductionApplied() {
        return govtDeductionApplied;
    }

    public void setGovtDeductionApplied(Boolean govtDeductionApplied) {
        this.govtDeductionApplied = govtDeductionApplied;
    }

    public Boolean getRoundOffApplied() {
        return roundOffApplied;
    }

    public void setRoundOffApplied(Boolean roundOffApplied) {
        this.roundOffApplied = roundOffApplied;
    }

    public String getBagWeightsJson() {
        return bagWeightsJson;
    }

    public void setBagWeightsJson(String bagWeightsJson) {
        this.bagWeightsJson = bagWeightsJson;
    }
}
