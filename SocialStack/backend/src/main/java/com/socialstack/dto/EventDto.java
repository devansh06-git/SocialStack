package com.socialstack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.socialstack.entity.Event;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventDto {

    // ── Submit / Create ─────────────────────────────────────────────────────
    @Data
    public static class SubmitRequest {
        private String eventName;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate eventDate;
        @JsonFormat(pattern = "HH:mm:ss")
        private LocalTime eventTime;
        private String location;
        private String category;
        private String description;
        private String organizer;
        private String email;
        private Integer maxParticipants;
        private Long submittedBy;        // userId of submitter
        /** Set when a faculty member requests Head/Coordinator for this event after approval. */
        private Long requestedFacultyId;
        private String requestedFacultyRole; // HEAD | COORDINATOR
    }

    // ── Response (sent to frontend) ─────────────────────────────────────────
    @Data
    public static class EventResponse {
        private Long   id;
        private String eventName;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate eventDate;
        @JsonFormat(pattern = "HH:mm:ss")
        private LocalTime eventTime;
        private String location;
        private String category;
        private String description;
        private String organizer;
        private String email;
        private Integer maxParticipants;
        private String status;
        private Long   submittedById;
        private String submittedByName;
        private Long   requestedFacultyId;
        private String requestedFacultyRole;

        public static EventResponse from(Event e) {
            EventResponse r = new EventResponse();
            r.id              = e.getId();
            r.eventName       = e.getEventName();
            r.eventDate       = e.getEventDate();
            r.eventTime       = e.getEventTime();
            r.location        = e.getLocation();
            r.category        = e.getCategory();
            r.description     = e.getDescription();
            r.organizer       = e.getOrganizer();
            r.email           = e.getEmail();
            r.maxParticipants = e.getMaxParticipants();
            r.status          = e.getStatus().name();
            if (e.getSubmittedBy() != null) {
                r.submittedById   = e.getSubmittedBy().getId();
                r.submittedByName = e.getSubmittedBy().getFullName();
            }
            r.requestedFacultyId   = e.getRequestedFacultyId();
            r.requestedFacultyRole = e.getRequestedFacultyRole();
            return r;
        }
    }
}
