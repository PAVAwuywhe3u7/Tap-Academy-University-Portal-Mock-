package com.university.portal.dto;

import com.university.portal.entity.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AttendanceMarkRequest {

    @NotNull(message = "Student id is required")
    private Long studentId;

    @NotBlank(message = "Class name is required")
    private String className;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;
}
