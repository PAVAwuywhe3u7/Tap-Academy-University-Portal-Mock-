package com.university.portal.service;

import com.university.portal.dto.AssignmentDTO;
import com.university.portal.dto.AssignmentEvaluationResult;
import com.university.portal.dto.AssignmentGradeUpdateRequest;
import com.university.portal.entity.Assignment;
import com.university.portal.entity.User;
import com.university.portal.entity.UserRole;
import com.university.portal.exception.BadRequestException;
import com.university.portal.exception.ResourceNotFoundException;
import com.university.portal.repository.AssignmentRepository;
import com.university.portal.repository.UserRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final AiEvaluationService aiEvaluationService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".doc", ".docx", ".png", ".jpg", ".jpeg");

    @Value("${app.upload.dir:uploads/assignments}")
    private String uploadDir;

    public AssignmentDTO submitAssignment(Long studentId, String course, String assignmentTitle, MultipartFile file) throws IOException {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (student.getRole() != UserRole.STUDENT) {
            throw new BadRequestException("Only students can submit assignments");
        }

        if (course == null || course.trim().isBlank()) {
            throw new BadRequestException("Course name cannot be empty");
        }

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 10MB");
        }

        String originalFileName = file.getOriginalFilename() == null ? "assignment" : file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Only PDF, DOC, DOCX, PNG, JPG, or JPEG files are allowed");
        }

        Files.createDirectories(Paths.get(uploadDir));
        String storedFileName = UUID.randomUUID() + "_" + sanitizeFileName(originalFileName);
        Path path = Paths.get(uploadDir, storedFileName);
        Files.write(path, file.getBytes());

        String normalizedTitle = (assignmentTitle == null || assignmentTitle.trim().isBlank()) ? null : assignmentTitle.trim();
        String content = extractTextForEvaluation(file.getBytes());
        if (normalizedTitle != null) {
            content = "Assignment: " + normalizedTitle + "\n" + content;
        }
        AssignmentEvaluationResult evaluation = aiEvaluationService.evaluateAssignment(content, course);

        Assignment assignment = new Assignment();
        assignment.setStudent(student);
        assignment.setCourse(course.trim());
        assignment.setAssignmentTitle(normalizedTitle);
        assignment.setOriginalFileName(originalFileName);
        assignment.setFilePath(path.toString());
        assignment.setSubmissionDate(LocalDateTime.now());
        assignment.setGrade(evaluation.getGrade());
        assignment.setFeedback(evaluation.getFeedback());
        assignment.setContentScore(evaluation.getContentScore());
        assignment.setGrammarScore(evaluation.getGrammarScore());
        assignment.setStructureScore(evaluation.getStructureScore());
        assignment.setOriginalityScore(evaluation.getOriginalityScore());
        assignment.setTotalScore(evaluation.getTotalScore());

        Assignment saved = assignmentRepository.save(assignment);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAssignmentsByStudent(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return assignmentRepository.findByStudent(student).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAssignmentsByCourse(String course) {
        return assignmentRepository.findByCourse(course).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssignmentDTO getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        return toDTO(assignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AssignmentDTO updateGrade(Long id, AssignmentGradeUpdateRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (request.getGrade() != null && !request.getGrade().isBlank()) {
            assignment.setGrade(request.getGrade().trim().toUpperCase(Locale.ROOT));
        }

        if (request.getFeedback() != null && !request.getFeedback().isBlank()) {
            assignment.setFeedback(request.getFeedback().trim());
        }

        return toDTO(assignmentRepository.save(assignment));
    }

    public void deleteAssignment(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        try {
            Files.deleteIfExists(Paths.get(assignment.getFilePath()));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", assignment.getFilePath());
        }

        assignmentRepository.delete(assignment);
    }

    @Transactional(readOnly = true)
    public long getPendingEvaluationsCount() {
        return assignmentRepository.findAll().stream().filter(a -> a.getGrade() == null || a.getGrade().isBlank()).count();
    }

    @Transactional(readOnly = true)
    public String getAverageGradeForStudent(Long studentId) {
        List<Assignment> assignments = assignmentRepository.findByStudentId(studentId).stream()
                .filter(a -> a.getGrade() != null && !a.getGrade().isBlank())
                .toList();
        if (assignments.isEmpty()) {
            return "N/A";
        }

        double score = assignments.stream().mapToInt(this::gradeToValue).average().orElse(0);
        if (score >= 2.6) {
            return "A";
        }
        if (score >= 1.8) {
            return "B";
        }
        return "C";
    }

    private AssignmentDTO toDTO(Assignment assignment) {
        return new AssignmentDTO(
                assignment.getId(),
                assignment.getStudent().getId(),
                assignment.getStudent().getName(),
                assignment.getCourse(),
                assignment.getAssignmentTitle(),
                assignment.getOriginalFileName(),
                assignment.getFilePath(),
                assignment.getFeedback(),
                assignment.getGrade(),
                assignment.getSubmissionDate(),
                assignment.getContentScore(),
                assignment.getGrammarScore(),
                assignment.getStructureScore(),
                assignment.getOriginalityScore(),
                assignment.getTotalScore()
        );
    }

    private int gradeToValue(Assignment assignment) {
        return switch (assignment.getGrade().toUpperCase(Locale.ROOT)) {
            case "A" -> 3;
            case "B" -> 2;
            default -> 1;
        };
    }

    private String extractTextForEvaluation(byte[] bytes) {
        String preview = new String(bytes, StandardCharsets.UTF_8);
        if (preview.replaceAll("\\s+", "").isBlank()) {
            return "Binary or image assignment submission with size " + bytes.length + " bytes.";
        }
        if (preview.length() > 12000) {
            return preview.substring(0, 12000);
        }
        return preview;
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return fileName.substring(lastDot).toLowerCase(Locale.ROOT);
    }
}
