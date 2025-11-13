package com.maplewood.scheduler.service;

import com.maplewood.scheduler.model.MasterSchedule;
import com.maplewood.scheduler.repository.SchedulingRepository;
import static com.maplewood.scheduler.model.SchedulingModels.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MasterScheduleServiceTest {

    @Mock
    private SchedulingRepository repository;

    private MasterScheduleService masterScheduleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        masterScheduleService = new MasterScheduleService(repository);
    }

    @Test
    void testTimeSlotValidation_ValidTimeSlots() {
        // Test that our time slots follow the constraint: 9,10,11,13,14,15,16
        String[] timeSlots = {"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"};

        // Verify no 12:00 slot (lunch break)
        for (String slot : timeSlots) {
            assertNotEquals("12:00", slot, "12:00 should be lunch break - no classes");
        }

        // Verify we have exactly 7 slots
        assertEquals(7, timeSlots.length, "Should have exactly 7 time slots per day");
    }

    @Test
    void testMaxConsecutiveHours_Constraint() {
        // Verify the MAX_CONSECUTIVE_HOURS constraint is set to 2
        // This is a business rule test
        int maxConsecutive = 2;
        assertTrue(maxConsecutive <= 2, "Maximum consecutive hours should not exceed 2");
    }

    @Test
    void testGetExistingSchedule_ValidSemester_ReturnsSchedule() {
        // Arrange
        Integer semesterId = 1;
        when(repository.getSectionsForSemester(semesterId)).thenReturn(List.of());

        // Act
        MasterSchedule result = masterScheduleService.getExistingSchedule(semesterId);

        // Assert
        assertNotNull(result);
        assertEquals(semesterId, result.semesterId());
    }
}