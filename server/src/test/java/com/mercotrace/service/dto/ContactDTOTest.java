package com.mercotrace.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.mercotrace.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ContactDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ContactDTO.class);
        ContactDTO dto1 = new ContactDTO();
        dto1.setId(1L);
        ContactDTO dto2 = new ContactDTO();
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setId(dto1.getId());
        assertThat(dto1).isEqualTo(dto2);
        dto2.setId(2L);
        assertThat(dto1).isNotEqualTo(dto2);
        dto1.setId(null);
        assertThat(dto1).isNotEqualTo(dto2);
    }
}
