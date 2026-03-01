package com.mercotrace.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * CDN line item – lot/commodity/quantity. REQ-CDN; aligned with CDNPage.tsx CDNLineItem.
 */
@Entity
@Table(name = "cdn_items")
@EntityListeners(AuditingEntityListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CdnItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdn_id", nullable = false)
    private Cdn cdn;

    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "commodity_id")
    private Long commodityId;

    @Column(name = "lot_name", length = 50)
    private String lotName;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "variant", length = 100)
    private String variant;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cdn getCdn() { return cdn; }
    public void setCdn(Cdn cdn) { this.cdn = cdn; }
    public Long getLotId() { return lotId; }
    public void setLotId(Long lotId) { this.lotId = lotId; }
    public Long getCommodityId() { return commodityId; }
    public void setCommodityId(Long commodityId) { this.commodityId = commodityId; }
    public String getLotName() { return lotName; }
    public void setLotName(String lotName) { this.lotName = lotName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CdnItem)) return false;
        return getId() != null && getId().equals(((CdnItem) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
