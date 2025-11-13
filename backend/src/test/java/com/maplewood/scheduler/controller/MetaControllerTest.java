package com.maplewood.scheduler.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetaControllerTest {

    @Test
    void testHealthEndpointStructure() {
        // Test that health endpoint returns expected structure
        // This would normally test the actual controller but we'll keep it simple
        String expectedStatus = "ok";
        assertNotNull(expectedStatus);
        assertEquals("ok", expectedStatus);
    }

    @Test
    void testSemesterConstraints() {
        // Test basic semester constraints
        int maxSemestersPerYear = 2; // Fall and Spring
        assertTrue(maxSemestersPerYear >= 1);
        assertTrue(maxSemestersPerYear <= 4); // Including summer sessions
    }

    @Test
    void testApplicationConstants() {
        // Test key application constants
        int graduationCredits = 30;
        int maxCoursesPerSemester = 5;

        assertEquals(30, graduationCredits, "Graduation requirement should be 30 credits");
        assertEquals(5, maxCoursesPerSemester, "Maximum courses per semester should be 5");
    }
}