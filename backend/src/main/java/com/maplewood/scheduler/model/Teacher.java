package com.maplewood.scheduler.model;

public record Teacher(
        int id,
        String firstName,
        String lastName,
        int specializationId
) {
    public String fullName() { return firstName + " " + lastName; }
}
