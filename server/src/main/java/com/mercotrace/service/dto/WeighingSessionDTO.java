package com.mercotrace.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO for WeighingSession. JSON keys aligned with frontend mkt_weighing_sessions.
 */
public class WeighingSessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("lot_id")
    private Long lotId;

    @JsonProperty("bid_number")
    private Integer bidNumber;

    @JsonProperty("buyer_mark")
    private String buyerMark;

    @JsonProperty("buyer_name")
    private String buyerName;

    @JsonProperty("seller_name")
    private String sellerName;

    @JsonProperty("lot_name")
    private String lotName;

    @JsonProperty("total_bags")
    private Integer totalBags;

    @JsonProperty("original_weight")
    private BigDecimal originalWeight;

    @JsonProperty("net_weight")
    private BigDecimal netWeight;

    @JsonProperty("manual_entry")
    private Boolean manualEntry;

    @JsonProperty("deductions")
    private BigDecimal deductions;

    @JsonProperty("govt_deduction_applied")
    private Boolean govtDeductionApplied;

    @JsonProperty("round_off_applied")
    private Boolean roundOffApplied;

    @JsonProperty("bag_weights")
    private List<BagWeightEntryDTO> bagWeights;

    @JsonProperty("created_at")
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Long getLotId() { return lotId; }
    public void setLotId(Long lotId) { this.lotId = lotId; }

    public Integer getBidNumber() { return bidNumber; }
    public void setBidNumber(Integer bidNumber) { this.bidNumber = bidNumber; }

    public String getBuyerMark() { return buyerMark; }
    public void setBuyerMark(String buyerMark) { this.buyerMark = buyerMark; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getLotName() { return lotName; }
    public void setLotName(String lotName) { this.lotName = lotName; }

    public Integer getTotalBags() { return totalBags; }
    public void setTotalBags(Integer totalBags) { this.totalBags = totalBags; }

    public BigDecimal getOriginalWeight() { return originalWeight; }
    public void setOriginalWeight(BigDecimal originalWeight) { this.originalWeight = originalWeight; }

    public BigDecimal getNetWeight() { return netWeight; }
    public void setNetWeight(BigDecimal netWeight) { this.netWeight = netWeight; }

    public Boolean getManualEntry() { return manualEntry; }
    public void setManualEntry(Boolean manualEntry) { this.manualEntry = manualEntry; }

    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }

    public Boolean getGovtDeductionApplied() { return govtDeductionApplied; }
    public void setGovtDeductionApplied(Boolean govtDeductionApplied) { this.govtDeductionApplied = govtDeductionApplied; }

    public Boolean getRoundOffApplied() { return roundOffApplied; }
    public void setRoundOffApplied(Boolean roundOffApplied) { this.roundOffApplied = roundOffApplied; }

    public List<BagWeightEntryDTO> getBagWeights() { return bagWeights; }
    public void setBagWeights(List<BagWeightEntryDTO> bagWeights) { this.bagWeights = bagWeights; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
