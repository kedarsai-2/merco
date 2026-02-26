package com.mercotrace.repository;

import com.mercotrace.domain.DailySerial;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailySerialRepository extends JpaRepository<DailySerial, Long> {

    Optional<DailySerial> findOneByTraderIdAndSerialDate(Long traderId, LocalDate serialDate);
}

