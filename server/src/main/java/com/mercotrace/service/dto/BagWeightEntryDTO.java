package com.mercotrace.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;

/** Single bag weight entry within a weighing session. */
public class BagWeightEntryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("bagNumber")
    private Integer bagNumber;

    @JsonProperty("weight")
    private BigDecimal weight;

    @JsonProperty("timestamp")
    private String timestamp;

    public Integer getBagNumber() { return bagNumber; }
    public void setBagNumber(Integer bagNumber) { this.bagNumber = bagNumber; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
