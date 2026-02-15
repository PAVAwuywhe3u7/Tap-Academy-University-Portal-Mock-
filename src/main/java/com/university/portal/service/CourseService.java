package com.university.portal.service;

import com.university.portal.dto.CourseDTO;
import com.university.portal.dto.CourseRequest;
import com.university.portal.entity.Course;
import com.university.portal.exception.BadRequestException;
import com.university.portal.exception.ResourceNotFoundException;
import com.university.portal.repository.CourseRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getActiveCourses() {
        return courseRepository.findByActiveTrueOrderByCodeAsc().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CourseDTO createCourse(CourseRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (courseRepository.existsByCode(code)) {
            throw new BadRequestException("Course code already exists: " + code);
        }
        Course course = new Course();
        course.setCode(code);
        course.setTitle(request.getTitle().trim());
        course.setDepartment(request.getDepartment().trim());
        course.setFacultyName(request.getFacultyName().trim());
        course.setActive(request.isActive());
        return toDTO(courseRepository.save(course));
    }

    public CourseDTO updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        String incomingCode = request.getCode().trim().toUpperCase();
        courseRepository.findByCode(incomingCode).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Course code already exists: " + incomingCode);
            }
        });

        course.setCode(incomingCode);
        course.setTitle(request.getTitle().trim());
        course.setDepartment(request.getDepartment().trim());
        course.setFacultyName(request.getFacultyName().trim());
        course.setActive(request.isActive());
        return toDTO(courseRepository.save(course));
    }

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countCourses() {
        return courseRepository.count();
    }

    private CourseDTO toDTO(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getDepartment(),
                course.getFacultyName(),
                course.isActive()
        );
    }
}
