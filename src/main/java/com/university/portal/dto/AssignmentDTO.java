package com.university.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String course;
    private String assignmentTitle;
    private String originalFileName;
    private String filePath;
    private String feedback;
    private String grade;
    private LocalDateTime submissionDate;
    private Integer contentScore;
    private Integer grammarScore;
    private Integer structureScore;
    private Integer originalityScore;
    private Integer totalScore;
}
