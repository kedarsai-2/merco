package com.mercotrace.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.mercotrace.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link com.mercotrace.web.rest.ArApDocumentResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ArApDocumentResourceIT {

    private static final String BASE_URL = "/api/arap-documents";

    @Autowired
    private MockMvc restArApDocumentMockMvc;

    @Test
    @Transactional
    void getAllArApDocuments() throws Exception {
        restArApDocumentMockMvc
            .perform(get(BASE_URL).param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Transactional
    void getAllArApDocumentsWithTypeFilter() throws Exception {
        restArApDocumentMockMvc
            .perform(get(BASE_URL).param("page", "0").param("size", "10").param("type", "AR"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
