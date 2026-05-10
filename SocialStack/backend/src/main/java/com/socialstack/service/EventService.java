package com.socialstack.service;

import com.socialstack.dto.EventDto;
import com.socialstack.entity.Event;
import com.socialstack.entity.FacultyEvent;
import com.socialstack.entity.User;
import com.socialstack.repository.EventRepository;
import com.socialstack.repository.FacultyEventRepository;
import com.socialstack.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository        eventRepo;
    private final UserRepository         userRepo;
    private final FacultyEventRepository facultyEventRepo;

    public EventService(EventRepository eventRepo,
                        UserRepository userRepo,
                        FacultyEventRepository facultyEventRepo) {
        this.eventRepo        = eventRepo;
        this.userRepo         = userRepo;
        this.facultyEventRepo = facultyEventRepo;
    }

    // ── Get all events ───────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<EventDto.EventResponse> getAllEvents() {
        return eventRepo.findAll()
                .stream()
                .map(EventDto.EventResponse::from)
                .collect(Collectors.toList());
    }

    // ── Get event by id ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public EventDto.EventResponse getById(Long id) {
        Event e = findOrThrow(id);
        return EventDto.EventResponse.from(e);
    }

    // ── Submit new event ─────────────────────────────────────────────────────
    public EventDto.EventResponse submitEvent(EventDto.SubmitRequest req) {
        User submitter = null;
        if (req.getSubmittedBy() != null) {
            submitter = userRepo.findById(req.getSubmittedBy())
                    .orElse(null);
        }

        Long requestedFacultyId = req.getRequestedFacultyId();
        String requestedRole = req.getRequestedFacultyRole();
        if (requestedRole != null && !requestedRole.isBlank()) {
            requestedRole = requestedRole.trim().toUpperCase();
            if (requestedFacultyId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "requestedFacultyId is required when requestedFacultyRole is set");
            }
            User rf = userRepo.findById(requestedFacultyId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested faculty not found"));
            if (rf.getRole() != User.Role.FACULTY) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestedFacultyId must be a faculty account");
            }
            if (!requestedRole.equals("HEAD") && !requestedRole.equals("COORDINATOR")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestedFacultyRole must be HEAD or COORDINATOR");
            }
        } else {
            requestedRole = null;
        }

        Event event = Event.builder()
                .eventName(req.getEventName())
                .eventDate(req.getEventDate())
                .eventTime(req.getEventTime())
                .location(req.getLocation())
                .category(req.getCategory())
                .description(req.getDescription())
                .organizer(req.getOrganizer())
                .email(req.getEmail())
                .maxParticipants(req.getMaxParticipants())
                .status(Event.Status.PENDING)
                .submittedBy(submitter)
                .requestedFacultyId(requestedFacultyId)
                .requestedFacultyRole(requestedRole)
                .build();

        return EventDto.EventResponse.from(eventRepo.save(event));
    }

    // ── Approve event ────────────────────────────────────────────────────────
    public EventDto.EventResponse approveEvent(Long id) {
        Event event = findOrThrow(id);
        if (event.getStatus() == Event.Status.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot approve a rejected event");
        }
        event.setStatus(Event.Status.APPROVED);
        event = eventRepo.save(event);

        assignPendingFacultyIfRequested(event);

        return EventDto.EventResponse.from(findOrThrow(id));
    }

    private void assignPendingFacultyIfRequested(Event event) {
        Long fid = event.getRequestedFacultyId();
        String roleStr = event.getRequestedFacultyRole();
        if (fid == null || roleStr == null || roleStr.isBlank()) {
            return;
        }
        if (facultyEventRepo.existsByFacultyIdAndEventId(fid, event.getId())) {
            return;
        }
        User faculty = userRepo.findById(fid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Requested faculty missing"));
        if (faculty.getRole() != User.Role.FACULTY) {
            return;
        }
        FacultyEvent.FacultyRole fr;
        try {
            fr = FacultyEvent.FacultyRole.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }
        FacultyEvent fe = FacultyEvent.builder()
                .faculty(faculty)
                .event(event)
                .facultyRole(fr)
                .build();
        facultyEventRepo.save(fe);
    }

    // ── Reject event ─────────────────────────────────────────────────────────
    public EventDto.EventResponse rejectEvent(Long id) {
        Event event = findOrThrow(id);
        event.setStatus(Event.Status.REJECTED);
        return EventDto.EventResponse.from(eventRepo.save(event));
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private Event findOrThrow(Long id) {
        return eventRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + id));
    }
}
