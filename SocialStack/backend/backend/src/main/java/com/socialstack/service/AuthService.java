package com.socialstack.service;

import com.socialstack.dto.AuthDto;
import com.socialstack.entity.User;
import com.socialstack.repository.UserRepository;
import com.socialstack.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository  userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils        jwtUtils;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepo        = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils        = jwtUtils;
    }

    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        // Try email first, then collegeId
        User user = userRepo.findByEmail(req.getIdentifier())
                .or(() -> userRepo.findByCollegeId(req.getIdentifier()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }

        // Optional role check if frontend sends it
        if (req.getRole() != null && !req.getRole().isBlank()) {
            String expected = req.getRole().toUpperCase();
            if (!user.getRole().name().equals(expected)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Account role does not match. Expected: " + expected);
            }
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        return new AuthDto.LoginResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getDepartment()
        );
    }

    public AuthDto.RegisterResponse register(AuthDto.RegisterRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (req.getCollegeId() != null && !req.getCollegeId().isBlank()
                && userRepo.existsByCollegeId(req.getCollegeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "College ID already registered");
        }

        User.Role role = User.Role.STUDENT;
        if (req.getRole() != null && req.getRole().equalsIgnoreCase("FACULTY")) {
            role = User.Role.FACULTY;
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .collegeId(req.getCollegeId())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .department(req.getDepartment())
                .phone(req.getPhone())
                .isActive(true)
                .build();

        user = userRepo.save(user);

        return new AuthDto.RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
