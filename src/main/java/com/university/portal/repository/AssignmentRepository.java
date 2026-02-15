package com.university.portal.repository;

import com.university.portal.entity.Assignment;
import com.university.portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByStudentId(Long studentId);
    List<Assignment> findByStudent(User student);
    List<Assignment> findByStudentAndCourse(User student, String course);
    List<Assignment> findByCourse(String course);
    List<Assignment> findByStudentAndSubmissionDateBetween(User student, LocalDateTime startDate, LocalDateTime endDate);
    List<Assignment> findByGrade(String grade);
}
