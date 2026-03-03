package com.mercotrace.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.mercotrace.domain.Contact;
import com.mercotrace.service.dto.ContactDTO;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContactMapperTest {

    private ContactMapper contactMapper;

    @BeforeEach
    void setUp() {
        contactMapper = new ContactMapperImpl();
    }

    @Test
    void toDtoMapsEntityToDto() {
        Contact entity = new Contact();
        entity.setId(1L);
        entity.setTraderId(10L);
        entity.setName("Test Contact");
        entity.setPhone("9876543210");
        entity.setMark("M1");
        entity.setAddress("Address");
        entity.setOpeningBalance(BigDecimal.TEN);
        entity.setCurrentBalance(BigDecimal.ONE);
        entity.setCreatedAt(Instant.now());
        entity.setType("SELLER");

        ContactDTO dto = contactMapper.toDto(entity);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTraderId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("Test Contact");
        assertThat(dto.getPhone()).isEqualTo("9876543210");
        assertThat(dto.getMark()).isEqualTo("M1");
        assertThat(dto.getOpeningBalance()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(dto.getCurrentBalance()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(dto.getType()).isEqualTo("SELLER");
    }

    @Test
    void toEntityMapsDtoToEntity() {
        ContactDTO dto = new ContactDTO();
        dto.setId(2L);
        dto.setTraderId(20L);
        dto.setName("DTO Contact");
        dto.setPhone("9123456789");
        dto.setOpeningBalance(BigDecimal.ZERO);
        dto.setCurrentBalance(BigDecimal.ZERO);

        Contact entity = contactMapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getTraderId()).isEqualTo(20L);
        assertThat(entity.getName()).isEqualTo("DTO Contact");
        assertThat(entity.getPhone()).isEqualTo("9123456789");
    }
}
