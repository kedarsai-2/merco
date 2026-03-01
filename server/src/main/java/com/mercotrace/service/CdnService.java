package com.mercotrace.service;

import com.mercotrace.service.dto.CDNDTOs.CDNCreateDTO;
import com.mercotrace.service.dto.CDNDTOs.CDNResponseDTO;
import com.mercotrace.service.dto.CDNDTOs.ReceiveByPINDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * CDN (Consignment Dispatch Note) service: create, list, get by id, receive by PIN.
 */
public interface CdnService {

    /**
     * Create CDN with items and generate PIN. Returns DTO including plain PIN (one-time display).
     */
    CDNResponseDTO create(CDNCreateDTO request);

    /**
     * Get CDN by id (trader-scoped). Returns null if not found or wrong trader.
     */
    CDNResponseDTO getById(Long id);

    /**
     * List CDNs with optional search (cdn_number, receiving_party, dispatching_party).
     */
    Page<CDNResponseDTO> list(Pageable pageable, String search);

    /**
     * Receive CDN by PIN. Validates PIN not used and not expired; marks transfer used and CDN TRANSFERRED.
     */
    CDNResponseDTO receiveByPin(ReceiveByPINDTO request);
}
