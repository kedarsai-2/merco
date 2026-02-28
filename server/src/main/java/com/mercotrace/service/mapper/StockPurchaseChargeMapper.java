package com.mercotrace.service.mapper;

import com.mercotrace.domain.StockPurchaseCharge;
import com.mercotrace.service.dto.StockPurchaseDTOs.PurchaseChargeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockPurchaseChargeMapper {

    @Mapping(source = "chargeName", target = "name")
    @Mapping(source = "chargeAmount", target = "amount")
    PurchaseChargeDTO toDto(StockPurchaseCharge entity);
}
