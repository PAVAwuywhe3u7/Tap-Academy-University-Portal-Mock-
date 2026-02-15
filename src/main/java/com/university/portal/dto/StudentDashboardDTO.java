package com.university.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentDashboardDTO {
    private double attendancePercentage;
    private long submittedAssignments;
    private String averageGrade;
}
