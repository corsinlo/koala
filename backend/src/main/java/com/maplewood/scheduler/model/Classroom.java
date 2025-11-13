package com.maplewood.scheduler.model;

public record Classroom(
        int id,
        String name,
        int roomTypeId,
        int capacity
) {}
