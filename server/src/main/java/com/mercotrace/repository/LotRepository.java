package com.mercotrace.repository;

import com.mercotrace.domain.Lot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {

    List<Lot> findAllBySellerVehicleIdIn(Iterable<Long> sellerVehicleIds);
}

