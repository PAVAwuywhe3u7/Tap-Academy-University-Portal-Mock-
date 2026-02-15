package com.university.portal.dto;

import com.university.portal.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceBatchItemRequest {

    @NotNull(message = "Student id is required")
    private Long studentId;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;
}
