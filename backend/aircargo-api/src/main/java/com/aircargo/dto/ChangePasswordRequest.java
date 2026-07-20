package com.aircargo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank @Size(min = 12, max = 100)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{12,}$",
             message = "La contraseña debe tener mínimo 12 caracteres, incluir mayúscula, minúscula, número y carácter especial")
    String newPassword,
    String currentPassword,
    @NotBlank String totpCode
) {}
