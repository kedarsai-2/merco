package com.mercotrace.repository;

import com.mercotrace.domain.Cdn;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for CDN. Trader-scoped for multi-tenant.
 */
@Repository
public interface CdnRepository extends JpaRepository<Cdn, Long> {

    Optional<Cdn> findByIdAndTraderIdAndIsDeletedFalse(Long id, Long traderId);

    Page<Cdn> findAllByTraderIdAndIsDeletedFalse(Long traderId, Pageable pageable);

    @Query("SELECT c FROM Cdn c WHERE c.traderId = :traderId AND c.isDeleted = false " +
           "AND (LOWER(c.cdnNumber) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(COALESCE(c.receivingPartyName, '')) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(COALESCE(c.dispatchingPartyName, '')) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Cdn> findAllByTraderIdAndSearchAndIsDeletedFalse(
        @Param("traderId") Long traderId,
        @Param("q") String q,
        Pageable pageable
    );

    Optional<Cdn> findFirstByTraderIdAndCdnNumberIgnoreCaseAndIsDeletedFalse(Long traderId, String cdnNumber);

    /** Max cdn_number serial for generating next number (e.g. CDN-0001 -> 1). */
    @Query(value = "SELECT MAX(CAST(SUBSTRING(c.cdn_number FROM 5) AS int)) FROM cdn c WHERE c.trader_id = :traderId AND c.is_deleted = false", nativeQuery = true)
    Integer findMaxCdnSerialByTrader(@Param("traderId") Long traderId);
}
