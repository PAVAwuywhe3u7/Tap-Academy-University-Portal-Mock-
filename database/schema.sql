CREATE DATABASE IF NOT EXISTS university_db;
USE university_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BIT NOT NULL DEFAULT 1,
    account_non_expired BIT NOT NULL DEFAULT 1,
    account_non_locked BIT NOT NULL DEFAULT 1,
    credentials_non_expired BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(40) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    department VARCHAR(120) NOT NULL,
    faculty_name VARCHAR(120) NOT NULL,
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    class_name VARCHAR(120) NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course VARCHAR(120) NOT NULL,
    assignment_title VARCHAR(180),
    original_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(300) NOT NULL,
    feedback VARCHAR(2500),
    grade VARCHAR(5),
    submission_date DATETIME NOT NULL,
    content_score INT NOT NULL,
    grammar_score INT NOT NULL,
    structure_score INT NOT NULL,
    originality_score INT NOT NULL,
    total_score INT NOT NULL,
    CONSTRAINT fk_assignment_student FOREIGN KEY (student_id) REFERENCES users(id)
);
