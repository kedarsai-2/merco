package com.mercotrace.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mercotrace.IntegrationTest;
import com.mercotrace.domain.Role;
import com.mercotrace.domain.User;
import com.mercotrace.repository.RoleRepository;
import com.mercotrace.repository.UserRepository;
import com.mercotrace.repository.UserRoleRepository;
import com.mercotrace.service.dto.UserRoleDTO;
import com.mercotrace.service.impl.UserRoleServiceImpl;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.cache.Cache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link UserRoleService} caching behaviour.
 */
@IntegrationTest
@Transactional
class UserRoleServiceCachingIT {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private CacheManager cacheManager;

    private User user;

    private Role role;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLogin("rbac-cache-user");
        user.setPassword("x");
        user.setActivated(true);
        user.setEmail("rbac-cache-user@example.com");
        user.setFirstName("RBAC");
        user.setLastName("Cache");
        user = userRepository.saveAndFlush(user);

        role = new Role();
        role.setRoleName("RBAC_CACHE_ROLE");
        role = roleRepository.saveAndFlush(role);

        javax.cache.Cache<Object, Object> nativeCache = toNativeUserRolesByUserCache();
        nativeCache.clear();
    }

    @AfterEach
    void tearDown() {
        javax.cache.Cache<Object, Object> nativeCache = toNativeUserRolesByUserCache();
        nativeCache.clear();
        userRoleRepository.deleteAll();
        if (role != null && role.getId() != null) {
            roleRepository.deleteById(role.getId());
        }
        if (user != null && user.getId() != null) {
            userRepository.deleteById(user.getId());
        }
    }

    @Test
    void findByUserIdCachesResultAndSetRolesForUserEvictsEntry() {
        Long userId = user.getId();
        Long roleId = role.getId();
        assertThat(userId).isNotNull();
        assertThat(roleId).isNotNull();

        javax.cache.Cache<Object, Object> nativeCache = toNativeUserRolesByUserCache();
        assertThat(nativeCache.iterator().hasNext()).isFalse();

        // initial assignment
        userRoleService.setRolesForUser(userId, Set.of(roleId));
        List<UserRoleDTO> firstCall = userRoleService.findByUserId(userId);
        assertThat(firstCall).isNotEmpty();

        assertThat(nativeCache.iterator().hasNext()).isTrue();

        // change assignments; this should evict the cached entry
        userRoleService.setRolesForUser(userId, Set.of());

        assertThat(nativeCache.iterator().hasNext()).isFalse();
    }

    @SuppressWarnings("unchecked")
    private javax.cache.Cache<Object, Object> toNativeUserRolesByUserCache() {
        org.springframework.cache.Cache springCache = cacheManager.getCache(UserRoleServiceImpl.CACHE_USER_ROLES_BY_USER);
        Objects.requireNonNull(springCache, "userRolesByUser cache must be configured");
        return (Cache<Object, Object>) springCache.getNativeCache();
    }
}

