package com.mercotrace.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercotrace.IntegrationTest;
import com.mercotrace.domain.Contact;
import com.mercotrace.repository.ContactRepository;
import com.mercotrace.service.dto.ContactDTO;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ContactResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ContactResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/contacts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restContactMockMvc;

    private Contact contact;
    private Contact insertedContact;

    private String uniquePhone() {
        return "9" + String.format("%09d", Math.abs(UUID.randomUUID().hashCode() % 1000000000));
    }

    @BeforeEach
    void initTest() {
        contact = new Contact();
        contact.setTraderId(1L);
        contact.setName(DEFAULT_NAME);
        contact.setPhone(uniquePhone());
        contact.setOpeningBalance(BigDecimal.ZERO);
        contact.setCurrentBalance(BigDecimal.ZERO);
    }

    @AfterEach
    void cleanup() {
        if (insertedContact != null && insertedContact.getId() != null) {
            contactRepository.deleteById(insertedContact.getId());
            insertedContact = null;
        }
    }

    @Test
    @Transactional
    void createContact() throws Exception {
        long databaseSizeBeforeCreate = contactRepository.count();

        ContactDTO dto = new ContactDTO();
        dto.setTraderId(1L);
        dto.setName(DEFAULT_NAME);
        dto.setPhone(uniquePhone());

        String responseBody = restContactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dto)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ContactDTO returned = om.readValue(responseBody, ContactDTO.class);
        assertThat(contactRepository.count()).isEqualTo(databaseSizeBeforeCreate + 1);
        assertThat(returned.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(returned.getId()).isNotNull();
        insertedContact = new Contact();
        insertedContact.setId(returned.getId());
    }

    @Test
    @Transactional
    void createContactWithExistingIdFails() throws Exception {
        insertedContact = contactRepository.saveAndFlush(contact);
        ContactDTO dto = new ContactDTO();
        dto.setId(insertedContact.getId());
        dto.setTraderId(1L);
        dto.setName("Other");
        dto.setPhone(uniquePhone());

        restContactMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(dto)))
            .andExpect(status().isBadRequest());

        assertThat(contactRepository.count()).isEqualTo(1);
    }

    @Test
    @Transactional
    void getAllContacts() throws Exception {
        insertedContact = contactRepository.saveAndFlush(contact);

        restContactMockMvc
            .perform(get(ENTITY_API_URL))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(insertedContact.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getContact() throws Exception {
        insertedContact = contactRepository.saveAndFlush(contact);

        restContactMockMvc
            .perform(get(ENTITY_API_URL_ID, insertedContact.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(insertedContact.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingContact() throws Exception {
        restContactMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void updateContact() throws Exception {
        insertedContact = contactRepository.saveAndFlush(contact);
        long databaseSizeBeforeUpdate = contactRepository.count();

        Contact updatedContact = contactRepository.findById(insertedContact.getId()).orElseThrow();
        em.detach(updatedContact);
        updatedContact.setName(UPDATED_NAME);

        ContactDTO dto = new ContactDTO();
        dto.setId(updatedContact.getId());
        dto.setTraderId(updatedContact.getTraderId());
        dto.setName(updatedContact.getName());
        dto.setPhone(updatedContact.getPhone());
        dto.setOpeningBalance(updatedContact.getOpeningBalance());
        dto.setCurrentBalance(updatedContact.getCurrentBalance());

        restContactMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dto.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(dto))
            )
            .andExpect(status().isOk());

        assertThat(contactRepository.count()).isEqualTo(databaseSizeBeforeUpdate);
        Contact persisted = contactRepository.findById(insertedContact.getId()).orElseThrow();
        assertThat(persisted.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void deleteContact() throws Exception {
        insertedContact = contactRepository.saveAndFlush(contact);
        long databaseSizeBeforeDelete = contactRepository.count();

        restContactMockMvc
            .perform(delete(ENTITY_API_URL_ID, insertedContact.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertThat(contactRepository.count()).isEqualTo(databaseSizeBeforeDelete - 1);
        insertedContact = null;
    }
}
