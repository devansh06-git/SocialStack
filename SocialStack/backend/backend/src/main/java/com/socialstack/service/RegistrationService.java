package com.socialstack.service;

import com.socialstack.dto.RegistrationDto;
import com.socialstack.entity.Event;
import com.socialstack.entity.EventRegistration;
import com.socialstack.entity.User;
import com.socialstack.repository.EventRegistrationRepository;
import com.socialstack.repository.EventRepository;
import com.socialstack.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RegistrationService {

    private final EventRegistrationRepository regRepo;
    private final UserRepository              userRepo;
    private final EventRepository             eventRepo;

    public RegistrationService(EventRegistrationRepository regRepo,
                               UserRepository userRepo,
                               EventRepository eventRepo) {
        this.regRepo   = regRepo;
        this.userRepo  = userRepo;
        this.eventRepo = eventRepo;
    }

    // ── Register student for an event ────────────────────────────────────────
    public RegistrationDto.RegistrationResponse register(RegistrationDto.RegisterRequest req) {
        User student = userRepo.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        Event event = eventRepo.findById(req.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (event.getStatus() != Event.Status.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot register for a non-approved event");
        }

        if (regRepo.existsByStudentIdAndEventId(req.getStudentId(), req.getEventId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already registered for this event");
        }

        // Check capacity
        if (event.getMaxParticipants() != null) {
            long current = regRepo.countByEventIdAndStatusNot(
                    event.getId(), EventRegistration.RegistrationStatus.CANCELLED);
            if (current >= event.getMaxParticipants()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Event is at full capacity");
            }
        }

        EventRegistration reg = EventRegistration.builder()
                .student(student)
                .event(event)
                .status(EventRegistration.RegistrationStatus.REGISTERED)
                .build();

        return RegistrationDto.RegistrationResponse.from(regRepo.save(reg));
    }

    // ── Get student's registrations ───────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<RegistrationDto.RegistrationResponse> getByStudent(Long studentId) {
        return regRepo.findByStudentId(studentId)
                .stream()
                .map(RegistrationDto.RegistrationResponse::from)
                .collect(Collectors.toList());
    }

    // ── Cancel / delete registration ─────────────────────────────────────────
    public void cancel(Long regId) {
        EventRegistration reg = regRepo.findById(regId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found"));
        regRepo.delete(reg);
    }
}
