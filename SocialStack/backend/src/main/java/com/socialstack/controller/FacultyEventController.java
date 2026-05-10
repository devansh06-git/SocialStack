package com.socialstack.controller;

import com.socialstack.dto.FacultyEventDto;
import com.socialstack.service.FacultyEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty-events")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyEventController {

    private final FacultyEventService facultyEventService;

    public FacultyEventController(FacultyEventService facultyEventService) {
        this.facultyEventService = facultyEventService;
    }

    /**
     * GET /api/faculty-events/faculty/{userId}
     * Returns all events the faculty member is assigned to.
     */
    @GetMapping("/faculty/{userId}")
    public ResponseEntity<List<FacultyEventDto.FacultyEventResponse>> getByFaculty(@PathVariable Long userId) {
        return ResponseEntity.ok(facultyEventService.getByFaculty(userId));
    }

    /**
     * POST /api/faculty-events/assign
     * Assign a faculty member to an event.
     * Body: { "facultyId", "eventId", "facultyRole": "HEAD|COORDINATOR" }
     */
    @PostMapping("/assign")
    public ResponseEntity<FacultyEventDto.FacultyEventResponse> assign(
            @RequestBody FacultyEventDto.AssignRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facultyEventService.assign(req));
    }

    /**
     * GET /api/faculty-events/faculty/{userId}/participants
     * Returns one row per student registration for events this faculty is assigned to (Head/Coordinator).
     */
    @GetMapping("/faculty/{userId}/participants")
    public ResponseEntity<List<FacultyEventDto.ParticipantEventRow>> getParticipants(
            @PathVariable Long userId) {
        return ResponseEntity.ok(facultyEventService.getParticipants(userId));
    }
}
