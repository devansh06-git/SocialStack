package com.socialstack.controller;

import com.socialstack.dto.AuthDto;
import com.socialstack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Body: { "identifier": "email or collegeId", "password": "...", "role": "STUDENT|FACULTY" }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@RequestBody AuthDto.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /**
     * POST /api/auth/register
     * Body: { "fullName", "email", "collegeId", "password", "role", "department" }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthDto.RegisterResponse> register(@Valid @RequestBody AuthDto.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }
}
