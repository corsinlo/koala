package com.maplewood.scheduler.model;

public record SectionMeeting(
    Integer id,
    Integer sectionId,
    Integer dayOfWeek, // 1 = Monday .. 5 = Friday
    String startTime,  // 'HH:mm'
    Integer durationMinutes
) {}