package com.maplewood.scheduler.model;

public record Section(
    Integer id,
    Integer courseId,
    Integer teacherId,
    Integer roomId,
    Integer semesterId,
    Integer sectionNumber,
    Integer capacity,
    String courseName,
    String courseCode,
    String teacherName,
    String roomName,
    Integer enrolledStudents
) {}