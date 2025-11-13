package com.maplewood.scheduler.model;

import java.util.List;

public record StudentSchedule(
    Integer studentId,
    String studentName,
    Integer semesterId,
    String semesterName,
    List<EnrolledSection> enrolledSections,
    StudentProgress progress
) {
    public record EnrolledSection(
        Integer sectionId,
        String courseCode,
        String courseName,
        Integer sectionNumber,
        String teacherName,
        String roomName,
        Double credits,
        List<SectionMeeting> meetings,
        Integer capacity,
        Integer enrolledStudents,
        Integer availableSpots
    ) {}
}