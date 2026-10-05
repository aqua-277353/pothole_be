package com.pothole.backend.auth.dto;

import com.pothole.backend.user.User;

public record RegisterResponseDTO(Long id, String name, String email) {

    public static RegisterResponseDTO from(User user) {
        return new RegisterResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}
