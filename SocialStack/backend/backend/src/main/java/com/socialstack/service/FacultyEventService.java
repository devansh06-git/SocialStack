package com.socialstack.service;

import com.socialstack.dto.FacultyEventDto;
import com.socialstack.entity.EventRegistration;
import com.socialstack.entity.FacultyEvent;
import com.socialstack.entity.Event;
import com.socialstack.entity.User;
import com.socialstack.repository.EventRegistrationRepository;
import com.socialstack.repository.FacultyEventRepository;
import com.socialstack.repository.EventRepository;
import com.socialstack.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyEventService {

    private final FacultyEventRepository     facultyEventRepo;
    private final UserRepository             userRepo;
    private final EventRepository            eventRepo;
    private final EventRegistrationRepository regRepo;

    public FacultyEventService(FacultyEventRepository facultyEventRepo,
                               UserRepository userRepo,
                               EventRepository eventRepo,
                               EventRegistrationRepository regRepo) {
        this.facultyEventRepo = facultyEventRepo;
        this.userRepo         = userRepo;
        this.eventRepo        = eventRepo;
        this.regRepo          = regRepo;
    }

    // ── Get faculty's assigned events ────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<FacultyEventDto.FacultyEventResponse> getByFaculty(Long facultyId) {
        return facultyEventRepo.findByFacultyIdWithDetails(facultyId)
                .stream()
                .map(FacultyEventDto.FacultyEventResponse::from)
                .collect(Collectors.toList());
    }

    // ── Assign faculty to event ───────────────────────────────────────────────
    public FacultyEventDto.FacultyEventResponse assign(FacultyEventDto.AssignRequest req) {
        User faculty = userRepo.findById(req.getFacultyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty user not found"));

        if (faculty.getRole() != User.Role.FACULTY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an faculty/faculty");
        }

        Event event = eventRepo.findById(req.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (facultyEventRepo.existsByFacultyIdAndEventId(req.getFacultyId(), req.getEventId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Faculty already assigned to this event");
        }

        FacultyEvent ae = FacultyEvent.builder()
                .faculty(faculty)
                .event(event)
                .facultyRole(FacultyEvent.FacultyRole.valueOf(req.getFacultyRole().toUpperCase()))
                .build();

        return FacultyEventDto.FacultyEventResponse.from(facultyEventRepo.save(ae));
    }

    // ── Registrations for events this faculty is assigned to (one row per registration) ──
    @Transactional(readOnly = true)
    public List<FacultyEventDto.ParticipantEventRow> getParticipants(Long facultyId) {
        List<FacultyEvent> assignments = facultyEventRepo.findByFacultyIdWithDetails(facultyId);
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, FacultyEvent.FacultyRole> eventToRole = assignments.stream()
                .collect(Collectors.toMap(
                        fe -> fe.getEvent().getId(),
                        FacultyEvent::getFacultyRole,
                        (a, b) -> a));

        List<Long> eventIds = assignments.stream()
                .map(fe -> fe.getEvent().getId())
                .distinct()
                .collect(Collectors.toList());

        List<EventRegistration> regs = regRepo.findAllForEventsWithDetails(
                eventIds,
                EventRegistration.RegistrationStatus.CANCELLED);

        return regs.stream().map(reg -> {
            Event ev = reg.getEvent();
            FacultyEventDto.ParticipantEventRow row = new FacultyEventDto.ParticipantEventRow();
            User st = reg.getStudent();
            row.setStudentName(st.getFullName());
            row.setStudentEmail(st.getEmail());
            row.setStudentCollegeId(st.getCollegeId());
            row.setStudentDepartment(st.getDepartment());
            row.setStudentPhone(st.getPhone());
            row.setEventName(ev.getEventName());
            row.setEventDate(ev.getEventDate());
            row.setEventCategory(ev.getCategory());
            row.setEventLocation(ev.getLocation());
            row.setEventId(ev.getId());
            FacultyEvent.FacultyRole fr = eventToRole.get(ev.getId());
            row.setFacultyRole(fr != null ? fr.name() : "");
            row.setRegistrationStatus(reg.getStatus().name());
            return row;
        }).collect(Collectors.toList());
    }
}
