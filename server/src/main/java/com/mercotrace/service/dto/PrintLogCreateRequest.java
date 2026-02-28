package com.mercotrace.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Request body for logging a print event.
 */
public class PrintLogCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @JsonProperty("reference_type")
    private String referenceType;

    @JsonProperty("reference_id")
    private String referenceId;

    @NotNull
    @JsonProperty("print_type")
    private String printType;

    @JsonProperty("printed_at")
    private java.time.Instant printedAt;

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getPrintType() { return printType; }
    public void setPrintType(String printType) { this.printType = printType; }

    public java.time.Instant getPrintedAt() { return printedAt; }
    public void setPrintedAt(java.time.Instant printedAt) { this.printedAt = printedAt; }
}
