package com.mercotrace.web.rest;

import com.mercotrace.repository.UserRepository;
import com.mercotrace.service.UserService;
import com.mercotrace.service.MailService;
import com.mercotrace.service.TraderService;
import com.mercotrace.service.dto.AdminUserDTO;
import com.mercotrace.service.dto.Module1AuthDTO;
import com.mercotrace.service.dto.TraderDTO;
import com.mercotrace.web.rest.vm.LoginVM;
import com.mercotrace.web.rest.vm.ManagedUserVM;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Module 1 spec — auth paths: /api/auth/register, /api/auth/login, /api/auth/profile.
 * Delegates to existing JHipster auth/account.
 */
@RestController
@RequestMapping("/api/auth")
public class Module1AuthResource {

    private final UserService userService;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final com.mercotrace.web.rest.AccountResource accountResource;
    private final com.mercotrace.web.rest.AuthenticateController authenticateController;
    private final TraderService traderService;

    public Module1AuthResource(
        UserService userService,
        MailService mailService,
        UserRepository userRepository,
        com.mercotrace.web.rest.AccountResource accountResource,
        com.mercotrace.web.rest.AuthenticateController authenticateController,
        TraderService traderService
    ) {
        this.userService = userService;
        this.mailService = mailService;
        this.userRepository = userRepository;
        this.accountResource = accountResource;
        this.authenticateController = authenticateController;
        this.traderService = traderService;
    }

    /** Module 1 spec: POST /auth/register — Register Trader (Directory Listing only). */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody ManagedUserVM vm) {
        if (vm.getPassword() == null || vm.getPassword().length() < 4 || vm.getPassword().length() > 100) {
            throw new com.mercotrace.service.InvalidPasswordException();
        }
        var user = userService.registerUser(vm, vm.getPassword());
        mailService.sendActivationEmail(user);
    }

    /** Module 1 spec: POST /auth/login — Login User. Returns token + normalized user/trader payloads. */
    @PostMapping("/login")
    public ResponseEntity<Module1AuthDTO> login(@Valid @RequestBody LoginVM loginVM) {
        // Frontend sends an email and requires 6+ char password. Enforce that here.
        if (loginVM.getPassword() == null || loginVM.getPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }

         // Allow email-based login by resolving email -> internal login username
         String username = loginVM.getUsername();
         if (username != null && username.contains("@")) {
             userRepository
                 .findOneByEmailIgnoreCase(username.toLowerCase())
                 .ifPresent(user -> loginVM.setUsername(user.getLogin()));
         }

        // Delegate authentication to existing JWT controller
        ResponseEntity<com.mercotrace.web.rest.AuthenticateController.JWTToken> jwtResponse;
        try {
            jwtResponse = authenticateController.authorize(loginVM);
        } catch (Exception ex) {
            // Normalize authentication failures into a clean 401 with clear message
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (jwtResponse.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }

        String jwt = jwtResponse.getBody().getIdToken();

        // Fetch current authenticated user
        AdminUserDTO account = accountResource.getAccount();

        // TODO: Link User -> Trader properly; for now, use the first trader (id=1) as module-1 base trader.
        TraderDTO trader = traderService
            .findOne(1L)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trader not configured"));

        Module1AuthDTO dto = new Module1AuthDTO();
        dto.setToken(jwt);

        // Map user
        Module1AuthDTO.UserPayload userPayload = new Module1AuthDTO.UserPayload();
        if (account.getId() != null) {
            userPayload.setUserId(account.getId().toString());
        }
        if (trader.getId() != null) {
            userPayload.setTraderId(trader.getId().toString());
        }
        userPayload.setUsername(account.getLogin());
        userPayload.setActive(account.isActivated());
        userPayload.setCreatedAt(account.getCreatedDate() != null ? account.getCreatedDate().toString() : null);

        StringBuilder nameBuilder = new StringBuilder();
        if (account.getFirstName() != null) {
            nameBuilder.append(account.getFirstName());
        }
        if (account.getLastName() != null) {
            if (!nameBuilder.isEmpty()) {
                nameBuilder.append(" ");
            }
            nameBuilder.append(account.getLastName());
        }
        userPayload.setName(nameBuilder.toString());
        userPayload.setRole(account.getAuthorities() != null && !account.getAuthorities().isEmpty()
            ? account.getAuthorities().iterator().next()
            : "TRADER");

        dto.setUser(userPayload);

        // Map trader
        Module1AuthDTO.TraderPayload traderPayload = new Module1AuthDTO.TraderPayload();
        if (trader.getId() != null) {
            traderPayload.setTraderId(trader.getId().toString());
        }
        traderPayload.setBusinessName(trader.getBusinessName());
        traderPayload.setOwnerName(trader.getOwnerName());
        traderPayload.setAddress(trader.getAddress());
        traderPayload.setCategory(trader.getCategory());
        traderPayload.setApprovalStatus(trader.getApprovalStatus() != null ? trader.getApprovalStatus().name() : "PENDING");
        traderPayload.setBillPrefix(trader.getBillPrefix());
        traderPayload.setCreatedAt(trader.getCreatedAt() != null ? trader.getCreatedAt().toString() : null);
        traderPayload.setUpdatedAt(trader.getUpdatedAt() != null ? trader.getUpdatedAt().toString() : null);
        traderPayload.setShopPhotos(new String[0]);

        dto.setTrader(traderPayload);

        return ResponseEntity.status(jwtResponse.getStatusCode()).headers(jwtResponse.getHeaders()).body(dto);
    }

    /** Module 1 spec: GET /auth/profile — Get current user profile. */
    @GetMapping("/profile")
    public com.mercotrace.service.dto.AdminUserDTO getProfile() {
        return accountResource.getAccount();
    }

    /** Module 1 spec: PUT /auth/profile — Update user profile. */
    @PutMapping("/profile")
    public void updateProfile(@RequestBody com.mercotrace.service.dto.AdminUserDTO userDTO) {
        // Delegate to AccountResource without triggering bean validation on AdminUserDTO here.
        // AccountResource will use the current authenticated user and only the updated fields.
        accountResource.saveAccount(userDTO);
    }
}
