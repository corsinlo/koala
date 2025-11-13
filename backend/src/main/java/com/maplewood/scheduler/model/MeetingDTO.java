package com.maplewood.scheduler.model;

public record MeetingDTO(
        int dayOfWeek,
        String startTime,
        int durationMinutes
) {}
