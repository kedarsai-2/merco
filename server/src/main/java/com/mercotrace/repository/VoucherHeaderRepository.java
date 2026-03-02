package com.mercotrace.repository;

import com.mercotrace.domain.VoucherHeader;
import com.mercotrace.domain.enumeration.VoucherLifecycleStatus;
import com.mercotrace.domain.enumeration.VoucherType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for accounting voucher headers.
 * Scoped by trader; filter by type, status, search, date range.
 */
@Repository
public interface VoucherHeaderRepository extends JpaRepository<VoucherHeader, Long> {

    @Query(
        "SELECT v FROM VoucherHeader v WHERE v.traderId = :traderId " +
        "AND (:voucherType IS NULL OR v.voucherType = :voucherType) " +
        "AND (:status IS NULL OR v.status = :status) " +
        "AND (:dateFrom IS NULL OR v.voucherDate >= :dateFrom) " +
        "AND (:dateTo IS NULL OR v.voucherDate <= :dateTo) " +
        "AND (:search IS NULL OR :search = '' OR " +
        "  LOWER(v.voucherNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "  LOWER(v.narration) LIKE LOWER(CONCAT('%', :search, '%')))"
    )
    Page<VoucherHeader> findAllByTraderIdAndFilters(
        @Param("traderId") Long traderId,
        @Param("voucherType") VoucherType voucherType,
        @Param("status") VoucherLifecycleStatus status,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        @Param("search") String search,
        Pageable pageable
    );

    Optional<VoucherHeader> findOneByIdAndTraderId(Long id, Long traderId);

    long countByTraderIdAndVoucherType(Long traderId, VoucherType voucherType);
}
