package com.mercotrace.service.impl;

import com.mercotrace.domain.ArApDocument;
import com.mercotrace.domain.Contact;
import com.mercotrace.domain.enumeration.ArApStatus;
import com.mercotrace.domain.enumeration.ArApType;
import com.mercotrace.repository.ArApDocumentRepository;
import com.mercotrace.service.ArApDocumentService;
import com.mercotrace.service.TraderContextService;
import com.mercotrace.service.dto.ArApDocumentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArApDocumentServiceImpl implements ArApDocumentService {

    private final ArApDocumentRepository arApDocumentRepository;
    private final TraderContextService traderContextService;

    public ArApDocumentServiceImpl(ArApDocumentRepository arApDocumentRepository, TraderContextService traderContextService) {
        this.arApDocumentRepository = arApDocumentRepository;
        this.traderContextService = traderContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArApDocumentDTO> getPage(Pageable pageable, ArApType type, ArApStatus status) {
        Long traderId = traderContextService.getCurrentTraderId();
        return arApDocumentRepository.findAllByTraderIdAndTypeAndStatus(traderId, type, status, pageable)
            .map(this::toDto);
    }

    private ArApDocumentDTO toDto(ArApDocument d) {
        ArApDocumentDTO o = new ArApDocumentDTO();
        o.setDocumentId(d.getId() != null ? d.getId().toString() : null);
        o.setTraderId(d.getTraderId() != null ? d.getTraderId().toString() : null);
        if (d.getContact() != null) {
            o.setContactId(d.getContact().getId() != null ? d.getContact().getId().toString() : null);
            o.setContactName(d.getContact().getName());
        }
        o.setLedgerId(d.getLedgerId() != null ? d.getLedgerId().toString() : null);
        o.setType(d.getType());
        o.setReferenceVoucherId(d.getReferenceVoucherId() != null ? d.getReferenceVoucherId().toString() : null);
        o.setReferenceNumber(d.getReferenceNumber());
        o.setOriginalAmount(d.getOriginalAmount());
        o.setOutstandingBalance(d.getOutstandingBalance());
        o.setStatus(d.getStatus());
        o.setDocumentDate(d.getDocumentDate());
        o.setCreatedAt(d.getCreatedDate());
        return o;
    }
}
