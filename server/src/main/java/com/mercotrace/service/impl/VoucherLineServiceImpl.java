package com.mercotrace.service.impl;

import com.mercotrace.domain.VoucherLine;
import com.mercotrace.repository.VoucherLineRepository;
import com.mercotrace.service.TraderContextService;
import com.mercotrace.service.VoucherLineService;
import com.mercotrace.service.dto.VoucherLineDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VoucherLineServiceImpl implements VoucherLineService {

    private final VoucherLineRepository voucherLineRepository;
    private final TraderContextService traderContextService;

    public VoucherLineServiceImpl(VoucherLineRepository voucherLineRepository, TraderContextService traderContextService) {
        this.voucherLineRepository = voucherLineRepository;
        this.traderContextService = traderContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherLineDTO> getLinesByDateRange(LocalDate dateFrom, LocalDate dateTo) {
        Long traderId = traderContextService.getCurrentTraderId();
        return voucherLineRepository.findAllByTraderIdAndVoucherDateBetween(traderId, dateFrom, dateTo)
            .stream()
            .map(line -> toDto(line))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherLineDTO> getLinesByDateRange(LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        Long traderId = traderContextService.getCurrentTraderId();
        return voucherLineRepository.findPageByTraderIdAndVoucherDateBetween(traderId, dateFrom, dateTo, pageable)
            .map(this::toDto);
    }

    private VoucherLineDTO toDto(VoucherLine line) {
        VoucherLineDTO d = new VoucherLineDTO();
        d.setLineId(line.getId() != null ? line.getId().toString() : null);
        d.setVoucherId(line.getVoucherHeader() != null && line.getVoucherHeader().getId() != null
            ? line.getVoucherHeader().getId().toString() : null);
        d.setLedgerId(line.getLedgerId() != null ? line.getLedgerId().toString() : null);
        d.setLedgerName(line.getLedgerName());
        d.setDebit(line.getDebit());
        d.setCredit(line.getCredit());
        if (line.getCommodityId() != null) d.setCommodityId(line.getCommodityId().toString());
        d.setCommodityName(line.getCommodityName());
        d.setQuantity(line.getQuantity());
        d.setRate(line.getRate());
        if (line.getLotId() != null) d.setLotId(line.getLotId().toString());
        return d;
    }
}
