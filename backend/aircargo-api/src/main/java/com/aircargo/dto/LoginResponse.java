package com.aircargo.dto;

import com.aircargo.entity.UserRole;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
    String token,
    String refreshToken,
    UUID userId,
    String email,
    String fullName,
    UserRole role,
    UUID airlineId,
    boolean hasPasswordSet,
    List<SiteDTO> sites,
    boolean mustChangePassword,
    boolean mfaEnabled
) {}

