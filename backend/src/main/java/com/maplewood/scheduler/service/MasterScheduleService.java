package com.maplewood.scheduler.service;

import com.maplewood.scheduler.model.Section;
import com.maplewood.scheduler.model.SectionMeeting;
import com.maplewood.scheduler.model.MasterSchedule;
import static com.maplewood.scheduler.model.SchedulingModels.*;
import com.maplewood.scheduler.repository.SchedulingRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MasterScheduleService {

    private final SchedulingRepository repository;

    // Time constraints from the challenge
    private static final String[] TIME_SLOTS = {
        "09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"
    }; // 12:00-13:00 is lunch break
    private static final int MAX_CONSECUTIVE_HOURS = 2;
    private static final int SESSION_DURATION_MINUTES = 60;

    public MasterScheduleService(SchedulingRepository repository) {
        this.repository = repository;
    }

    public MasterSchedule generateScheduleForSemester(Integer semesterId) {
        // Clear any existing schedule for this semester
        repository.clearScheduleForSemester(semesterId);

        // Get all resources
        List<CourseData> courses = repository.getCoursesForSemester(semesterId);
        List<TeacherData> teachers = repository.getAvailableTeachers();
        List<ClassroomData> classrooms = repository.getAvailableClassrooms();

        // Initialize tracking structures
        Map<Integer, Set<TimeSlot>> teacherSchedules = new HashMap<>();
        Map<Integer, Set<TimeSlot>> roomSchedules = new HashMap<>();
        List<String> schedulingErrors = new ArrayList<>();

        // Generate schedule
        for (CourseData course : courses) {
            try {
                generateSectionsForCourse(course, teachers, classrooms,
                    teacherSchedules, roomSchedules, semesterId);
            } catch (Exception e) {
                schedulingErrors.add("Failed to schedule course " + course.code() + ": " + e.getMessage());
            }
        }

        if (!schedulingErrors.isEmpty()) {
            throw new RuntimeException("Scheduling errors: " + String.join(", ", schedulingErrors));
        }

        // Build response
        return buildMasterSchedule(semesterId);
    }

    private void generateSectionsForCourse(CourseData course, List<TeacherData> allTeachers,
                                         List<ClassroomData> allClassrooms,
                                         Map<Integer, Set<TimeSlot>> teacherSchedules,
                                         Map<Integer, Set<TimeSlot>> roomSchedules,
                                         Integer semesterId) {

        // Find compatible teachers (same specialization)
        List<TeacherData> compatibleTeachers = allTeachers.stream()
            .filter(t -> t.specializationId().equals(course.specializationId()))
            .collect(Collectors.toList());

        if (compatibleTeachers.isEmpty()) {
            throw new RuntimeException("No teachers available for specialization " + course.specializationName());
        }

        // Estimate number of sections needed (assuming ~15 students per course, capacity 10 per section)
        int estimatedSections = Math.max(1, 2); // Create at least 2 sections per course

        int sectionsCreated = 0;
        for (int sectionNum = 1; sectionNum <= estimatedSections; sectionNum++) {
            boolean sectionScheduled = false;

            // Try to assign teacher and room
            for (TeacherData teacher : compatibleTeachers) {
                if (sectionScheduled) break;

                // Find compatible classrooms
                List<ClassroomData> compatibleRooms = findCompatibleClassrooms(course, allClassrooms);

                for (ClassroomData room : compatibleRooms) {
                    if (sectionScheduled) break;

                    // Try to schedule the required hours across the week
                    List<TimeSlot> proposedMeetings = scheduleClassMeetings(
                        course, teacherSchedules.getOrDefault(teacher.id(), new HashSet<>()),
                        roomSchedules.getOrDefault(room.id(), new HashSet<>())
                    );

                    if (!proposedMeetings.isEmpty()) {
                        // Create the section
                        repository.createSection(course.id(), teacher.id(), room.id(), semesterId, sectionNum);
                        Integer sectionId = repository.getLastInsertedSectionId();

                        // Create the meetings
                        for (TimeSlot meeting : proposedMeetings) {
                            repository.createSectionMeeting(sectionId, meeting.dayOfWeek(),
                                meeting.startTime(), meeting.durationMinutes());

                            // Update tracking
                            teacherSchedules.computeIfAbsent(teacher.id(), k -> new HashSet<>()).add(meeting);
                            roomSchedules.computeIfAbsent(room.id(), k -> new HashSet<>()).add(meeting);
                        }

                        sectionsCreated++;
                        sectionScheduled = true;
                    }
                }
            }

            if (!sectionScheduled) {
                System.out.println("Warning: Could not schedule section " + sectionNum + " for course " + course.code());
            }
        }

        if (sectionsCreated == 0) {
            throw new RuntimeException("Could not schedule any sections for course " + course.code());
        }
    }

    private List<ClassroomData> findCompatibleClassrooms(CourseData course, List<ClassroomData> allClassrooms) {
        // For now, simple logic - could be enhanced to match room types to course specializations
        return allClassrooms.stream()
            .sorted((r1, r2) -> r1.name().compareTo(r2.name()))
            .collect(Collectors.toList());
    }

    private List<TimeSlot> scheduleClassMeetings(CourseData course, Set<TimeSlot> teacherBusyTimes,
                                               Set<TimeSlot> roomBusyTimes) {
        int requiredHours = course.hoursPerWeek();
        List<TimeSlot> meetings = new ArrayList<>();

        // Try to distribute hours across different days
        // Strategy: prefer MWF or TTh patterns
        List<List<Integer>> dayPatterns = List.of(
            Arrays.asList(1, 3, 5), // Monday, Wednesday, Friday
            Arrays.asList(2, 4),    // Tuesday, Thursday
            Arrays.asList(1, 2, 3, 4, 5) // All days if needed
        );

        for (List<Integer> pattern : dayPatterns) {
            meetings.clear();
            if (tryScheduleOnDays(pattern, requiredHours, teacherBusyTimes, roomBusyTimes, meetings)) {
                break;
            }
        }

        return meetings;
    }

    private boolean tryScheduleOnDays(List<Integer> days, int requiredHours,
                                    Set<TimeSlot> teacherBusyTimes, Set<TimeSlot> roomBusyTimes,
                                    List<TimeSlot> meetings) {
        int hoursScheduled = 0;

        for (int day : days) {
            if (hoursScheduled >= requiredHours) break;

            for (String startTime : TIME_SLOTS) {
                if (hoursScheduled >= requiredHours) break;

                // Try 1-hour session
                TimeSlot slot = new TimeSlot(day, startTime, SESSION_DURATION_MINUTES);

                if (!hasConflict(slot, teacherBusyTimes) && !hasConflict(slot, roomBusyTimes)) {
                    meetings.add(slot);
                    hoursScheduled += 1;

                    // Try to add consecutive hour if course needs it and we have capacity
                    if (hoursScheduled < requiredHours && requiredHours - hoursScheduled >= 1) {
                        String nextStartTime = getNextTimeSlot(startTime);
                        if (nextStartTime != null && !nextStartTime.equals("12:00")) { // Avoid lunch
                            TimeSlot nextSlot = new TimeSlot(day, nextStartTime, SESSION_DURATION_MINUTES);
                            if (!hasConflict(nextSlot, teacherBusyTimes) && !hasConflict(nextSlot, roomBusyTimes)) {
                                meetings.add(nextSlot);
                                hoursScheduled += 1;
                            }
                        }
                    }
                }
            }
        }

        return hoursScheduled >= requiredHours;
    }

    private boolean hasConflict(TimeSlot slot, Set<TimeSlot> existingSlots) {
        return existingSlots.stream().anyMatch(existing ->
            existing.dayOfWeek().equals(slot.dayOfWeek()) &&
            timeSlotsOverlap(existing, slot)
        );
    }

    private boolean timeSlotsOverlap(TimeSlot slot1, TimeSlot slot2) {
        int start1 = timeToMinutes(slot1.startTime());
        int end1 = start1 + slot1.durationMinutes();
        int start2 = timeToMinutes(slot2.startTime());
        int end2 = start2 + slot2.durationMinutes();

        return start1 < end2 && start2 < end1;
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String getNextTimeSlot(String currentTime) {
        int currentIndex = Arrays.asList(TIME_SLOTS).indexOf(currentTime);
        return currentIndex >= 0 && currentIndex < TIME_SLOTS.length - 1 ?
               TIME_SLOTS[currentIndex + 1] : null;
    }

    private MasterSchedule buildMasterSchedule(Integer semesterId) {
        List<Section> sections = repository.getSectionsForSemester(semesterId);
        List<MasterSchedule.ScheduleEntry> entries = new ArrayList<>();

        for (Section section : sections) {
            List<SectionMeeting> meetings = repository.getMeetingsForSection(section.id());

            entries.add(new MasterSchedule.ScheduleEntry(
                section.id(),
                section.courseCode(),
                section.courseName(),
                section.sectionNumber(),
                section.teacherName(),
                section.roomName(),
                meetings,
                section.enrolledStudents(),
                section.capacity(),
                section.capacity() - section.enrolledStudents()
            ));
        }

        // Calculate stats
        long uniqueTeachers = sections.stream().mapToInt(Section::teacherId).distinct().count();
        long uniqueRooms = sections.stream().mapToInt(Section::roomId).distinct().count();

        MasterSchedule.ScheduleStats stats = new MasterSchedule.ScheduleStats(
            sections.size(),
            (int) uniqueTeachers,
            (int) uniqueRooms,
            uniqueTeachers > 0 ? (double) sections.size() / uniqueTeachers : 0.0,
            uniqueRooms > 0 ? (double) sections.size() / uniqueRooms : 0.0
        );

        // Get semester name
        String semesterName = "Semester " + semesterId; // Could be improved with actual semester lookup

        return new MasterSchedule(semesterId, semesterName, entries, stats);
    }

    public MasterSchedule getExistingSchedule(Integer semesterId) {
        return buildMasterSchedule(semesterId);
    }
}