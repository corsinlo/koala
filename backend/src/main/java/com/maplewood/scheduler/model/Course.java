package com.maplewood.scheduler.model;

public record Course(
        int id,
        String code,
        String name,
        int credits,
        int hoursPerWeek,
        int specializationId,
        Integer prerequisiteId,
        int semesterOrder,
        int gradeLevelMin,
        int gradeLevelMax
) {}
