package com.mercotrace.repository;

import com.mercotrace.domain.CdnTransfer;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * PIN-based CDN transfer. Find by pin hash for receive flow (REQ-CDN-009 to REQ-CDN-011).
 */
@Repository
public interface CdnTransferRepository extends JpaRepository<CdnTransfer, Long> {

    Optional<CdnTransfer> findFirstByCdnIdAndIsDeletedFalse(Long cdnId);

    @Query("SELECT t FROM CdnTransfer t JOIN FETCH t.cdn c WHERE t.isUsed = false AND t.isDeleted = false AND t.pinExpiry > :now")
    List<CdnTransfer> findAllByNotUsedAndNotExpired(@Param("now") Instant now);
}
