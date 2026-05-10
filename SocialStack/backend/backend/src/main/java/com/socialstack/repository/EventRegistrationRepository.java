package com.socialstack.repository;

import com.socialstack.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    List<EventRegistration> findByStudentId(Long studentId);
    Optional<EventRegistration> findByStudentIdAndEventId(Long studentId, Long eventId);
    boolean existsByStudentIdAndEventId(Long studentId, Long eventId);
    long countByEventIdAndStatusNot(Long eventId, EventRegistration.RegistrationStatus status);

    List<EventRegistration> findByEvent_IdInAndStatusNot(
            Collection<Long> eventIds,
            EventRegistration.RegistrationStatus status);

    @Query("""
            SELECT DISTINCT er FROM EventRegistration er
            JOIN FETCH er.event ev
            JOIN FETCH er.student st
            WHERE ev.id IN :eventIds
            AND er.status <> :excludedStatus
            """)
    List<EventRegistration> findAllForEventsWithDetails(
            @Param("eventIds") Collection<Long> eventIds,
            @Param("excludedStatus") EventRegistration.RegistrationStatus excludedStatus);
}
