package com.mercotrace.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for PrintLog. Aligned with frontend mkt_print_logs.
 */
public class PrintLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("reference_type")
    private String referenceType;

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("print_type")
    private String printType;

    @JsonProperty("printed_at")
    private Instant printedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getPrintType() { return printType; }
    public void setPrintType(String printType) { this.printType = printType; }

    public Instant getPrintedAt() { return printedAt; }
    public void setPrintedAt(Instant printedAt) { this.printedAt = printedAt; }
}
