package com.mercotrace.service;

import com.mercotrace.domain.enumeration.ArApStatus;
import com.mercotrace.domain.enumeration.ArApType;
import com.mercotrace.service.dto.ArApDocumentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Service for AR/AP documents (list for aging reports). */
public interface ArApDocumentService {

    Page<ArApDocumentDTO> getPage(Pageable pageable, ArApType type, ArApStatus status);
}
