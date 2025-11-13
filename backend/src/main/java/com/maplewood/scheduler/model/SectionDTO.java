package com.maplewood.scheduler.model;

import java.util.List;

public record SectionDTO(
        int id,
        String courseCode,
        String courseName,
        int sectionNumber,
        String teacherName,
        String roomName,
        int capacity,
        int enrolled,
        List<MeetingDTO> meetings
) {}
