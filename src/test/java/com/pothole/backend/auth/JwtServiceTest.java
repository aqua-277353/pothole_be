package com.pothole.backend.auth;

import com.pothole.backend.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String TEST_SECRET =
            "test-secret-that-is-long-enough-for-hmac-sha256-signing-key";

    private final JwtService jwtService = new JwtService(TEST_SECRET, 3_600_000);

    @Test
    void generatesAndValidatesToken() {
        User user = new User("Aqua", "aqua@example.com", "bcrypt-hash");

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.validateToken(token));
        assertEquals("aqua@example.com", jwtService.extractUsername(token));
    }

    @Test
    void rejectsModifiedToken() {
        User user = new User("Aqua", "aqua@example.com", "bcrypt-hash");
        String token = jwtService.generateToken(user);

        assertFalse(jwtService.validateToken(token + "modified"));
    }
}
