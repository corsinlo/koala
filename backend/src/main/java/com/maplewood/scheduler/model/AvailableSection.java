package com.maplewood.scheduler.model;

import java.util.List;

public record AvailableSection(
    Integer sectionId,
    String courseCode,
    String courseName,
    Integer sectionNumber,
    String teacherName,
    String roomName,
    Double credits,
    Integer hoursPerWeek,
    List<SectionMeeting> meetings,
    Integer capacity,
    Integer enrolledStudents,
    Integer availableSpots,

    // Enrollment eligibility info
    boolean canEnroll,
    String enrollmentStatus, // "available", "prerequisite_missing", "time_conflict", "semester_limit", "section_full"
    String statusMessage,

    // Course info
    String prerequisiteCourse,
    Integer gradeLevel,
    String specializationName,
    boolean isCore
) {}