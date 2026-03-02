package com.mercotrace.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.mercotrace.IntegrationTest;
import com.mercotrace.domain.Role;
import com.mercotrace.repository.RoleRepository;
import com.mercotrace.service.criteria.RoleCriteria;
import com.mercotrace.service.dto.RoleDTO;
import java.time.Instant;
import javax.cache.Cache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link RoleQueryService} caching behaviour used by Settings RBAC.
 */
@IntegrationTest
@Transactional
class RoleQueryServiceCachingIT {

    @Autowired
    private RoleQueryService roleQueryService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private com.mercotrace.service.RoleService roleService;

    @Autowired
    private com.mercotrace.service.mapper.RoleMapper roleMapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        javax.cache.Cache<Object, Object> nativeCache = toNativeRolesPageCache();
        nativeCache.clear();
    }

    @AfterEach
    void tearDown() {
        javax.cache.Cache<Object, Object> nativeCache = toNativeRolesPageCache();
        nativeCache.clear();
    }

    @Test
    void findByCriteriaPopulatesCacheAndRoleWritesEvictAllEntries() {
        Role role = new Role();
        role.setRoleName("RBAC_CACHE_ROLE_QS");
        role.setCreatedAt(Instant.now());
        role = roleRepository.saveAndFlush(role);

        RoleCriteria criteria = new RoleCriteria();
        PageRequest pageable = PageRequest.of(0, 20);

        javax.cache.Cache<Object, Object> nativeCache = toNativeRolesPageCache();
        assertThat(nativeCache.iterator().hasNext()).isFalse();

        Page<RoleDTO> page = roleQueryService.findByCriteria(criteria, pageable);
        assertThat(page.getContent()).isNotEmpty();
        assertThat(nativeCache.iterator().hasNext()).isTrue();

        RoleDTO dto = roleMapper.toDto(role);
        dto.setId(null);
        dto.setRoleName("RBAC_CACHE_ROLE_QS_NEW");
        dto.setCreatedAt(Instant.now());
        roleService.save(dto);

        assertThat(nativeCache.iterator().hasNext()).isFalse();
    }

    @SuppressWarnings("unchecked")
    private javax.cache.Cache<Object, Object> toNativeRolesPageCache() {
        org.springframework.cache.Cache springCache = cacheManager.getCache(RoleQueryService.CACHE_ROLES_BY_CRITERIA_PAGE);
        if (springCache == null) {
            throw new IllegalStateException("rolesByCriteriaPage cache must be configured");
        }
        return (Cache<Object, Object>) springCache.getNativeCache();
    }
}

