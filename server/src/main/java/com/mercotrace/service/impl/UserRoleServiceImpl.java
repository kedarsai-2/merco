package com.mercotrace.service.impl;

import com.mercotrace.domain.Role;
import com.mercotrace.domain.User;
import com.mercotrace.domain.UserRole;
import com.mercotrace.repository.RoleRepository;
import com.mercotrace.repository.UserRepository;
import com.mercotrace.repository.UserRoleRepository;
import com.mercotrace.service.UserRoleService;
import com.mercotrace.service.dto.UserRoleDTO;
import com.mercotrace.service.mapper.UserRoleMapper;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing {@link UserRole}.
 */
@Service
@Transactional
public class UserRoleServiceImpl implements UserRoleService {

    private static final Logger LOG = LoggerFactory.getLogger(UserRoleServiceImpl.class);

    /** Cache for paginated user-role mappings used by Settings RBAC (Role Allocation grid). */
    public static final String CACHE_USER_ROLES_PAGE = "userRolesPage";

    /** Cache for role mappings of a single user, keyed by userId. */
    public static final String CACHE_USER_ROLES_BY_USER = "userRolesByUser";

    private final UserRoleRepository userRoleRepository;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserRoleMapper userRoleMapper;

    public UserRoleServiceImpl(
        UserRoleRepository userRoleRepository,
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserRoleMapper userRoleMapper
    ) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_USER_ROLES_PAGE, unless = "#result == null || #result.empty")
    public Page<UserRoleDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all UserRole mappings");
        return userRoleRepository.findAll(pageable).map(userRoleMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_USER_ROLES_BY_USER, key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<UserRoleDTO> findByUserId(Long userId) {
        LOG.debug("Request to get UserRole for user {}", userId);
        return userRoleRepository.findByUserId(userId).stream().map(userRoleMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Caching(
        evict = {
            @CacheEvict(cacheNames = CACHE_USER_ROLES_PAGE, allEntries = true),
            @CacheEvict(cacheNames = CACHE_USER_ROLES_BY_USER, key = "#userId")
        }
    )
    public void setRolesForUser(Long userId, Set<Long> roleIds) {
        LOG.debug("Request to set roles {} for user {}", roleIds, userId);
        User user = userRepository.findById(userId).orElseThrow();
        List<Role> roles = roleIds.isEmpty()
            ? List.of()
            : roleRepository.findAllById(roleIds).stream().collect(Collectors.toList());

        userRoleRepository.deleteByUserId(user.getId());

        for (Role role : roles) {
            UserRole mapping = new UserRole().user(user).role(role);
            userRoleRepository.save(mapping);
        }
    }
}

