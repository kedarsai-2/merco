package com.mercotrace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mercotrace.domain.Contact;
import com.mercotrace.repository.ContactRepository;
import com.mercotrace.service.dto.ContactDTO;
import com.mercotrace.service.mapper.ContactMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    private static final long TRADER_ID = 1L;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactMapper contactMapper;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private ContactServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(cacheManager.getCache(ContactServiceImpl.STOCK_PURCHASE_VENDORS_BY_TRADER_CACHE)).thenReturn(cache);
        service = new ContactServiceImpl(contactRepository, contactMapper, cacheManager);
    }

    @Test
    void saveSetsDefaultsWhenNull() {
        ContactDTO dto = new ContactDTO();
        dto.setTraderId(TRADER_ID);
        dto.setName("Contact");
        dto.setPhone("9876543210");
        Contact entity = new Contact();
        entity.setName("Contact");
        entity.setPhone("9876543210");
        when(contactMapper.toEntity(dto)).thenReturn(entity);
        when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(contactMapper.toDto(any(Contact.class))).thenAnswer(inv -> {
            Contact c = inv.getArgument(0);
            ContactDTO out = new ContactDTO();
            out.setId(c.getId());
            out.setOpeningBalance(c.getOpeningBalance());
            out.setCurrentBalance(c.getCurrentBalance());
            return out;
        });

        ContactDTO result = service.save(dto);

        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getOpeningBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.getCurrentBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void findOneReturnsEmptyWhenNotFound() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ContactDTO> result = service.findOne(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findOneReturnsDtoWhenFound() {
        Contact entity = new Contact();
        entity.setId(1L);
        entity.setName("C1");
        entity.setPhone("9876543210");
        entity.setOpeningBalance(BigDecimal.ZERO);
        entity.setCurrentBalance(BigDecimal.ZERO);
        ContactDTO dto = new ContactDTO();
        dto.setId(1L);
        dto.setName("C1");
        when(contactRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(contactMapper.toDto(entity)).thenReturn(dto);

        Optional<ContactDTO> result = service.findOne(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("C1");
    }

    @Test
    void findAllByTraderDelegatesToRepository() {
        Contact c = new Contact();
        c.setId(1L);
        c.setTraderId(TRADER_ID);
        c.setName("C");
        c.setPhone("9876543210");
        c.setOpeningBalance(BigDecimal.ZERO);
        c.setCurrentBalance(BigDecimal.ZERO);
        when(contactRepository.findAllByTraderId(TRADER_ID)).thenReturn(List.of(c));
        ContactDTO dto = new ContactDTO();
        dto.setId(1L);
        when(contactMapper.toDto(c)).thenReturn(dto);

        List<ContactDTO> result = service.findAllByTrader(TRADER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void searchByMarkReturnsFilteredWhenFragmentProvided() {
        Contact c1 = new Contact();
        c1.setId(1L);
        c1.setName("Alpha");
        c1.setPhone("9876543210");
        c1.setOpeningBalance(BigDecimal.ZERO);
        c1.setCurrentBalance(BigDecimal.ZERO);
        Contact c2 = new Contact();
        c2.setId(2L);
        c2.setName("Beta");
        c2.setPhone("9123456789");
        c2.setOpeningBalance(BigDecimal.ZERO);
        c2.setCurrentBalance(BigDecimal.ZERO);
        ContactDTO dto1 = new ContactDTO();
        dto1.setId(1L);
        dto1.setName("Alpha");
        dto1.setMark("M1");
        ContactDTO dto2 = new ContactDTO();
        dto2.setId(2L);
        dto2.setName("Beta");
        dto2.setMark("M2");
        when(contactRepository.findAllByTraderId(TRADER_ID)).thenReturn(List.of(c1, c2));
        when(contactMapper.toDto(c1)).thenReturn(dto1);
        when(contactMapper.toDto(c2)).thenReturn(dto2);

        List<ContactDTO> result = service.searchByMark(TRADER_ID, "alpha");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Alpha");
    }

    @Test
    void deleteCallsRepository() {
        service.delete(1L);
        verify(contactRepository).deleteById(1L);
    }
}
