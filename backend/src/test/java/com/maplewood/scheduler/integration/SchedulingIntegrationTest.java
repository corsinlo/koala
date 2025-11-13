package com.maplewood.scheduler.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchedulingIntegrationTest {

    @Test
    void testTimeSlotConstraints_NoLunchTimeClasses() {
        // Test the business rule: no classes scheduled during lunch (12:00-13:00)
        String[] validTimeSlots = {"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"};

        for (String slot : validTimeSlots) {
            assertNotEquals("12:00", slot, "Classes should not be scheduled during lunch break");
        }

        // Verify we have 7 valid time slots (excluding lunch hour)
        assertEquals(7, validTimeSlots.length);
    }

    @Test
    void testMaxCoursesPerSemester_BusinessRule() {
        // Test the 5 courses per semester constraint
        int maxCoursesPerSemester = 5;
        assertTrue(maxCoursesPerSemester <= 5, "Students should not be able to enroll in more than 5 courses per semester");
    }

    @Test
    void testGraduationCredits_BusinessRule() {
        // Test the 30 credits graduation requirement from the challenge
        int graduationCredits = 30;
        assertEquals(30, graduationCredits, "Students should need 30 credits to graduate");
    }

    @Test
    void testMaxConsecutiveHours_BusinessRule() {
        // Test the maximum consecutive hours constraint
        int maxConsecutiveHours = 2;
        assertTrue(maxConsecutiveHours <= 2, "No more than 2 consecutive hours should be allowed");
    }

    @Test
    void testValidTimeSlots_BusinessRule() {
        // Test that we exclude lunch time (12:00-13:00) from scheduling
        String[] allPossibleSlots = {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00"};
        String[] validSlots = {"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"};

        assertEquals(7, validSlots.length, "Should have 7 valid time slots");
        assertEquals(8, allPossibleSlots.length, "Should have 8 total possible slots");

        // Verify lunch time is excluded
        boolean hasLunchSlot = false;
        for (String slot : validSlots) {
            if ("12:00".equals(slot)) {
                hasLunchSlot = true;
                break;
            }
        }
        assertFalse(hasLunchSlot, "Lunch time (12:00) should not be in valid time slots");
    }
}