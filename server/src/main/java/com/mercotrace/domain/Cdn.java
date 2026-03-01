package com.mercotrace.domain;

import com.mercotrace.domain.enumeration.CdnSource;
import com.mercotrace.domain.enumeration.CdnStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * CDN (Consignment Dispatch Note) header. REQ-CDN-001; trader-scoped.
 * Frontend: CDNPage.tsx. Status ACTIVE → DISPATCHED; source MANUAL → DIRECT.
 */
@Entity
@Table(name = "cdn")
@EntityListeners(AuditingEntityListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Cdn implements Serializable {

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
    @Column(name = "cdn_number", nullable = false, length = 50)
    private String cdnNumber;

    @NotNull
    @Column(name = "cdn_date", nullable = false)
    private Instant cdnDate;

    @Column(name = "dispatching_party_id")
    private Long dispatchingPartyId;

    @Column(name = "dispatching_party_name", length = 150)
    private String dispatchingPartyName;

    @Column(name = "receiving_party_id")
    private Long receivingPartyId;

    @Column(name = "receiving_party_name", length = 150)
    private String receivingPartyName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private CdnSource source;

    @Column(name = "source_reference_id")
    private Long sourceReferenceId;

    @Column(name = "transporter_name", length = 150)
    private String transporterName;

    @Column(name = "driver_name", length = 150)
    private String driverName;

    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "freight_formula", length = 200)
    private String freightFormula;

    @Column(name = "freight_amount", precision = 15, scale = 2)
    private BigDecimal freightAmount = BigDecimal.ZERO;

    @Column(name = "advance_paid", precision = 15, scale = 2)
    private BigDecimal advancePaid = BigDecimal.ZERO;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private CdnStatus status;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant lastModifiedAt;

    @OneToMany(mappedBy = "cdn", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CdnItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "cdn", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CdnTransfer> transfers = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTraderId() { return traderId; }
    public void setTraderId(Long traderId) { this.traderId = traderId; }
    public String getCdnNumber() { return cdnNumber; }
    public void setCdnNumber(String cdnNumber) { this.cdnNumber = cdnNumber; }
    public Instant getCdnDate() { return cdnDate; }
    public void setCdnDate(Instant cdnDate) { this.cdnDate = cdnDate; }
    public Long getDispatchingPartyId() { return dispatchingPartyId; }
    public void setDispatchingPartyId(Long dispatchingPartyId) { this.dispatchingPartyId = dispatchingPartyId; }
    public String getDispatchingPartyName() { return dispatchingPartyName; }
    public void setDispatchingPartyName(String dispatchingPartyName) { this.dispatchingPartyName = dispatchingPartyName; }
    public Long getReceivingPartyId() { return receivingPartyId; }
    public void setReceivingPartyId(Long receivingPartyId) { this.receivingPartyId = receivingPartyId; }
    public String getReceivingPartyName() { return receivingPartyName; }
    public void setReceivingPartyName(String receivingPartyName) { this.receivingPartyName = receivingPartyName; }
    public CdnSource getSource() { return source; }
    public void setSource(CdnSource source) { this.source = source; }
    public Long getSourceReferenceId() { return sourceReferenceId; }
    public void setSourceReferenceId(Long sourceReferenceId) { this.sourceReferenceId = sourceReferenceId; }
    public String getTransporterName() { return transporterName; }
    public void setTransporterName(String transporterName) { this.transporterName = transporterName; }
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getFreightFormula() { return freightFormula; }
    public void setFreightFormula(String freightFormula) { this.freightFormula = freightFormula; }
    public BigDecimal getFreightAmount() { return freightAmount; }
    public void setFreightAmount(BigDecimal freightAmount) { this.freightAmount = freightAmount; }
    public BigDecimal getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(BigDecimal advancePaid) { this.advancePaid = advancePaid; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public CdnStatus getStatus() { return status; }
    public void setStatus(CdnStatus status) { this.status = status; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    public Instant getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(Instant lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    public List<CdnItem> getItems() { return items; }
    public void setItems(List<CdnItem> items) { this.items = items; }
    public List<CdnTransfer> getTransfers() { return transfers; }
    public void setTransfers(List<CdnTransfer> transfers) { this.transfers = transfers; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cdn)) return false;
        return getId() != null && getId().equals(((Cdn) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
