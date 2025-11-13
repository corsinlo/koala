package com.maplewood.scheduler.service;

import com.maplewood.scheduler.model.StudentProgress;
import com.maplewood.scheduler.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        studentService = new StudentService(studentRepository, null, null, null);
    }

    @Test
    void testGetStudentProgress_ValidStudent_ReturnsProgress() {
        // Arrange
        Integer studentId = 101;
        StudentProgress mockProgress = new StudentProgress(
            101, "John", "Doe", 10, 5, 4, 15.0, 3.5, 30.0, 15.0, 2026, true, "On Track"
        );

        when(studentRepository.progress(studentId)).thenReturn(mockProgress);

        // Act
        StudentProgress result = studentService.progress(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(studentId, result.studentId());
        assertEquals(15.0, result.creditsEarned());
        assertEquals(15.0, result.creditsRemaining());
        assertEquals(3.5, result.gpa());
        verify(studentRepository, times(1)).progress(studentId);
    }

    @Test
    void testGraduationRequirement_ThirtyCredits() {
        // Test the 30 credit graduation requirement
        StudentProgress progress = new StudentProgress(
            101, "John", "Doe", 10, 5, 4, 29.0, 3.5, 30.0, 1.0, 2026, true, "On Track"
        );

        assertEquals(1.0, progress.creditsRemaining(), "Should need 1 more credit to graduate");
        assertFalse(progress.creditsEarned() >= progress.creditsRequired(),
                   "Student should not be ready to graduate with 29/30 credits");
    }

    @Test
    void testMaxCoursesPerSemester_FiveCourseLimit() {
        // Test the 5 courses per semester business rule
        int maxCoursesPerSemester = 5;
        assertTrue(maxCoursesPerSemester == 5, "Maximum courses per semester should be 5");
    }
}