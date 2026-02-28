package com.mercotrace.service.mapper;

import com.mercotrace.domain.StockPurchaseItem;
import com.mercotrace.service.dto.StockPurchaseDTOs.PurchaseLineItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockPurchaseItemMapper {

    @Mapping(source = "commodityName", target = "commodity")
    PurchaseLineItemDTO toDto(StockPurchaseItem entity);
}
