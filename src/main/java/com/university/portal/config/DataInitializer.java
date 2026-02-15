package com.university.portal.config;

import com.university.portal.entity.Course;
import com.university.portal.entity.User;
import com.university.portal.entity.UserRole;
import com.university.portal.repository.CourseRepository;
import com.university.portal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedCourses();
    }

    private void seedUsers() {
        createUserIfMissing("Student One", "student@example.com", UserRole.STUDENT);
        createUserIfMissing("Faculty One", "faculty@example.com", UserRole.FACULTY);
        createUserIfMissing("Admin One", "admin@example.com", UserRole.ADMIN);
    }

    private void createUserIfMissing(String name, String email, UserRole role) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(role);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
        log.info("Created seed user: {}", email);
    }

    private void seedCourses() {
        createCourseIfMissing("CSE101", "Programming Fundamentals", "Computer Science", "Faculty One");
        createCourseIfMissing("MAT201", "Discrete Mathematics", "Mathematics", "Faculty One");
        createCourseIfMissing("ECE210", "Digital Systems", "Electronics", "Faculty One");
    }

    private void createCourseIfMissing(String code, String title, String department, String facultyName) {
        if (courseRepository.existsByCode(code)) {
            return;
        }
        Course course = new Course();
        course.setCode(code);
        course.setTitle(title);
        course.setDepartment(department);
        course.setFacultyName(facultyName);
        course.setActive(true);
        courseRepository.save(course);
    }
}
