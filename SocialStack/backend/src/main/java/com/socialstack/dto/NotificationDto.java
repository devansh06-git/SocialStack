package com.socialstack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    /** EVENT_APPROVED | EVENT_PENDING | EVENT_REJECTED */
    private String kind;
    private String title;
    private String message;
    private String icon;
    /** success (green), warning (amber), danger (red), info (indigo) */
    private String variant;
    private Long eventId;
    private String eventName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime at;
}
