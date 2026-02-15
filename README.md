# University Web Portal

Full-stack university management portal built with Java 17, Spring Boot, JWT security, MySQL, Thymeleaf, and Bootstrap 5.

## Tech Stack
- Java 17
- Spring Boot 3.4.x
- Spring Data JPA
- Spring Security + JWT
- MySQL
- Lombok + Jakarta Validation
- Thymeleaf + Bootstrap 5 + Font Awesome

## Project Structure
- `src/main/java/com/university/portal/controller`
- `src/main/java/com/university/portal/service`
- `src/main/java/com/university/portal/repository`
- `src/main/java/com/university/portal/entity`
- `src/main/java/com/university/portal/config`
- `src/main/java/com/university/portal/dto`
- `src/main/java/com/university/portal/exception`
- `src/main/resources/templates`
- `src/main/resources/static/css`
- `src/main/resources/static/js`

## Modules Implemented
- University homepage
- JWT authentication (Student / Faculty / Admin)
- Attendance management (faculty marking + admin reporting)
- Assignment upload (PDF/DOCX/images) + AI-style evaluation
- Student, faculty, and admin dashboards
- User and course management for admin

## Quick Start
1. Create MySQL database (or let JDBC create it): `university_db`.
2. Update DB credentials in `src/main/resources/application.properties` if needed.
3. Run:
   ```bash
   mvn spring-boot:run
   ```
4. Open `http://localhost:8080`.

## Default Seed Accounts
- `student@example.com` / `password123`
- `faculty@example.com` / `password123`
- `admin@example.com` / `password123`

## DB Schema + Postman
- Schema SQL: `database/schema.sql`
- Postman collection: `postman/university-portal.postman_collection.json`
