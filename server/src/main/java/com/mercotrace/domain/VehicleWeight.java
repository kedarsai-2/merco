package com.mercotrace.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A VehicleWeight.
 */
@Entity
@Table(name = "vehicle_weight")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class VehicleWeight extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "loaded_weight", nullable = false)
    private Double loadedWeight;

    @Column(name = "empty_weight", nullable = false)
    private Double emptyWeight;

    @Column(name = "deducted_weight", nullable = false)
    private Double deductedWeight;

    @Column(name = "net_weight", nullable = false)
    private Double netWeight;

    @Column(name = "recorded_by", length = 50)
    private String recordedBy;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getLoadedWeight() {
        return loadedWeight;
    }

    public void setLoadedWeight(Double loadedWeight) {
        this.loadedWeight = loadedWeight;
    }

    public Double getEmptyWeight() {
        return emptyWeight;
    }

    public void setEmptyWeight(Double emptyWeight) {
        this.emptyWeight = emptyWeight;
    }

    public Double getDeductedWeight() {
        return deductedWeight;
    }

    public void setDeductedWeight(Double deductedWeight) {
        this.deductedWeight = deductedWeight;
    }

    public Double getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Double netWeight) {
        this.netWeight = netWeight;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VehicleWeight)) {
            return false;
        }
        return getId() != null && getId().equals(((VehicleWeight) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "VehicleWeight{" +
            "id=" + getId() +
            ", vehicleId=" + getVehicleId() +
            ", loadedWeight=" + getLoadedWeight() +
            ", emptyWeight=" + getEmptyWeight() +
            ", deductedWeight=" + getDeductedWeight() +
            ", netWeight=" + getNetWeight() +
            ", recordedBy='" + getRecordedBy() + "'" +
            ", recordedAt='" + getRecordedAt() + "'" +
            "}";
    }
}

