package com.aircargo.controller;

import com.aircargo.config.ErrorMessages;
import com.aircargo.common.auth.JwtUtil;
import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.dto.ChangePasswordRequest;
import com.aircargo.dto.LoginRequest;
import com.aircargo.dto.LoginResponse;
import com.aircargo.dto.SetPasswordRequest;
import com.aircargo.dto.SiteDTO;
import com.aircargo.entity.AppUser;
import com.aircargo.entity.UserRole;
import com.aircargo.repository.AppUserRepository;
import com.aircargo.repository.SiteRepository;
import com.aircargo.service.ActiveSessionTracker;
import com.aircargo.service.AuditService;
import com.aircargo.service.MfaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final ActiveSessionTracker sessionTracker;
    private final SiteRepository siteRepository;
    private final MfaService mfaService;

    public AuthController(AppUserRepository userRepository, JwtUtil jwtUtil,
                          AuditService auditService, ActiveSessionTracker sessionTracker,
                          SiteRepository siteRepository, MfaService mfaService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
        this.sessionTracker = sessionTracker;
        this.siteRepository = siteRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.mfaService = mfaService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest servletRequest) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ErrorMessages.USER_NOT_FOUND));
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ErrorMessages.USER_INACTIVE));
        }

        String passwordHash = user.getPasswordHash();
        boolean hasPasswordSet = passwordHash != null && !passwordHash.isBlank();

        // Account lockout check
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", String.format(ErrorMessages.ACCOUNT_LOCKED,
                            user.getLockedUntil().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))));
        }

        if (hasPasswordSet) {
            if (request.password() == null || request.password().isBlank()) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).build();
            }
            if (!passwordEncoder.matches(request.password(), passwordHash)) {
                // Increment failed attempts
                int attempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    user.setLockedUntil(OffsetDateTime.now().plusMinutes(30));
                    userRepository.save(user);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", ErrorMessages.ACCOUNT_LOCKED_5_ATTEMPTS));
                }
                userRepository.save(user);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", String.format(ErrorMessages.WRONG_PASSWORD, 5 - attempts)));
            }
            // Successful password login — reset failed attempts
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        // MFA check — SuperUser can bypass MFA if not configured
        if (mfaService.isMfaRequired(user)) {
            if (user.getRole() != UserRole.SUPER_USER) {
                if (request.totpCode() == null || request.totpCode().isBlank()) {
                    return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                            .body(Map.of(
                                    "mfaRequired", true,
                                    "message", ErrorMessages.MFA_REQUIRED
                            ));
                }
                if (user.getMfaLocked()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", ErrorMessages.MFA_ACCOUNT_LOCKED));
                }
                if (!mfaService.verifyCode(user.getMfaSecret(), request.totpCode())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", ErrorMessages.MFA_CODE_INVALID));
                }
            }
        }

        user.setLastLogin(OffsetDateTime.now());
        userRepository.save(user);

        String airlineIdStr = user.getAirline() != null && user.getAirline().getId() != null
                ? user.getAirline().getId().toString() : "";

        String token = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getRole().name(),
                airlineIdStr,
                user.getEmail(),
                user.getFullName()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        auditService.logLogin(user.getId(), user.getEmail(), user.getFullName(), servletRequest.getRemoteAddr());

        sessionTracker.recordHeartbeat(user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getLastLogin());

        List<SiteDTO> userSites;
        if (user.getRole() == UserRole.SUPER_USER && user.getSites().isEmpty()) {
            userSites = siteRepository.findByIsActiveTrue().stream()
                    .map(SiteDTO::fromEntity)
                    .collect(Collectors.toList());
        } else {
            userSites = user.getSites().stream()
                    .map(SiteDTO::fromEntity)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(new LoginResponse(
                token,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getAirline() != null ? user.getAirline().getId() : null,
                hasPasswordSet,
                userSites,
                Boolean.TRUE.equals(user.getMustChangePassword()),
                user.getMfaEnabled()
        ));
    }

    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@Valid @RequestBody SetPasswordRequest request,
                                          HttpServletRequest servletRequest) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ErrorMessages.USER_NOT_FOUND));
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", ErrorMessages.USER_INACTIVE));
        }

        String currentHash = user.getPasswordHash();
        if (currentHash != null && !currentHash.isBlank()) {
            // User has an existing password — current password is required to change it
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", ErrorMessages.PASSWORD_REQUIRED));
            }
            if (!passwordEncoder.matches(request.currentPassword(), currentHash)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", ErrorMessages.CURRENT_PASSWORD_WRONG));
            }
        } else {
            // First-time setup — no current password needed
            if (request.currentPassword() != null && !request.currentPassword().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", ErrorMessages.NO_PREVIOUS_PASSWORD));
            }
        }

        // Revoke any existing access token so old session is invalidated
        String authHeader = servletRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtUtil.revokeToken(authHeader.substring(7));
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        auditService.log(user.getId(), user.getEmail(), user.getFullName(), "PASSWORD_SET",
                "USER", user.getId().toString(), null, servletRequest.getRemoteAddr());

        String airlineIdStr = user.getAirline() != null && user.getAirline().getId() != null
                ? user.getAirline().getId().toString() : "";

        String token = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getRole().name(),
                airlineIdStr,
                user.getEmail(),
                user.getFullName()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());
        return ResponseEntity.ok(Map.of(
                "message", "Contraseña establecida correctamente",
                "token", token,
                "refreshToken", refreshToken
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                             @AuthenticationPrincipal UserPrincipal principal,
                                             HttpServletRequest servletRequest) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AppUser user = userRepository.findById(principal.getUserIdAsUuid()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Always require MFA verification (SuperUser bypasses this)
        if (user.getRole() != UserRole.SUPER_USER) {
            if (!Boolean.TRUE.equals(user.getMfaEnabled()) || user.getMfaSecret() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", ErrorMessages.MFA_NOT_CONFIGURED
                ));
            }
            if (user.getMfaLocked()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", ErrorMessages.MFA_LOCKED));
            }
            if (!mfaService.verifyCode(user.getMfaSecret(), request.totpCode())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", ErrorMessages.TOTP_INVALID));
            }
        }

        // If not forced change, validate current password
        if (!Boolean.TRUE.equals(user.getMustChangePassword())) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", ErrorMessages.CURRENT_PASSWORD_NEEDED
                ));
            }
            if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", ErrorMessages.CURRENT_PASSWORD_WRONG));
            }
        }

        // Revoke the current access token before issuing a new one
        String authHeader = servletRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtUtil.revokeToken(authHeader.substring(7));
        }

        // Save new password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        auditService.log(user.getId(), user.getEmail(), user.getFullName(), "PASSWORD_CHANGED",
                "USER", user.getId().toString(), null, servletRequest.getRemoteAddr());

        // Generate new token
        String airlineIdStr = user.getAirline() != null && user.getAirline().getId() != null
                ? user.getAirline().getId().toString() : "";

        String token = jwtUtil.generateAccessToken(
                user.getId().toString(),
                user.getRole().name(),
                airlineIdStr,
                user.getEmail(),
                user.getFullName()
        );
        String refreshToken = jwtUtil.generateRefreshToken(user.getId().toString());

        return ResponseEntity.ok(Map.of(
                "message", "Contraseña cambiada correctamente",
                "token", token,
                "refreshToken", refreshToken
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AppUser user = userRepository.findById(principal.getUserIdAsUuid()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean hasPasswordSet = user.getPasswordHash() != null && !user.getPasswordHash().isBlank();
        List<SiteDTO> userSites;
        if (user.getRole() == UserRole.SUPER_USER && user.getSites().isEmpty()) {
            userSites = siteRepository.findByIsActiveTrue().stream()
                    .map(SiteDTO::fromEntity)
                    .collect(Collectors.toList());
        } else {
            userSites = user.getSites().stream()
                    .map(SiteDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(new LoginResponse(
                null, null, user.getId(), user.getEmail(), user.getFullName(),
                user.getRole(), user.getAirline() != null ? user.getAirline().getId() : null,
                hasPasswordSet, userSites,
                Boolean.TRUE.equals(user.getMustChangePassword()),
                user.getMfaEnabled()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", ErrorMessages.REFRESH_TOKEN_REQUIRED));
        }

        if (jwtUtil.isRevoked(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ErrorMessages.REFRESH_TOKEN_REVOCADO));
        }

        try {
            if (!jwtUtil.isValid(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", ErrorMessages.REFRESH_TOKEN_EXPIRED));
            }

            String tokenType = jwtUtil.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", ErrorMessages.REFRESH_TOKEN_WRONG_TYPE));
            }

            String userId = jwtUtil.parseToken(refreshToken).getSubject();
            AppUser user = userRepository.findById(
                    java.util.UUID.fromString(userId)).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", ErrorMessages.USER_NOT_FOUND));
            }
            if (!Boolean.TRUE.equals(user.getIsActive())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", ErrorMessages.USER_INACTIVE));
            }

            String airlineIdStr = user.getAirline() != null && user.getAirline().getId() != null
                    ? user.getAirline().getId().toString() : "";

            String newAccessToken = jwtUtil.generateAccessToken(
                    user.getId().toString(),
                    user.getRole().name(),
                    airlineIdStr,
                    user.getEmail(),
                    user.getFullName()
            );

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", refreshToken
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ErrorMessages.REFRESH_TOKEN_INVALID));
        }
    }

    @GetMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            sessionTracker.recordHeartbeat(
                    principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    principal.role(), OffsetDateTime.now());
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
