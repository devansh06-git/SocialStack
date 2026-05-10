-- ============================================================
--  SocialStack — Complete MySQL Database Schema
--  Covers: Faculty Dashboard + Student Panel (Dashboard1)
--  Spring Boot JPA will auto-create tables if spring.jpa.hibernate.ddl-auto=update
--  But use this for clean FROM-SCRATCH setup.
-- ============================================================

-- 1. Create & use your database
CREATE DATABASE IF NOT EXISTS socialstack_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE socialstack_db;

-- ============================================================
-- TABLE 1: users
--   Stores both Students and Faculty accounts.
--   The "role" column distinguishes them: 'STUDENT' or 'FACULTY'
--   (backend uses FACULTY for faculty – do not change this value)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(120)        NOT NULL,
    email        VARCHAR(180)        NOT NULL UNIQUE,
    college_id   VARCHAR(60)         UNIQUE,          -- optional college roll/staff ID
    password     VARCHAR(255)        NOT NULL,         -- BCrypt hashed
    role         ENUM('STUDENT','FACULTY') NOT NULL DEFAULT 'STUDENT',
    department   VARCHAR(100),
    phone        VARCHAR(20),
    is_active    BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME            ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE 2: events
--   All events – submitted by students or created by faculty.
--   status flow:  PENDING → APPROVED | REJECTED
-- ============================================================
CREATE TABLE IF NOT EXISTS events (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_name       VARCHAR(200)    NOT NULL,
    event_date       DATE,
    event_time       TIME,
    location         VARCHAR(200),
    category         VARCHAR(80),     -- e.g. technical, cultural, sports, workshop
    description      TEXT,
    organizer        VARCHAR(120),
    email            VARCHAR(180),
    max_participants INT             DEFAULT NULL,    -- NULL = unlimited
    status           ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    submitted_by     BIGINT          DEFAULT NULL,    -- FK → users.id (who submitted)
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_submitted_by FOREIGN KEY (submitted_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- TABLE 3: faculty_events  (Faculty ↔ Event role assignments)
--   Faculty can be HEAD or COORDINATOR of an event.
--   Maps to Java entity: FacultyEvent
-- ============================================================
CREATE TABLE IF NOT EXISTS faculty_events (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id    BIGINT          NOT NULL,   -- FK → users.id  (must be role=FACULTY)
    event_id    BIGINT          NOT NULL,   -- FK → events.id
    faculty_role  ENUM('HEAD','COORDINATOR') NOT NULL,
    assigned_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_facultyevents_faculty  FOREIGN KEY (faculty_id)  REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_facultyevents_event  FOREIGN KEY (event_id)  REFERENCES events(id)  ON DELETE CASCADE,
    UNIQUE KEY uq_faculty_event (faculty_id, event_id)   -- one faculty, one role per event
);

-- ============================================================
-- TABLE 4: event_registrations
--   Students register for approved events.
--   Maps to Java entity: EventRegistration
-- ============================================================
CREATE TABLE IF NOT EXISTS event_registrations (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT          NOT NULL,   -- FK → users.id (must be role=STUDENT)
    event_id     BIGINT          NOT NULL,   -- FK → events.id
    status       ENUM('REGISTERED','ATTENDED','CANCELLED','PENDING') NOT NULL DEFAULT 'REGISTERED',
    registered_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_student FOREIGN KEY (student_id) REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_reg_event   FOREIGN KEY (event_id)   REFERENCES events(id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_event (student_id, event_id)  -- one registration per student per event
);

-- ============================================================
-- INDEXES  (improves query performance for dashboards)
-- ============================================================
CREATE INDEX idx_events_status      ON events(status);
CREATE INDEX idx_events_category    ON events(category);
CREATE INDEX idx_events_date        ON events(event_date);
CREATE INDEX idx_faculty_events_faculty ON faculty_events(faculty_id);
CREATE INDEX idx_faculty_events_event ON faculty_events(event_id);
CREATE INDEX idx_reg_student        ON event_registrations(student_id);
CREATE INDEX idx_reg_event          ON event_registrations(event_id);
CREATE INDEX idx_users_role         ON users(role);

-- ============================================================
-- SAMPLE SEED DATA  (for testing – remove before production)
-- ============================================================

-- Faculty users  (password = "password123" BCrypt hash)
INSERT INTO users (full_name, email, college_id, password, role, department) VALUES
  ('Dr. Anita Sharma',  'anita.sharma@college.edu',  'FAC001', '$2a$10$e0NRtGqoF5F7cM2VrE/8.OlSmE5KzP4.XCMYt.mGMCvZIpVTh9Qy2', 'FACULTY', 'Computer Science'),
  ('Prof. Raj Mehta',   'raj.mehta@college.edu',     'FAC002', '$2a$10$e0NRtGqoF5F7cM2VrE/8.OlSmE5KzP4.XCMYt.mGMCvZIpVTh9Qy2', 'FACULTY', 'Electronics');

-- Student users  (password = "password123" BCrypt hash)
INSERT INTO users (full_name, email, college_id, password, role, department) VALUES
  ('Vedant Shah',       'vedant.shah@student.edu',   'STU001', '$2a$10$e0NRtGqoF5F7cM2VrE/8.OlSmE5KzP4.XCMYt.mGMCvZIpVTh9Qy2', 'STUDENT', 'CSE'),
  ('Riya Patel',        'riya.patel@student.edu',    'STU002', '$2a$10$e0NRtGqoF5F7cM2VrE/8.OlSmE5KzP4.XCMYt.mGMCvZIpVTh9Qy2', 'STUDENT', 'IT'),
  ('Arjun Verma',       'arjun.verma@student.edu',   'STU003', '$2a$10$e0NRtGqoF5F7cM2VrE/8.OlSmE5KzP4.XCMYt.mGMCvZIpVTh9Qy2', 'STUDENT', 'CSE');

-- Events
INSERT INTO events (event_name, event_date, event_time, location, category, description, organizer, email, max_participants, status, submitted_by) VALUES
  ('TechFest 2026',     '2026-05-10', '09:00:00', 'Main Auditorium', 'technical',  'Annual tech festival with workshops, hackathon and talks.', 'Dr. Anita Sharma', 'anita.sharma@college.edu', 200, 'APPROVED', 1),
  ('Cultural Night',    '2026-05-18', '18:00:00', 'Open Ground',     'cultural',   'Inter-college cultural performances and competitions.',       'Prof. Raj Mehta',  'raj.mehta@college.edu',    300, 'APPROVED', 2),
  ('Spring Workshop',   '2026-04-25', '10:00:00', 'Lab 204',         'workshop',   'Hands-on Spring Boot and React workshop for final years.',    'Vedant Shah',      'vedant.shah@student.edu',  50,  'PENDING',  3),
  ('Cricket League',    '2026-05-05', '07:30:00', 'Sports Ground',   'sports',     'Inter-department cricket tournament.',                        'Arjun Verma',      'arjun.verma@student.edu',  100, 'APPROVED', 5);

-- Faculty role assignments
INSERT INTO faculty_events (faculty_id, event_id, faculty_role) VALUES
  (1, 1, 'HEAD'),          -- Dr. Anita = Head of TechFest
  (2, 1, 'COORDINATOR'),   -- Prof. Raj = Coordinator of TechFest
  (2, 2, 'HEAD');          -- Prof. Raj = Head of Cultural Night

-- Student registrations
INSERT INTO event_registrations (student_id, event_id, status) VALUES
  (3, 1, 'REGISTERED'),    -- Vedant → TechFest
  (4, 1, 'REGISTERED'),    -- Riya   → TechFest
  (3, 2, 'REGISTERED'),    -- Vedant → Cultural Night
  (5, 4, 'REGISTERED');    -- Arjun  → Cricket

-- ============================================================
-- HOW TO CONNECT SPRING BOOT (application.properties)
-- ============================================================
-- spring.datasource.url=jdbc:mysql://localhost:3306/socialstack_db?useSSL=false&serverTimezone=UTC
-- spring.datasource.username=root
-- spring.datasource.password=YOUR_MYSQL_PASSWORD
-- spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
-- spring.jpa.hibernate.ddl-auto=update
-- spring.jpa.show-sql=true
-- spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
-- server.port=8080
-- spring.mvc.cors.allowed-origins=*

-- ============================================================
-- API ENDPOINTS SUMMARY (frontend ↔ backend mapping)
-- ============================================================
-- AUTH
--   POST /api/auth/login          { identifier, password, role }
--   POST /api/auth/register       { fullName, email, collegeId, password, role, department }
--
-- EVENTS (Faculty Dashboard & Student Panel)
--   GET  /api/events              → all events
--   POST /api/events/submit       → submit new event (PENDING)
--   PUT  /api/events/{id}/approve → approve (Faculty only)
--   PUT  /api/events/{id}/reject  → reject  (Faculty only)
--
-- FACULTY / FACULTY EVENTS
--   GET  /api/faculty-events/faculty/{userId}                 → faculty's events
--   POST /api/faculty-events/assign  { facultyId, eventId, facultyRole }
--   GET  /api/faculty-events/faculty/{userId}/participants    → participants in faculty events
--
-- STUDENT REGISTRATIONS
--   POST /api/registrations/register  { studentId, eventId }
--   GET  /api/registrations/student/{userId}              → student's registrations
--   DELETE /api/registrations/{regId}                     → cancel registration
--
-- ============================================================
-- PARTICIPANT COUNT HELPER VIEW (optional)
-- ============================================================
CREATE OR REPLACE VIEW event_participant_counts AS
SELECT
    e.id            AS event_id,
    e.event_name,
    e.max_participants,
    COUNT(er.id)    AS current_participants
FROM events e
LEFT JOIN event_registrations er ON er.event_id = e.id
    AND er.status IN ('REGISTERED','ATTENDED')
GROUP BY e.id, e.event_name, e.max_participants;
