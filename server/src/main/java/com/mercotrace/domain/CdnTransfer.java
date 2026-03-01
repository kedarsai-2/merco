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
 * PIN-based CDN transfer. One-time PIN, expiry (REQ-CDN-009 to REQ-CDN-011).
 */
@Entity
@Table(name = "cdn_transfers")
@EntityListeners(AuditingEntityListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CdnTransfer implements Serializable {

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

    @NotNull
    @Column(name = "sender_trader_id", nullable = false)
    private Long senderTraderId;

    @Column(name = "receiver_trader_id")
    private Long receiverTraderId;

    @NotNull
    @Column(name = "pin_hash", nullable = false, length = 255)
    private String pinHash;

    @NotNull
    @Column(name = "pin_expiry", nullable = false)
    private Instant pinExpiry;

    @NotNull
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = Boolean.FALSE;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_arrival_id")
    private Long createdArrivalId;

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
    public Long getSenderTraderId() { return senderTraderId; }
    public void setSenderTraderId(Long senderTraderId) { this.senderTraderId = senderTraderId; }
    public Long getReceiverTraderId() { return receiverTraderId; }
    public void setReceiverTraderId(Long receiverTraderId) { this.receiverTraderId = receiverTraderId; }
    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
    public Instant getPinExpiry() { return pinExpiry; }
    public void setPinExpiry(Instant pinExpiry) { this.pinExpiry = pinExpiry; }
    public Boolean getIsUsed() { return isUsed; }
    public void setIsUsed(Boolean isUsed) { this.isUsed = isUsed; }
    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }
    public Long getCreatedArrivalId() { return createdArrivalId; }
    public void setCreatedArrivalId(Long createdArrivalId) { this.createdArrivalId = createdArrivalId; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CdnTransfer)) return false;
        return getId() != null && getId().equals(((CdnTransfer) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
