package com.university.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "Student is required")
    private User student;

    @NotBlank(message = "Course is required")
    @Column(nullable = false, length = 120)
    private String course;

    @Column(length = 180)
    private String assignmentTitle;

    @NotBlank(message = "Original file name is required")
    @Column(length = 255)
    private String originalFileName;

    @NotBlank(message = "File path is required")
    @Column(name = "file_path", nullable = false, length = 300)
    private String filePath;

    @Column(length = 2500)
    private String feedback;

    @Column(length = 5)
    private String grade;

    @Column(nullable = false)
    private LocalDateTime submissionDate;

    @Column
    private Integer contentScore;

    @Column
    private Integer grammarScore;

    @Column
    private Integer structureScore;

    @Column
    private Integer originalityScore;

    @Column
    private Integer totalScore;

    @PrePersist
    void onCreate() {
        if (this.submissionDate == null) {
            this.submissionDate = LocalDateTime.now();
        }
    }
}
