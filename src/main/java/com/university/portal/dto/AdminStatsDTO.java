package com.university.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminStatsDTO {
    private long totalUsers;
    private long totalStudents;
    private long totalFaculty;
    private long totalAdmins;
    private long totalCourses;
    private long totalAssignments;
}
