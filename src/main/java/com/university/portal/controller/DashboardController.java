package com.university.portal.controller;

import com.university.portal.dto.ApiResponse;
import com.university.portal.dto.FacultyDashboardDTO;
import com.university.portal.dto.StudentDashboardDTO;
import com.university.portal.entity.User;
import com.university.portal.service.DashboardService;
import com.university.portal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping("/student/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardDTO>> studentDashboard(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        StudentDashboardDTO dashboard = dashboardService.getStudentDashboard(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Student dashboard loaded", dashboard));
    }

    @GetMapping("/faculty/dashboard")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FacultyDashboardDTO>> facultyDashboard() {
        FacultyDashboardDTO dashboard = dashboardService.getFacultyDashboard();
        return ResponseEntity.ok(ApiResponse.success("Faculty dashboard loaded", dashboard));
    }
}
