package com.university.portal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class AttendanceBatchRequest {

    @NotBlank(message = "Class name is required")
    private String className;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Valid
    @NotEmpty(message = "At least one attendance record is required")
    private List<AttendanceBatchItemRequest> records;
}
