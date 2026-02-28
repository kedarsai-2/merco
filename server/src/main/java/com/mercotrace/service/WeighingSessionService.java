package com.mercotrace.service;

import com.mercotrace.service.dto.WeighingSessionCreateRequest;
import com.mercotrace.service.dto.WeighingSessionDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for post-auction weighing sessions.
 */
public interface WeighingSessionService {

    /**
     * Create a weighing session (called when user completes weighing on frontend).
     */
    WeighingSessionDTO create(WeighingSessionCreateRequest request);

    /**
     * List sessions for current trader (paginated).
     */
    Page<WeighingSessionDTO> list(Pageable pageable);

    /**
     * Get session by id.
     */
    Optional<WeighingSessionDTO> getById(Long id);

    /**
     * Get session by bid number (first match for current trader).
     */
    Optional<WeighingSessionDTO> getByBidNumber(Integer bidNumber);
}
