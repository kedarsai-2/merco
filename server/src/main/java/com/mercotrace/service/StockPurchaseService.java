package com.mercotrace.service;

import com.mercotrace.service.dto.StockPurchaseDTOs.CreateStockPurchaseRequestDTO;
import com.mercotrace.service.dto.StockPurchaseDTOs.StockPurchaseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for Stock Purchase: list (paginated, optional vendor search) and create.
 */
public interface StockPurchaseService {

    /**
     * Get paginated list of stock purchases for the current trader.
     * @param vendorSearch optional; when present filter by vendor name (contains, case-insensitive).
     */
    Page<StockPurchaseDTO> getPurchases(Pageable pageable, String vendorSearch);

    /**
     * Create a stock purchase. Validates vendor and items; resolves commodity by id or name.
     */
    StockPurchaseDTO create(CreateStockPurchaseRequestDTO request);

    /**
     * Get one purchase by id (for current trader).
     */
    StockPurchaseDTO getById(Long id);
}
