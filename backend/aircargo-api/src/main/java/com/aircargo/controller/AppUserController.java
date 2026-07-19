package com.aircargo.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.dto.AppUserDTO;
import com.aircargo.dto.ConnectedUserDTO;
import com.aircargo.entity.AppUser;
import com.aircargo.repository.AppUserRepository;
import com.aircargo.service.ActiveSessionTracker;
import com.aircargo.service.AppUserService;
import com.aircargo.service.AuditService;
import com.aircargo.service.MfaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;
    private final AuditService auditService;
    private final ActiveSessionTracker sessionTracker;
    private final MfaService mfaService;
    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AppUserController(AppUserService appUserService, AuditService auditService,
                             ActiveSessionTracker sessionTracker, MfaService mfaService,
                             AppUserRepository userRepository) {
        this.appUserService = appUserService;
        this.auditService = auditService;
        this.sessionTracker = sessionTracker;
        this.mfaService = mfaService;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @GetMapping
    public List<AppUserDTO> getAll(@RequestParam(required = false) UUID airlineId) {
        return appUserService.getAll(airlineId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserDTO> getById(@PathVariable UUID id) {
        return appUserService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AppUserDTO> create(@Valid @RequestBody AppUserDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        if (dto.getSiteIds() == null || dto.getSiteIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        AppUserDTO created = appUserService.create(dto);
        auditService.logUserCreate(
                principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                created.getId(), created.getEmail(), request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppUserDTO> update(@PathVariable UUID id, @Valid @RequestBody AppUserDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        return appUserService.update(id, dto)
                .map(updated -> {
                    auditService.logUserUpdate(
                            principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                            id, "{\"role\":\"" + dto.getRole() + "\",\"isActive\":" + dto.getIsActive() + "}",
                            request.getRemoteAddr());
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        AppUserDTO existing = appUserService.getById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        if (existing.getId().equals(principal.getUserIdAsUuid())) {
            return ResponseEntity.badRequest().build();
        }
        boolean removed = appUserService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        auditService.logUserDelete(
                principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                id, existing.getEmail(), request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable UUID id,
                                            @AuthenticationPrincipal UserPrincipal principal,
                                            HttpServletRequest request) {
        AppUserDTO user = appUserService.getById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        appUserService.resetPassword(id);
        auditService.logPasswordReset(
                principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                id, user.getEmail(), request.getRemoteAddr());
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida"));
    }

    @GetMapping("/connected")
    public List<ConnectedUserDTO> getConnected() {
        return sessionTracker.getConnectedUsers();
    }

    @PostMapping("/{id}/mfa/setup")
    public ResponseEntity<?> setupMfa(@PathVariable UUID id) {
        AppUserDTO user = appUserService.getById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            return ResponseEntity.badRequest().body(Map.of("error", "MFA ya está habilitado para este usuario"));
        }
        String secret = mfaService.generateSecret();
        String otpAuthUrl = mfaService.getOtpAuthUrl(user.getEmail(), secret);
        return ResponseEntity.ok(Map.of(
                "secret", secret,
                "otpAuthUrl", otpAuthUrl
        ));
    }

    @PostMapping("/{id}/mfa/enable")
    public ResponseEntity<?> enableMfa(@PathVariable UUID id,
                                        @RequestBody Map<String, String> body,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request) {
        AppUserDTO user = appUserService.getById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        String secret = body.get("secret");
        String totpCode = body.get("totpCode");
        if (secret == null || totpCode == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "secret y totpCode son requeridos"));
        }
        if (!mfaService.verifyCode(secret, totpCode)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Código TOTP inválido"));
        }
        mfaService.enableMfa(id, secret);
        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                "MFA_ENABLED", "USER", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(Map.of("message", "MFA habilitado correctamente"));
    }

    @PostMapping("/{id}/mfa/disable")
    public ResponseEntity<?> disableMfa(@PathVariable UUID id,
                                         @AuthenticationPrincipal UserPrincipal principal,
                                         HttpServletRequest request) {
        AppUserDTO user = appUserService.getById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        mfaService.disableMfa(id);
        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                "MFA_DISABLED", "USER", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(Map.of("message", "MFA deshabilitado"));
    }

    @PostMapping("/{id}/mfa/lock")
    public ResponseEntity<?> lockMfa(@PathVariable UUID id,
                                      @AuthenticationPrincipal UserPrincipal principal,
                                      HttpServletRequest request) {
        AppUserDTO user = appUserService.getById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        mfaService.lockMfa(id);
        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                "MFA_LOCKED", "USER", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(Map.of("message", "Cuenta bloqueada"));
    }

    @PostMapping("/{id}/mfa/unlock")
    public ResponseEntity<?> unlockMfa(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request) {
        AppUserDTO user = appUserService.getById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        mfaService.unlockMfa(id);
        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                "MFA_UNLOCKED", "USER", id.toString(), null, request.getRemoteAddr());
        return ResponseEntity.ok(Map.of("message", "Cuenta desbloqueada"));
    }

    @PostMapping("/{id}/generate-temp-password")
    public ResponseEntity<?> generateTempPassword(@PathVariable UUID id,
                                                   @AuthenticationPrincipal UserPrincipal principal,
                                                   HttpServletRequest request) {
        AppUser user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        // Generate a 12-character secure password: uppercase, lowercase, digits, symbols
        String tempPassword = generateSecurePassword(12);

        // Hash and save
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                "TEMP_PASSWORD_GENERATED", "USER", id.toString(),
                "{\"email\":\"" + user.getEmail() + "\"}", request.getRemoteAddr());

        // Return the plain text password ONCE — it cannot be recovered after this
        return ResponseEntity.ok(Map.of(
                "tempPassword", tempPassword,
                "message", "Contraseña temporal generada. Compártela con el usuario una sola vez."
        ));
    }

    private String generateSecurePassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String symbols = "!@#$%^&*";
        String all = upper + lower + digits + symbols;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure at least one of each type
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(symbols.charAt(random.nextInt(symbols.length())));

        // Fill the rest randomly
        for (int i = password.length(); i < length; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // Shuffle the characters
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }

        return new String(chars);
    }
}
