package com.mercotrace.service;

import com.mercotrace.service.dto.VoucherLineDTO;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Service for voucher lines (by date range for Financial Reports). */
public interface VoucherLineService {

    /**
     * All lines whose voucher date is between dateFrom and dateTo, trader-scoped.
     */
    List<VoucherLineDTO> getLinesByDateRange(LocalDate dateFrom, LocalDate dateTo);

    Page<VoucherLineDTO> getLinesByDateRange(LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
}
