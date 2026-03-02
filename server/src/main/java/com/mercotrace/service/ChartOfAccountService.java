package com.mercotrace.service;

import com.mercotrace.service.dto.ChartOfAccountCreateRequest;
import com.mercotrace.service.dto.ChartOfAccountDTO;
import com.mercotrace.service.dto.ChartOfAccountUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Service for Chart of Accounts (CoA) ledgers. */
public interface ChartOfAccountService {

    Page<ChartOfAccountDTO> getPage(Pageable pageable, String search, String accountingClass, String classification);
    ChartOfAccountDTO getById(Long id);
    ChartOfAccountDTO create(ChartOfAccountCreateRequest request);
    ChartOfAccountDTO update(Long id, ChartOfAccountUpdateRequest request);
    void delete(Long id);
}
