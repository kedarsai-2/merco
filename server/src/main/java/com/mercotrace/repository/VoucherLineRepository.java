package com.mercotrace.repository;

import com.mercotrace.domain.VoucherLine;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for voucher lines (ledger debit/credit lines).
 */
@Repository
public interface VoucherLineRepository extends JpaRepository<VoucherLine, Long> {

    List<VoucherLine> findAllByVoucherHeaderIdOrderById(Long voucherHeaderId);

    /** Lines whose voucher is in date range, trader-scoped. For Financial Reports. */
    @Query(
        "SELECT vl FROM VoucherLine vl JOIN vl.voucherHeader v " +
        "WHERE v.traderId = :traderId AND v.voucherDate >= :dateFrom AND v.voucherDate <= :dateTo " +
        "ORDER BY v.voucherDate ASC, vl.id ASC"
    )
    List<VoucherLine> findAllByTraderIdAndVoucherDateBetween(
        @Param("traderId") Long traderId,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo
    );

    @Query(
        "SELECT vl FROM VoucherLine vl JOIN vl.voucherHeader v " +
        "WHERE v.traderId = :traderId AND v.voucherDate >= :dateFrom AND v.voucherDate <= :dateTo " +
        "ORDER BY v.voucherDate ASC, vl.id ASC"
    )
    Page<VoucherLine> findPageByTraderIdAndVoucherDateBetween(
        @Param("traderId") Long traderId,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        Pageable pageable
    );
}
