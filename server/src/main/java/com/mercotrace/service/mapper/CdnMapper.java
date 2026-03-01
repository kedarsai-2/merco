package com.mercotrace.service.mapper;

import com.mercotrace.domain.Cdn;
import com.mercotrace.domain.CdnItem;
import com.mercotrace.domain.enumeration.CdnSource;
import com.mercotrace.domain.enumeration.CdnStatus;
import com.mercotrace.service.dto.CDNDTOs.CDNLineItemDTO;
import com.mercotrace.service.dto.CDNDTOs.CDNResponseDTO;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Maps CDN entity to response DTO. Status/source converted to frontend-friendly strings.
 * Pin/pinUsed/pinExpiresAt and items are set by service layer.
 */
@Service
public class CdnMapper {

    /**
     * Map entity to response DTO. Caller must set items, pin, pinUsed, pinExpiresAt, dispatchingParty, receivingParty.
     */
    public CDNResponseDTO toResponseDto(Cdn entity) {
        if (entity == null) return null;
        CDNResponseDTO dto = new CDNResponseDTO();
        dto.setId(entity.getId());
        dto.setCdnNumber(entity.getCdnNumber());
        dto.setDate(entity.getCdnDate());
        dto.setFreightFormula(entity.getFreightFormula());
        dto.setTransporter(entity.getTransporterName());
        dto.setDriver(entity.getDriverName());
        dto.setAdvancePaid(entity.getAdvancePaid() != null ? entity.getAdvancePaid() : java.math.BigDecimal.ZERO);
        dto.setRemarks(entity.getRemarks());
        dto.setSource(toFrontendSource(entity.getSource()));
        dto.setStatus(toFrontendStatus(entity.getStatus(), entity.getCdnDate()));
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public CDNLineItemDTO toLineItemDto(CdnItem item) {
        if (item == null) return null;
        CDNLineItemDTO dto = new CDNLineItemDTO();
        dto.setId(item.getId() != null ? item.getId().toString() : null);
        dto.setLotName(item.getLotName());
        dto.setQuantity(item.getQuantity());
        dto.setVariant(item.getVariant());
        return dto;
    }

    public List<CDNLineItemDTO> toLineItemDtos(List<CdnItem> items) {
        if (items == null) return List.of();
        return items.stream().map(this::toLineItemDto).collect(Collectors.toList());
    }

    /** Backend CdnSource to frontend string (DIRECT → MANUAL for display). */
    public static String toFrontendSource(CdnSource source) {
        if (source == null) return null;
        return source == CdnSource.DIRECT ? "MANUAL" : source.name();
    }

    /** Backend CdnStatus to frontend: DISPATCHED→ACTIVE, TRANSFERRED→TRANSFERRED. EXPIRED is derived elsewhere from pin expiry. */
    public static String toFrontendStatus(CdnStatus status, java.time.Instant cdnDate) {
        if (status == null) return null;
        switch (status) {
            case DISPATCHED: return "ACTIVE";
            case TRANSFERRED: return "TRANSFERRED";
            case RECEIVED: return "TRANSFERRED";
            case DRAFT: return "ACTIVE";
            default: return status.name();
        }
    }
}
