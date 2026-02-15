package com.university.portal.repository;

import com.university.portal.entity.Attendance;
import com.university.portal.entity.AttendanceStatus;
import com.university.portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByStudent(User student);
    List<Attendance> findByClassNameAndDate(String className, LocalDate date);
    List<Attendance> findByClassName(String className);
    List<Attendance> findByDate(LocalDate date);
    Optional<Attendance> findByStudentAndClassNameAndDate(User student, String className, LocalDate date);
    List<Attendance> findByStudentAndDateBetween(User student, LocalDate startDate, LocalDate endDate);
    List<Attendance> findByClassNameAndDateBetween(String className, LocalDate startDate, LocalDate endDate);
    List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);
    Long countByStudentAndStatus(User student, AttendanceStatus status);

    @Query("""
            SELECT a FROM Attendance a
            WHERE (:className IS NULL OR a.className = :className)
              AND a.date BETWEEN :startDate AND :endDate
            ORDER BY a.date DESC
            """)
    List<Attendance> getReportData(
            @Param("className") String className,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
