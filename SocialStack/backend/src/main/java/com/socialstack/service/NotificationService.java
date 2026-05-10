package com.socialstack.service;

import com.socialstack.dto.NotificationDto;
import com.socialstack.entity.Event;
import com.socialstack.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private static final int MAX_SCHOOL_WIDE_EVENTS = 40;

    private final EventRepository eventRepo;

    public NotificationService(EventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> buildNotificationsForUser(Long userId, boolean student) {
        List<NotificationDto> submission = buildEventSubmissionNotifications(userId);
        if (!student) {
            return submission.stream()
                    .sorted(Comparator.comparing(NotificationDto::getAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        }
        List<NotificationDto> schoolWide = buildSchoolWideApprovedEvents(userId);
        List<NotificationDto> merged = new ArrayList<>(submission.size() + schoolWide.size());
        merged.addAll(submission);
        merged.addAll(schoolWide);
        merged.sort(Comparator.comparing(NotificationDto::getAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return merged;
    }

    private List<NotificationDto> buildSchoolWideApprovedEvents(Long userId) {
        List<NotificationDto> out = new ArrayList<>();
        List<Event> approved = eventRepo.findByStatusOrderByUpdatedAtDesc(Event.Status.APPROVED);
        int n = 0;
        for (Event e : approved) {
            if (n++ >= MAX_SCHOOL_WIDE_EVENTS) {
                break;
            }
            if (isOwnSubmissionApproval(userId, e)) {
                continue;
            }
            LocalDateTime at = Optional.ofNullable(e.getUpdatedAt()).orElse(e.getCreatedAt());
            String name = e.getEventName() != null ? e.getEventName() : "Event";
            String when = e.getEventDate() != null
                    ? e.getEventDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    : "TBD";
            String cat = e.getCategory() != null && !e.getCategory().isBlank() ? e.getCategory() : "General";
            String loc = e.getLocation() != null && !e.getLocation().isBlank() ? e.getLocation() : "TBA";
            out.add(NotificationDto.builder()
                    .kind("NEW_EVENT_PUBLISHED")
                    .title("New event: " + name)
                    .message("Published to Browse Events — " + when + " · " + cat + " · " + loc + ". Register before spots fill up.")
                    .icon("🎉")
                    .variant("info")
                    .eventId(e.getId())
                    .eventName(e.getEventName())
                    .at(at)
                    .build());
        }
        return out;
    }

    private boolean isOwnSubmissionApproval(Long userId, Event e) {
        return e.getSubmittedBy() != null && userId.equals(e.getSubmittedBy().getId());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> buildEventSubmissionNotifications(Long userId) {
        List<Event> mine = eventRepo.findBySubmittedBy_IdOrderByUpdatedAtDesc(userId);
        List<NotificationDto> out = new ArrayList<>();
        for (Event e : mine) {
            String name = e.getEventName() != null ? e.getEventName() : "Your event";
            LocalDateTime at = e.getUpdatedAt() != null ? e.getUpdatedAt() : e.getCreatedAt();
            switch (e.getStatus()) {
                case APPROVED -> out.add(NotificationDto.builder()
                        .kind("EVENT_APPROVED")
                        .title("Event approved")
                        .message("\"" + name + "\" is now approved. It appears in Browse Events for registration.")
                        .icon("✅")
                        .variant("success")
                        .eventId(e.getId())
                        .eventName(e.getEventName())
                        .at(at)
                        .build());
                case PENDING -> out.add(NotificationDto.builder()
                        .kind("EVENT_PENDING")
                        .title("Awaiting faculty approval")
                        .message("\"" + name + "\" is pending review.")
                        .icon("⏳")
                        .variant("warning")
                        .eventId(e.getId())
                        .eventName(e.getEventName())
                        .at(at)
                        .build());
                case REJECTED -> out.add(NotificationDto.builder()
                        .kind("EVENT_REJECTED")
                        .title("Event not approved")
                        .message("\"" + name + "\" was not approved. You can submit a revised event.")
                        .icon("❌")
                        .variant("danger")
                        .eventId(e.getId())
                        .eventName(e.getEventName())
                        .at(at)
                        .build());
                default -> { /* noop */ }
            }
        }
        return out;
    }
}
