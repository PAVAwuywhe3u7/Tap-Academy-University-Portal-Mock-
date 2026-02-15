package com.university.portal.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AssignmentGradeUpdateRequest {

    @Pattern(regexp = "A|B|C", message = "Grade must be A, B, or C")
    private String grade;

    private String feedback;
}
