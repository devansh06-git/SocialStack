package com.socialstack.repository;

import com.socialstack.entity.FacultyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyEventRepository extends JpaRepository<FacultyEvent, Long> {

    List<FacultyEvent> findByFacultyId(Long facultyId);

    boolean existsByFacultyIdAndEventId(Long facultyId, Long eventId);

    @Query("""
        SELECT ae FROM FacultyEvent ae
        JOIN FETCH ae.event e
        JOIN FETCH ae.faculty a
        WHERE ae.faculty.id = :facultyId
    """)
    List<FacultyEvent> findByFacultyIdWithDetails(@Param("facultyId") Long facultyId);

    @Query("""
        SELECT DISTINCT er.student FROM EventRegistration er
        WHERE er.event.id IN (
            SELECT ae.event.id FROM FacultyEvent ae WHERE ae.faculty.id = :facultyId
        )
        AND er.status <> 'CANCELLED'
    """)
    List<com.socialstack.entity.User> findParticipantsByFacultyId(@Param("facultyId") Long facultyId);
}
