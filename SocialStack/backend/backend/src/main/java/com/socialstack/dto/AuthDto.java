package com.socialstack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {

    // ── Login ───────────────────────────────────────────────────────────────
    @Data
    public static class LoginRequest {
        @NotBlank
        private String identifier;   // email OR collegeId
        @NotBlank
        private String password;
        private String role;         // optional hint: "STUDENT" | "FACULTY"
    }

    @Data
    public static class LoginResponse {
        private String token;
        private Long   userId;
        private String fullName;
        private String email;
        private String role;
        private String department;

        public LoginResponse(String token, Long userId, String fullName,
                             String email, String role, String department) {
            this.token      = token;
            this.userId     = userId;
            this.fullName   = fullName;
            this.email      = email;
            this.role       = role;
            this.department = department;
        }
    }

    // ── Register ────────────────────────────────────────────────────────────
    @Data
    public static class RegisterRequest {
        @NotBlank
        private String fullName;

        @NotBlank @Email
        private String email;

        private String collegeId;

        @NotBlank @Size(min = 6)
        private String password;

        private String role;         // "STUDENT" | "FACULTY"  (default: STUDENT)
        private String department;
        private String phone;
    }

    @Data
    public static class RegisterResponse {
        private Long   userId;
        private String fullName;
        private String email;
        private String role;

        public RegisterResponse(Long userId, String fullName, String email, String role) {
            this.userId   = userId;
            this.fullName = fullName;
            this.email    = email;
            this.role     = role;
        }
    }
}
