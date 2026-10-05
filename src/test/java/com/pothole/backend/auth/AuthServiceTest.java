package com.pothole.backend.auth;

import com.pothole.backend.auth.dto.RegisterRequestDTO;
import com.pothole.backend.user.User;
import com.pothole.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerHashesPasswordAndSavesUser() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                " Aqua ",
                "AQUA@EXAMPLE.COM",
                "password123"
        );
        when(userRepository.findByEmail("aqua@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = authService.register(request);

        assertEquals("Aqua", savedUser.getName());
        assertEquals("aqua@example.com", savedUser.getEmail());
        assertEquals("bcrypt-hash", savedUser.getPasswordHash());
        verify(passwordEncoder).encode("password123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("bcrypt-hash", userCaptor.getValue().getPasswordHash());
    }

    @Test
    void rejectsDuplicateEmailWithoutEncodingOrSaving() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Aqua",
                "aqua@example.com",
                "password123"
        );
        when(userRepository.findByEmail("aqua@example.com"))
                .thenReturn(Optional.of(new User("Existing", "aqua@example.com", "existing-hash")));

        assertThrows(DuplicateEmailException.class, () -> authService.register(request));
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginReturnsJwtForValidCredentials() {
        User user = new User("Aqua", "aqua@example.com", "bcrypt-hash");
        when(userRepository.findByEmail("aqua@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "bcrypt-hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        assertEquals("jwt-token", authService.login(
                new com.pothole.backend.auth.dto.LoginRequestDTO("AQUA@EXAMPLE.COM", "password123")));
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = new User("Aqua", "aqua@example.com", "bcrypt-hash");
        when(userRepository.findByEmail("aqua@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(
                new com.pothole.backend.auth.dto.LoginRequestDTO("aqua@example.com", "wrong-password")));
    }
}
