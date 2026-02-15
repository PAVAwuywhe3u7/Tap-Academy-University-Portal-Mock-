package com.university.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FacultyDashboardDTO {
    private long totalCourses;
    private long totalStudents;
    private long pendingEvaluations;
}
