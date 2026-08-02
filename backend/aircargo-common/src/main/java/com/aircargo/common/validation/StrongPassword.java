package com.aircargo.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "La contraseña debe tener al menos 12 caracteres e incluir mayúsculas, minúsculas, números y caracteres especiales.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
