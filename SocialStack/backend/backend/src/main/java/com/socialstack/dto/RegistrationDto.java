package com.socialstack.dto;

import com.socialstack.entity.EventRegistration;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistrationDto {

    @Data
    public static class RegisterRequest {
        private Long studentId;
        private Long eventId;
    }

    @Data
    public static class RegistrationResponse {
        private Long   id;
        private Long   studentId;
        private String studentName;
        private Long   eventId;
        private String eventName;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate eventDate;
        private String category;
        private String status;
        private LocalDateTime registeredAt;

        public static RegistrationResponse from(EventRegistration reg) {
            RegistrationResponse r = new RegistrationResponse();
            r.id           = reg.getId();
            r.studentId    = reg.getStudent().getId();
            r.studentName  = reg.getStudent().getFullName();
            r.eventId      = reg.getEvent().getId();
            r.eventName    = reg.getEvent().getEventName();
            r.eventDate    = reg.getEvent().getEventDate();
            r.category     = reg.getEvent().getCategory();
            r.status       = reg.getStatus().name();
            r.registeredAt = reg.getRegisteredAt();
            return r;
        }
    }
}
