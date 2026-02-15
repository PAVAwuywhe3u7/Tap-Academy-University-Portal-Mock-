package com.university.portal.controller;

import com.university.portal.dto.ApiResponse;
import com.university.portal.dto.AssignmentGradeUpdateRequest;
import com.university.portal.dto.AssignmentDTO;
import com.university.portal.entity.User;
import com.university.portal.entity.UserRole;
import com.university.portal.exception.UnauthorizedException;
import com.university.portal.service.AssignmentService;
import com.university.portal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final UserService userService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AssignmentDTO>> submitAssignment(
            @RequestParam Long studentId,
            @RequestParam String course,
            @RequestParam(required = false) String assignmentTitle,
            @RequestParam("file") MultipartFile file) {
        try {
            AssignmentDTO assignment = assignmentService.submitAssignment(studentId, course, assignmentTitle, file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Assignment submitted successfully", assignment));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AssignmentDTO>>> getStudentAssignments(@PathVariable Long studentId, Authentication authentication) {
        enforceStudentOwnership(studentId, authentication);
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success("Assignments fetched", assignments));
    }

    @GetMapping("/course/{course}")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AssignmentDTO>>> getAssignmentsByCourse(@PathVariable String course) {
        List<AssignmentDTO> assignments = assignmentService.getAssignmentsByCourse(course);
        return ResponseEntity.ok(ApiResponse.success("Course assignments fetched", assignments));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentDTO>> getAssignmentById(@PathVariable Long id) {
        AssignmentDTO assignment = assignmentService.getAssignmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Assignment fetched", assignment));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AssignmentDTO>>> getAllAssignments() {
        List<AssignmentDTO> assignments = assignmentService.getAllAssignments();
        return ResponseEntity.ok(ApiResponse.success("All assignments fetched", assignments));
    }

    @PutMapping("/{id}/grade")
    @PreAuthorize("hasRole('FACULTY') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AssignmentDTO>> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentGradeUpdateRequest request) {
        AssignmentDTO assignment = assignmentService.updateGrade(id, request);
        return ResponseEntity.ok(ApiResponse.success("Grade updated successfully", assignment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Assignment deleted successfully"));
    }

    private void enforceStudentOwnership(Long studentId, Authentication authentication) {
        User currentUser = userService.getUserByEmail(authentication.getName());
        if (currentUser.getRole() == UserRole.STUDENT && !currentUser.getId().equals(studentId)) {
            throw new UnauthorizedException("Students can only access their own assignments");
        }
    }
}
