package com.socialstack.controller;

import com.socialstack.dto.EventDto;
import com.socialstack.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * GET /api/events
     * Returns all events (public – used by both Student & Faculty dashboards).
     */
    @GetMapping
    public ResponseEntity<List<EventDto.EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    /**
     * GET /api/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventDto.EventResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    /**
     * POST /api/events/submit
     * Submit a new event (any authenticated user – student or faculty).
     */
    @PostMapping("/submit")
    public ResponseEntity<EventDto.EventResponse> submit(@RequestBody EventDto.SubmitRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.submitEvent(req));
    }

    /**
     * PUT /api/events/{id}/approve  (Faculty / FACULTY only)
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<EventDto.EventResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.approveEvent(id));
    }

    /**
     * PUT /api/events/{id}/reject   (Faculty / FACULTY only)
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<EventDto.EventResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.rejectEvent(id));
    }
}
