package com.mercotrace.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** Request body for creating a weighing session (complete session from frontend). */
public class WeighingSessionCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @JsonProperty("session_id")
    private String sessionId;

    @NotNull
    @JsonProperty("lot_id")
    private Long lotId;

    @NotNull
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

    @NotNull
    @JsonProperty("total_bags")
    private Integer totalBags;

    @NotNull
    @JsonProperty("original_weight")
    private BigDecimal originalWeight;

    @NotNull
    @JsonProperty("net_weight")
    private BigDecimal netWeight;

    @JsonProperty("manual_entry")
    private Boolean manualEntry = false;

    @JsonProperty("deductions")
    private BigDecimal deductions = BigDecimal.ZERO;

    @JsonProperty("govt_deduction_applied")
    private Boolean govtDeductionApplied = false;

    @JsonProperty("round_off_applied")
    private Boolean roundOffApplied = false;

    @JsonProperty("bag_weights")
    private List<BagWeightEntryDTO> bagWeights;

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
    public void setGovtDeductionApplied(Boolean v) { this.govtDeductionApplied = v; }

    public Boolean getRoundOffApplied() { return roundOffApplied; }
    public void setRoundOffApplied(Boolean v) { this.roundOffApplied = v; }

    public List<BagWeightEntryDTO> getBagWeights() { return bagWeights; }
    public void setBagWeights(List<BagWeightEntryDTO> bagWeights) { this.bagWeights = bagWeights; }
}
