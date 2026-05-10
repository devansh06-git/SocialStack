package com.socialstack.repository;

import com.socialstack.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(Event.Status status);
    List<Event> findBySubmittedById(Long userId);

    List<Event> findBySubmittedBy_IdOrderByUpdatedAtDesc(Long submittedByUserId);

    List<Event> findByStatusOrderByUpdatedAtDesc(Event.Status status);
}
