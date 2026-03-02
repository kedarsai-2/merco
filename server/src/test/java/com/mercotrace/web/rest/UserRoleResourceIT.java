package com.mercotrace.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercotrace.IntegrationTest;
import com.mercotrace.domain.Role;
import com.mercotrace.domain.User;
import com.mercotrace.domain.UserRole;
import com.mercotrace.repository.RoleRepository;
import com.mercotrace.repository.UserRepository;
import com.mercotrace.repository.UserRoleRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class UserRoleResourceIT {

    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private User user;

    private Role role1;

    private Role role2;

    @BeforeEach
    void initTest() {
        user = UserResourceIT.createEntity();
        user = userRepository.saveAndFlush(user);

        role1 = RoleResourceIT.createEntity();
        role1 = roleRepository.saveAndFlush(role1);

        role2 = RoleResourceIT.createUpdatedEntity();
        role2 = roleRepository.saveAndFlush(role2);
    }

    @Test
    @Transactional
    void setAndListUserRoles() throws Exception {
        // assign role1 and role2
        restMockMvc
            .perform(
                put("/api/admin/users/{id}/roles", user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(Set.of(role1.getId(), role2.getId())))
            )
            .andExpect(status().isNoContent());

        List<UserRole> mappings = userRoleRepository.findByUserId(user.getId());
        assertThat(mappings).hasSize(2);

        // list via API
        restMockMvc
            .perform(get("/api/admin/user-roles?page=0&size=20"))
            .andExpect(status().isOk());

        restMockMvc
            .perform(get("/api/admin/users/{id}/roles", user.getId()))
            .andExpect(status().isOk());

        // Repository-level assertion: mappings replaced as expected
        List<UserRole> after = userRoleRepository.findByUserId(user.getId());
        assertThat(after).hasSize(2);
    }
}

