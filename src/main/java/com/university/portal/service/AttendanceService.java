package com.university.portal.service;

import com.university.portal.dto.AttendanceBatchItemRequest;
import com.university.portal.dto.AttendanceBatchRequest;
import com.university.portal.dto.AttendanceDTO;
import com.university.portal.dto.AttendanceMarkRequest;
import com.university.portal.dto.AttendanceReportDTO;
import com.university.portal.entity.Attendance;
import com.university.portal.entity.AttendanceStatus;
import com.university.portal.entity.User;
import com.university.portal.entity.UserRole;
import com.university.portal.exception.BadRequestException;
import com.university.portal.exception.ResourceNotFoundException;
import com.university.portal.repository.AttendanceRepository;
import com.university.portal.repository.UserRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public AttendanceDTO markAttendance(AttendanceMarkRequest request) {
        User student = getStudentById(request.getStudentId());
        Attendance attendance = upsertAttendance(student, request.getClassName(), request.getDate(), request.getStatus());
        return toDTO(attendance);
    }

    public List<AttendanceDTO> markAttendanceBatch(AttendanceBatchRequest request) {
        return request.getRecords().stream()
                .map(record -> saveBatchItem(request.getClassName(), request.getDate(), record))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendanceByStudent(Long studentId) {
        User student = getStudentById(studentId);
        return attendanceRepository.findByStudent(student).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendanceByClassAndDate(String className, LocalDate date) {
        return attendanceRepository.findByClassNameAndDate(className, date).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendanceByClass(String className) {
        return attendanceRepository.findByClassName(className).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public double getAttendancePercentage(Long studentId, LocalDate startDate, LocalDate endDate) {
        User student = getStudentById(studentId);
        List<Attendance> attendances = attendanceRepository.findByStudentAndDateBetween(student, startDate, endDate);
        if (attendances.isEmpty()) {
            return 0.0;
        }

        long presentCount = attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
        return Math.round(((presentCount * 100.0) / attendances.size()) * 100.0) / 100.0;
    }

    @Transactional(readOnly = true)
    public List<AttendanceReportDTO> getAttendanceReport(String className, LocalDate startDate, LocalDate endDate) {
        LocalDate fromDate = startDate == null ? LocalDate.now().minusMonths(1) : startDate;
        LocalDate toDate = endDate == null ? LocalDate.now() : endDate;

        List<Attendance> records = attendanceRepository.getReportData(
                (className == null || className.isBlank()) ? null : className,
                fromDate,
                toDate
        );

        Map<String, AttendanceAccumulator> reportMap = new LinkedHashMap<>();
        for (Attendance record : records) {
            String key = record.getStudent().getId() + "|" + record.getClassName();
            AttendanceAccumulator acc = reportMap.computeIfAbsent(
                    key,
                    ignored -> new AttendanceAccumulator(
                            record.getStudent().getId(),
                            record.getStudent().getName(),
                            record.getClassName()
                    )
            );
            acc.totalClasses++;
            if (record.getStatus() == AttendanceStatus.PRESENT) {
                acc.presentClasses++;
            }
        }

        return reportMap.values().stream()
                .map(acc -> new AttendanceReportDTO(
                        acc.studentId,
                        acc.studentName,
                        acc.className,
                        acc.totalClasses,
                        acc.presentClasses,
                        acc.totalClasses == 0 ? 0.0 : Math.round((acc.presentClasses * 10000.0 / acc.totalClasses)) / 100.0
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> getStudentsForClass(String className) {
        if (className == null || className.isBlank()) {
            throw new BadRequestException("Class name is required");
        }
        return userRepository.findByRole(UserRole.STUDENT);
    }

    private AttendanceDTO toDTO(Attendance attendance) {
        return new AttendanceDTO(
                attendance.getId(),
                attendance.getStudent().getId(),
                attendance.getStudent().getName(),
                attendance.getClassName(),
                attendance.getDate(),
                attendance.getStatus().name()
        );
    }

    private User getStudentById(Long studentId) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        if (user.getRole() != UserRole.STUDENT) {
            throw new BadRequestException("User is not a student");
        }
        return user;
    }

    private AttendanceDTO saveBatchItem(String className, LocalDate date, AttendanceBatchItemRequest item) {
        User student = getStudentById(item.getStudentId());
        Attendance attendance = upsertAttendance(student, className, date, item.getStatus());
        return toDTO(attendance);
    }

    private Attendance upsertAttendance(User student, String className, LocalDate date, AttendanceStatus status) {
        if (className == null || className.isBlank()) {
            throw new BadRequestException("Class name cannot be empty");
        }
        if (date == null) {
            throw new BadRequestException("Date is required");
        }

        Attendance attendance = attendanceRepository.findByStudentAndClassNameAndDate(student, className.trim(), date)
                .orElseGet(Attendance::new);
        attendance.setStudent(student);
        attendance.setClassName(className.trim());
        attendance.setDate(date);
        attendance.setStatus(status);
        return attendanceRepository.save(attendance);
    }

    private static class AttendanceAccumulator {
        private final Long studentId;
        private final String studentName;
        private final String className;
        private long totalClasses;
        private long presentClasses;

        private AttendanceAccumulator(Long studentId, String studentName, String className) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.className = className;
        }
    }
}
