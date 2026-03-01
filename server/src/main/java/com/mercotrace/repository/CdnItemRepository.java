package com.mercotrace.repository;

import com.mercotrace.domain.CdnItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CdnItemRepository extends JpaRepository<CdnItem, Long> {

    List<CdnItem> findAllByCdnIdAndIsDeletedFalse(Long cdnId);

    @Query("SELECT i FROM CdnItem i WHERE i.cdn.id = :cdnId AND i.cdn.traderId = :traderId AND i.isDeleted = false")
    List<CdnItem> findAllByCdnIdAndTraderId(@Param("cdnId") Long cdnId, @Param("traderId") Long traderId);
}
