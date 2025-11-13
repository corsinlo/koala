package com.maplewood.scheduler.model;

public record StudentProgress(
        int studentId,
        String firstName,
        String lastName,
        int gradeLevel,
        int coursesTaken,
        int coursesPassed,
        double creditsEarned,
        double gpa,

        // Graduation tracking
        double creditsRequired,
        double creditsRemaining,
        int expectedGraduationYear,
        boolean onTrackToGraduate,
        String graduationStatus
) {}
