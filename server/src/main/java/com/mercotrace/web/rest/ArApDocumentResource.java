package com.mercotrace.web.rest;

import com.mercotrace.domain.enumeration.ArApStatus;
import com.mercotrace.domain.enumeration.ArApType;
import com.mercotrace.service.ArApDocumentService;
import com.mercotrace.service.dto.ArApDocumentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

/**
 * REST controller for AR/AP documents (aging reports).
 * Base path: /api/arap-documents.
 */
@RestController
@RequestMapping("/api/arap-documents")
public class ArApDocumentResource {

    private static final Logger LOG = LoggerFactory.getLogger(ArApDocumentResource.class);

    private final ArApDocumentService arApDocumentService;

    public ArApDocumentResource(ArApDocumentService arApDocumentService) {
        this.arApDocumentService = arApDocumentService;
    }

    /**
     * GET /api/arap-documents : get paginated AR/AP documents, trader-scoped.
     * Query params: type (AR|AP), status (optional), page, size, sort.
     */
    @GetMapping
    public ResponseEntity<Page<ArApDocumentDTO>> getAll(
        @org.springdoc.core.annotations.ParameterObject
        @PageableDefault(size = 100, sort = "documentDate", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(required = false) ArApType type,
        @RequestParam(required = false) ArApStatus status
    ) {
        LOG.debug("REST request to get AR/AP documents: type={}, status={}", type, status);
        Page<ArApDocumentDTO> page = arApDocumentService.getPage(pageable, type, status);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }
}
