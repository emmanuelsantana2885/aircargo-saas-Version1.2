package com.aircargo.authservice.dto;

import com.aircargo.common.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank @StrongPassword String newPassword,
    String currentPassword,
    @NotBlank String totpCode
) {}
