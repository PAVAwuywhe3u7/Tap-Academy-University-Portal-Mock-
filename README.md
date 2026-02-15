# Tap Academy University Portal (Mock)

Full-stack University Web Portal for **Vel Tech Rangarajan Dr.Sagunthala R&D Institute of Science and Technology**, built with Java 17, Spring Boot, JWT security, MySQL, Thymeleaf, and Bootstrap 5.

## Overview

This project includes:
- Public university homepage
- Role-based authentication (`STUDENT`, `FACULTY`, `ADMIN`)
- Attendance management with reporting
- Assignment upload with AI-style auto feedback and grading
- Dedicated student, faculty, and admin dashboards
- Admin user/course management APIs

## Tech Stack

- Java 17
- Spring Boot 3.4.x
- Spring Data JPA
- Spring Security + JWT
- MySQL 8
- Lombok + Jakarta Validation
- Thymeleaf (SSR) + Bootstrap 5 + Font Awesome

## Architecture

- MVC + layered architecture
- Clean separation:
  - `controller`
  - `service`
  - `repository`
  - `entity`
  - `config`
  - `dto`
  - `exception`

## Folder Structure

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
- `database/schema.sql`
- `postman/university-portal.postman_collection.json`

## Key Modules

### 1) University Homepage
- Hero section, events, news, responsive navbar, and footer
- Thymeleaf templates + custom CSS

### 2) Authentication
- BCrypt password hashing
- JWT-based login flow
- Role-based authorization for protected APIs

### 3) Attendance Management
- Faculty marks attendance by class/date
- Admin fetches filtered reports with attendance percentage

### 4) Assignment Submission + AI Evaluation
- Student uploads assignment files
- Server stores metadata and file path
- AI evaluator generates:
  - content relevance score
  - grammar quality score
  - structure clarity score
  - originality score (mock)
  - final grade

### 5) Dashboards
- Student: attendance, submissions, grades
- Faculty: attendance operations + reports
- Admin: users, courses, system statistics

## Local Setup

### Prerequisites
- Java 17
- Maven 3.9+
- MySQL 8 running on `localhost:3306`

### Steps
1. Create database (optional because JDBC URL supports auto-create):
   ```sql
   CREATE DATABASE IF NOT EXISTS university_db;
   ```
2. Update DB credentials in:
   - `src/main/resources/application.properties`
3. Run the project:
   ```bash
   mvn spring-boot:run
   ```
4. Open:
   - `http://localhost:8080`

## Default Seed Accounts

- `student@example.com` / `password123`
- `faculty@example.com` / `password123`
- `admin@example.com` / `password123`

## API and Test Assets

- SQL schema: `database/schema.sql`
- Postman collection: `postman/university-portal.postman_collection.json`

## Presentation Points (for judges)

- Professional full-stack architecture with clear layering
- Secure auth flow with JWT and role-based authorization
- Real-world modules: attendance + assignment evaluation
- Clean responsive UI (Thymeleaf + Bootstrap + custom CSS)
- Ready-to-run project with schema and Postman collection
