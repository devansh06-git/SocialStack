# SocialStack – Spring Boot Backend

## Tech Stack
- Java 17
- Spring Boot 3.2
- Spring Security + JWT (stateless)
- Spring Data JPA + Hibernate
- MySQL 8

---

## Quick Start

### 1. Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8 running locally

### 2. Database Setup
```sql
-- Run the provided schema file
source socialstack_db.sql;
```

### 3. Configure credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/socialstack_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 4. Run
```bash
cd backend
mvn spring-boot:run
```

Server starts on **http://localhost:8080**

---

## API Reference

### Auth  `(public)`
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login with email/collegeId + password |
| POST | `/api/auth/register` | Register a new user |

**Login request body:**
```json
{ "identifier": "anita.sharma@college.edu", "password": "password123", "role": "FACULTY" }
```

**Login response:**
```json
{ "token": "eyJ...", "userId": 1, "fullName": "Dr. Anita Sharma", "role": "FACULTY" }
```

---

### Events  `(GET = public, mutations = authenticated)`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/events` | List all events |
| GET | `/api/events/{id}` | Get single event |
| POST | `/api/events/submit` | Submit new event |
| PUT | `/api/events/{id}/approve` | ✅ FACULTY only |
| PUT | `/api/events/{id}/reject` | ✅ FACULTY only |

---

### Faculty Events  `(FACULTY only)`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/faculty-events/faculty/{userId}` | Faculty's assigned events |
| POST | `/api/faculty-events/assign` | Assign faculty to event |
| GET | `/api/faculty-events/faculty/{userId}/participants` | Participants in faculty's events |

---

### Registrations  `(authenticated)`
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/registrations/register` | Register student for event |
| GET | `/api/registrations/student/{userId}` | Student's registrations |
| DELETE | `/api/registrations/{regId}` | Cancel registration |

---

## Authentication
Protected endpoints need a Bearer token in the `Authorization` header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Store the token returned from `/api/auth/login` in `localStorage` and attach it to every request.

---

## Test Credentials (from seed data)
| Role | Email | Password |
|------|-------|----------|
| Faculty | anita.sharma@college.edu | password123 |
| Faculty | raj.mehta@college.edu | password123 |
| Student | vedant.shah@student.edu | password123 |
| Student | riya.patel@student.edu | password123 |

---

## Project Structure
```
backend/
├── pom.xml
└── src/main/java/com/socialstack/
    ├── SocialStackApplication.java
    ├── config/
    │   ├── SecurityConfig.java
    │   └── GlobalExceptionHandler.java
    ├── controller/
    │   ├── AuthController.java
    │   ├── EventController.java
    │   ├── FacultyEventController.java
    │   └── RegistrationController.java
    ├── dto/
    │   ├── AuthDto.java
    │   ├── EventDto.java
    │   ├── FacultyEventDto.java
    │   └── RegistrationDto.java
    ├── entity/
    │   ├── User.java
    │   ├── Event.java
    │   ├── FacultyEvent.java
    │   └── EventRegistration.java
    ├── repository/
    │   ├── UserRepository.java
    │   ├── EventRepository.java
    │   ├── FacultyEventRepository.java
    │   └── EventRegistrationRepository.java
    ├── security/
    │   ├── JwtUtils.java
    │   └── JwtAuthFilter.java
    └── service/
        ├── AuthService.java
        ├── EventService.java
        ├── FacultyEventService.java
        └── RegistrationService.java
```
