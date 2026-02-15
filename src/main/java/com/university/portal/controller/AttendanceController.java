package com.university.portal.controller;

import com.university.portal.dto.ApiResponse;
import com.university.portal.dto.AttendanceBatchRequest;
import com.university.portal.dto.AttendanceDTO;
import com.university.portal.dto.AttendanceMarkRequest;
import com.university.portal.dto.AttendanceReportDTO;
import com.university.portal.dto.UserDTO;
import com.university.portal.entity.User;
import com.university.portal.entity.UserRole;
import com.university.portal.exception.UnauthorizedException;
import com.university.portal.service.AttendanceService;
import com.university.portal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserService userService;

    @PostMapping("/mark")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AttendanceDTO>> markAttendance(@Valid @RequestBody AttendanceMarkRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Attendance saved", attendanceService.markAttendance(request)));
    }

    @PostMapping("/mark-batch")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> markBatch(@Valid @RequestBody AttendanceBatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Batch attendance saved", attendanceService.markAttendanceBatch(request)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getStudentAttendance(@PathVariable Long studentId, Authentication authentication) {
        enforceStudentOwnership(studentId, authentication);
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success("Student attendance fetched", attendance));
    }

    @GetMapping("/class/{className}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getClassAttendance(@PathVariable String className) {
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByClass(className);
        return ResponseEntity.ok(ApiResponse.success("Class attendance fetched", attendance));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceByClassAndDate(
            @RequestParam String className,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByClassAndDate(className, date);
        return ResponseEntity.ok(ApiResponse.success("Attendance filtered", attendance));
    }

    @GetMapping("/percentage/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Double>> getAttendancePercentage(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        enforceStudentOwnership(studentId, authentication);
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        double percentage = attendanceService.getAttendancePercentage(studentId, start, end);
        return ResponseEntity.ok(ApiResponse.success("Attendance percentage calculated", percentage));
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AttendanceReportDTO>>> report(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttendanceReportDTO> reportData = attendanceService.getAttendanceReport(className, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Attendance report generated", reportData));
    }

    @GetMapping("/faculty/students")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> studentsForClass(@RequestParam String className) {
        List<UserDTO> students = attendanceService.getStudentsForClass(className).stream()
                .map(userService::toDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Students loaded", students));
    }

    private void enforceStudentOwnership(Long studentId, Authentication authentication) {
        User currentUser = userService.getUserByEmail(authentication.getName());
        if (currentUser.getRole() == UserRole.STUDENT && !currentUser.getId().equals(studentId)) {
            throw new UnauthorizedException("Students can only access their own attendance records");
        }
    }
}
