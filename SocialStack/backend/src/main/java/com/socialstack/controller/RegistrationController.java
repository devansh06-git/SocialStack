package com.socialstack.controller;

import com.socialstack.dto.RegistrationDto;
import com.socialstack.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * POST /api/registrations/register
     * Body: { "studentId", "eventId" }
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationDto.RegistrationResponse> register(
            @RequestBody RegistrationDto.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.register(req));
    }

    /**
     * GET /api/registrations/student/{userId}
     * Returns all registrations for a student.
     */
    @GetMapping("/student/{userId}")
    public ResponseEntity<List<RegistrationDto.RegistrationResponse>> getByStudent(
            @PathVariable Long userId) {
        return ResponseEntity.ok(registrationService.getByStudent(userId));
    }

    /**
     * DELETE /api/registrations/{regId}
     * Cancel / remove a registration.
     */
    @DeleteMapping("/{regId}")
    public ResponseEntity<Void> cancel(@PathVariable Long regId) {
        registrationService.cancel(regId);
        return ResponseEntity.noContent().build();
    }
}
