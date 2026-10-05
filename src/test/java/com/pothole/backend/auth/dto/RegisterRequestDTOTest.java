package com.pothole.backend.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterRequestDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void rejectsInvalidEmail() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Aqua",
                "invalid-email",
                "password123"
        );

        assertEquals(1, validator.validateProperty(request, "email").size());
    }

    @Test
    void rejectsShortPassword() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Aqua",
                "aqua@example.com",
                "short"
        );

        assertEquals(1, validator.validateProperty(request, "password").size());
    }

    @Test
    void acceptsValidRequest() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Aqua",
                "aqua@example.com",
                "password123"
        );

        assertTrue(validator.validate(request).isEmpty());
    }
}
