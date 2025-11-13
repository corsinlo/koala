package com.maplewood.scheduler.model;

// Additional model classes for scheduling

public class SchedulingModels {

    public record CourseData(
        Integer id,
        String code,
        String name,
        Double credits,
        Integer hoursPerWeek,
        Integer specializationId,
        String specializationName
    ) {}

    public record TeacherData(
        Integer id,
        String firstName,
        String lastName,
        Integer specializationId,
        String specializationName,
        Integer maxDailyHours
    ) {
        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    public record ClassroomData(
        Integer id,
        String name,
        Integer roomTypeId,
        String roomTypeName,
        Integer capacity
    ) {}

    public record TimeSlot(
        Integer dayOfWeek, // 1=Monday, 5=Friday
        String startTime,  // "09:00"
        Integer durationMinutes
    ) {
        public String getEndTime() {
            String[] parts = startTime.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            int totalMinutes = hours * 60 + minutes + durationMinutes;
            int endHours = totalMinutes / 60;
            int endMins = totalMinutes % 60;

            return String.format("%02d:%02d", endHours, endMins);
        }

        public String getDayName() {
            return switch (dayOfWeek) {
                case 1 -> "Monday";
                case 2 -> "Tuesday";
                case 3 -> "Wednesday";
                case 4 -> "Thursday";
                case 5 -> "Friday";
                default -> "Unknown";
            };
        }
    }
}