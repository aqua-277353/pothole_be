package com.pothole.backend.auth;

import com.pothole.backend.auth.dto.RegisterRequestDTO;
import com.pothole.backend.auth.dto.RegisterResponseDTO;
import com.pothole.backend.auth.dto.LoginRequestDTO;
import com.pothole.backend.auth.dto.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RegisterResponseDTO.from(authService.register(request)));
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return new LoginResponseDTO(authService.login(request));
    }
}
