package com.socialstack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.socialstack.entity.Event;
import com.socialstack.entity.FacultyEvent;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class FacultyEventDto {

    @Data
    public static class AssignRequest {
        private Long   facultyId;
        private Long   eventId;
        private String facultyRole;   // "HEAD" | "COORDINATOR"
    }

    @Data
    public static class FacultyEventResponse {
        private Long   id;
        private Long   facultyId;
        private String facultyName;
        private Long   eventId;
        private String eventName;
        private LocalDate eventDate;
        private LocalTime eventTime;
        private String location;
        private String category;
        private String facultyRole;
        private LocalDateTime assignedAt;

        public static FacultyEventResponse from(FacultyEvent ae) {
            FacultyEventResponse r = new FacultyEventResponse();
            Event ev = ae.getEvent();
            r.id         = ae.getId();
            r.facultyId    = ae.getFaculty().getId();
            r.facultyName  = ae.getFaculty().getFullName();
            r.eventId    = ev.getId();
            r.eventName  = ev.getEventName();
            r.eventDate  = ev.getEventDate();
            r.eventTime  = ev.getEventTime();
            r.location   = ev.getLocation();
            r.category   = ev.getCategory();
            r.facultyRole  = ae.getFacultyRole().name();
            r.assignedAt = ae.getAssignedAt();
            return r;
        }
    }

    @Data
    public static class ParticipantResponse {
        private Long   userId;
        private String fullName;
        private String email;
        private String collegeId;
        private String department;

        public static ParticipantResponse from(com.socialstack.entity.User u) {
            ParticipantResponse r = new ParticipantResponse();
            r.userId     = u.getId();
            r.fullName   = u.getFullName();
            r.email      = u.getEmail();
            r.collegeId  = u.getCollegeId();
            r.department = u.getDepartment();
            return r;
        }
    }

    /** One row per student registration on an event this faculty manages (Head or Coordinator). */
    @Data
    public static class ParticipantEventRow {
        private String studentName;
        private String studentEmail;
        private String studentCollegeId;
        private String studentDepartment;
        private String studentPhone;
        private String eventName;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate eventDate;
        private String eventCategory;
        private String eventLocation;
        private Long   eventId;
        private String facultyRole;
        private String registrationStatus;
    }
}
