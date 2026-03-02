package com.mercotrace.service;

import com.mercotrace.service.dto.UserRoleDTO;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing {@link com.mercotrace.domain.UserRole}.
 */
public interface UserRoleService {

    /**
     * Get a page of all user-role mappings.
     */
    Page<UserRoleDTO> findAll(Pageable pageable);

    /**
     * Get all role mappings for a given user.
     */
    List<UserRoleDTO> findByUserId(Long userId);

    /**
     * Replace the complete set of roles assigned to a user.
     *
     * @param userId   the user identifier.
     * @param roleIds  the desired role identifiers.
     */
    void setRolesForUser(Long userId, Set<Long> roleIds);
}

