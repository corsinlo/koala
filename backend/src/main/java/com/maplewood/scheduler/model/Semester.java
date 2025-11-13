package com.maplewood.scheduler.model;

public record Semester(
        int id,
        String name,
        int year,
        int orderInYear,
        boolean isActive
) {}
