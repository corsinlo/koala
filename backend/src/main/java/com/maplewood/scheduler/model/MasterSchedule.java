package com.maplewood.scheduler.model;

import java.util.List;

public record MasterSchedule(
    Integer semesterId,
    String semesterName,
    List<ScheduleEntry> entries,
    ScheduleStats stats
) {
    public record ScheduleEntry(
        Integer sectionId,
        String courseCode,
        String courseName,
        Integer sectionNumber,
        String teacherName,
        String roomName,
        List<SectionMeeting> meetings,
        Integer enrolledStudents,
        Integer capacity,
        Integer availableSpots
    ) {}

    public record ScheduleStats(
        Integer totalSections,
        Integer totalTeachers,
        Integer totalRooms,
        Double averageTeacherLoad,
        Double roomUtilization
    ) {}
}