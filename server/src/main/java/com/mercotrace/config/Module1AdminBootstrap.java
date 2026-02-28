package com.mercotrace.config;

import com.mercotrace.domain.Authority;
import com.mercotrace.domain.User;
import com.mercotrace.repository.AuthorityRepository;
import com.mercotrace.repository.UserRepository;
import com.mercotrace.security.AuthoritiesConstants;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Ensures that a Module 1 super admin user exists in dev profile,
 * aligned with frontend expectations (email login + 6-char password).
 */
@Component
@Profile("dev")
public class Module1AdminBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(Module1AdminBootstrap.class);

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;

    public Module1AdminBootstrap(UserRepository userRepository, AuthorityRepository authorityRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Authority names for Auctions module (RBAC); superadmin gets all of these. */
    private static final String[] AUCTION_AUTHORITIES = {
        AuthoritiesConstants.AUCTIONS_VIEW,
        AuthoritiesConstants.AUCTIONS_CREATE,
        AuthoritiesConstants.AUCTIONS_EDIT,
        AuthoritiesConstants.AUCTIONS_DELETE,
        AuthoritiesConstants.AUCTIONS_APPROVE,
    };

    @PostConstruct
    public void ensureSuperAdmin() {
        String email = "superadmin@mercotrace.com";
        String login = email; // login == email for module 1

        userRepository
            .findOneWithAuthoritiesByEmailIgnoreCase(email)
            .ifPresentOrElse(
                existing -> {
                    LOG.info("Module1 super admin already present: {}", existing.getLogin());
                    grantAuctionAuthoritiesIfMissing(existing);
                },
                () -> {
                    LOG.info("Creating Module1 super admin user {}", email);
                    User user = new User();
                    user.setLogin(login);
                    user.setFirstName("Super");
                    user.setLastName("Admin");
                    user.setEmail(email);
                    user.setActivated(true);
                    user.setLangKey(Constants.DEFAULT_LANGUAGE);
                    user.setPassword(passwordEncoder.encode("admin12")); // 6 characters, matches frontend rules
                    user.setCreatedBy(Constants.SYSTEM);
                    user.setCreatedDate(Instant.now());
                    user.setLastModifiedBy(Constants.SYSTEM);
                    user.setLastModifiedDate(Instant.now());

                    Set<Authority> authorities = new HashSet<>();
                    authorityRepository.findById(AuthoritiesConstants.ADMIN).ifPresent(authorities::add);
                    authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
                    for (String role : AUCTION_AUTHORITIES) {
                        authorityRepository.findById(role).ifPresent(authorities::add);
                    }
                    user.setAuthorities(authorities);

                    userRepository.save(user);
                    LOG.info("Module1 super admin created with email {}", email);
                }
            );
    }

    /** Ensures existing superadmin has all Auctions module authorities (fixes 403 after adding RBAC). */
    private void grantAuctionAuthoritiesIfMissing(User user) {
        Set<Authority> authorities = user.getAuthorities();
        boolean modified = false;
        for (String role : AUCTION_AUTHORITIES) {
            if (authorityRepository.findById(role).filter(a -> authorities.add(a)).isPresent()) {
                modified = true;
            }
        }
        if (modified) {
            userRepository.save(user);
            LOG.info("Module1 super admin granted Auctions authorities");
        }
    }
}

