package com.aircargo.authservice.dto;

import com.aircargo.common.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SetPasswordRequest(
    @NotBlank @Email String email,
    @NotBlank @StrongPassword String newPassword,
    String currentPassword
) {}

