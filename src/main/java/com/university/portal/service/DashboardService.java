package com.university.portal.service;

import com.university.portal.dto.AdminStatsDTO;
import com.university.portal.dto.FacultyDashboardDTO;
import com.university.portal.dto.StudentDashboardDTO;
import com.university.portal.entity.UserRole;
import com.university.portal.repository.AssignmentRepository;
import com.university.portal.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final AttendanceService attendanceService;
    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;

    public StudentDashboardDTO getStudentDashboard(Long studentId) {
        double attendancePercentage = attendanceService.getAttendancePercentage(
                studentId,
                LocalDate.now().minusMonths(6),
                LocalDate.now()
        );
        long submittedAssignments = assignmentRepository.findByStudentId(studentId).size();
        String averageGrade = assignmentService.getAverageGradeForStudent(studentId);
        return new StudentDashboardDTO(attendancePercentage, submittedAssignments, averageGrade);
    }

    public FacultyDashboardDTO getFacultyDashboard() {
        long totalCourses = courseService.countCourses();
        long totalStudents = userRepository.findByRole(UserRole.STUDENT).size();
        long pendingEvaluations = assignmentService.getPendingEvaluationsCount();
        return new FacultyDashboardDTO(totalCourses, totalStudents, pendingEvaluations);
    }

    public AdminStatsDTO getAdminStats() {
        long totalUsers = userRepository.count();
        long totalStudents = userRepository.findByRole(UserRole.STUDENT).size();
        long totalFaculty = userRepository.findByRole(UserRole.FACULTY).size();
        long totalAdmins = userRepository.findByRole(UserRole.ADMIN).size();
        long totalCourses = courseService.countCourses();
        long totalAssignments = assignmentRepository.count();
        return new AdminStatsDTO(totalUsers, totalStudents, totalFaculty, totalAdmins, totalCourses, totalAssignments);
    }
}
