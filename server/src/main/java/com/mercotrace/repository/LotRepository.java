package com.mercotrace.repository;

import com.mercotrace.domain.Lot;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {

    List<Lot> findAllBySellerVehicleIdIn(Iterable<Long> sellerVehicleIds);

    Page<Lot> findAllByLotNameContainingIgnoreCase(String lotName, Pageable pageable);

    /**
     * Lots belonging to the given trader via Lot → SellerInVehicle → Vehicle (trader_id).
     * Single DB query with pagination and sort.
     */
    @Query(
        "SELECT l FROM Lot l WHERE l.sellerVehicleId IN (" +
        "SELECT siv.id FROM SellerInVehicle siv, Vehicle v WHERE siv.vehicleId = v.id AND v.traderId = :traderId)"
    )
    Page<Lot> findAllByTraderId(@Param("traderId") Long traderId, Pageable pageable);

    /**
     * Trader-scoped lots with lot name search (case-insensitive).
     */
    @Query(
        "SELECT l FROM Lot l WHERE l.sellerVehicleId IN (" +
        "SELECT siv.id FROM SellerInVehicle siv, Vehicle v WHERE siv.vehicleId = v.id AND v.traderId = :traderId) " +
        "AND LOWER(l.lotName) LIKE LOWER(CONCAT('%', :q, '%'))"
    )
    Page<Lot> findAllByTraderIdAndLotNameContainingIgnoreCase(@Param("traderId") Long traderId, @Param("q") String q, Pageable pageable);
}

