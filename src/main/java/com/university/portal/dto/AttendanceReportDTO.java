package com.university.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttendanceReportDTO {
    private Long studentId;
    private String studentName;
    private String className;
    private long totalClasses;
    private long presentClasses;
    private double attendancePercentage;
}
