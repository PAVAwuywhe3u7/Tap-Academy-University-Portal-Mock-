package com.university.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssignmentEvaluationResult {
    private int contentScore;
    private int grammarScore;
    private int structureScore;
    private int originalityScore;
    private int totalScore;
    private String grade;
    private String feedback;
}
