package com.mercotrace.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mercotrace.web.rest.TestUtil;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ContactTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Contact.class);
        Contact contact1 = createContact(1L, "Name1", "9876543210");
        Contact contact2 = new Contact();
        assertThat(contact1).isNotEqualTo(contact2);

        contact2.setId(contact1.getId());
        assertThat(contact1).isEqualTo(contact2);

        contact2.setId(2L);
        assertThat(contact1).isNotEqualTo(contact2);
    }

    private static Contact createContact(Long id, String name, String phone) {
        Contact c = new Contact();
        c.setId(id);
        c.setTraderId(1L);
        c.setName(name);
        c.setPhone(phone);
        c.setOpeningBalance(BigDecimal.ZERO);
        c.setCurrentBalance(BigDecimal.ZERO);
        return c;
    }
}
