package com.mercotrace.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A DailySerial.
 */
@Entity
@Table(name = "daily_serial")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DailySerial extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "trader_id", nullable = false)
    private Long traderId;

    @Column(name = "serial_date", nullable = false)
    private LocalDate serialDate;

    @Column(name = "seller_serial", nullable = false)
    private Integer sellerSerial;

    @Column(name = "lot_serial", nullable = false)
    private Integer lotSerial;

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

    public LocalDate getSerialDate() {
        return serialDate;
    }

    public void setSerialDate(LocalDate serialDate) {
        this.serialDate = serialDate;
    }

    public Integer getSellerSerial() {
        return sellerSerial;
    }

    public void setSellerSerial(Integer sellerSerial) {
        this.sellerSerial = sellerSerial;
    }

    public Integer getLotSerial() {
        return lotSerial;
    }

    public void setLotSerial(Integer lotSerial) {
        this.lotSerial = lotSerial;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DailySerial)) {
            return false;
        }
        return getId() != null && getId().equals(((DailySerial) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DailySerial{" +
            "id=" + getId() +
            ", traderId=" + getTraderId() +
            ", serialDate=" + getSerialDate() +
            ", sellerSerial=" + getSellerSerial() +
            ", lotSerial=" + getLotSerial() +
            "}";
    }
}

