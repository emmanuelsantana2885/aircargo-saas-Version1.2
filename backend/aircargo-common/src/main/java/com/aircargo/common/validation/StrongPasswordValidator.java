package com.aircargo.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 100;
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;
        if (value.length() < MIN_LENGTH || value.length() > MAX_LENGTH) return false;
        return UPPER.matcher(value).find()
                && LOWER.matcher(value).find()
                && DIGIT.matcher(value).find()
                && SPECIAL.matcher(value).find();
    }
}
