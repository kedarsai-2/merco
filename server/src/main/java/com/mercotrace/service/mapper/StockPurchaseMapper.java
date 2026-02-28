package com.mercotrace.service.mapper;

import com.mercotrace.domain.StockPurchase;
import com.mercotrace.service.dto.StockPurchaseDTOs.StockPurchaseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for {@link StockPurchase} to {@link StockPurchaseDTO}.
 * vendorName, items, charges, lotNumbers are set by the service.
 */
@Mapper(componentModel = "spring")
public interface StockPurchaseMapper {

    @Mapping(target = "vendorName", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "charges", ignore = true)
    @Mapping(target = "lotNumbers", ignore = true)
    @Mapping(source = "totalAmount", target = "subtotal")
    @Mapping(target = "totalCharges", source = "totalCharges")
    @Mapping(target = "grandTotal", ignore = true)
    @Mapping(source = "createdDate", target = "createdAt")
    StockPurchaseDTO toDto(StockPurchase entity);
}
