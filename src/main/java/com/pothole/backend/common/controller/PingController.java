package com.pothole.backend.common.controller;

import com.pothole.backend.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/ping")
    public ApiResponse ping() {
        return new ApiResponse("pong");
    }
}