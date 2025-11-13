package com.maplewood.scheduler.service;

import com.maplewood.scheduler.model.*;
import com.maplewood.scheduler.repository.LookupRepository;
import com.maplewood.scheduler.repository.ScheduleRepository;
import com.maplewood.scheduler.util.TimeSlots;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ScheduleService {

    private final LookupRepository lookupRepo;
    private final ScheduleRepository scheduleRepo;

    public ScheduleService(LookupRepository lookupRepo, ScheduleRepository scheduleRepo) {
        this.lookupRepo = lookupRepo;
        this.scheduleRepo = scheduleRepo;
    }

    @Transactional
    public int generate(int semesterId, Integer sectionsPerCourse) {
        int semesterOrder = lookupRepo.getSemesterOrderById(semesterId);
        List<Course> courses = lookupRepo.findCoursesForSemesterOrder(semesterOrder);
        int createdSections = 0;

        // Caches
        Map<Integer, List<Teacher>> teachersBySpec = new HashMap<>();
        Map<Integer, List<Integer>> teacherHoursToday = new HashMap<>(); // key: teacherId*10 + day -> hours

        Map<Integer, List<Classroom>> roomsBySpec = new HashMap<>();

        for (Course c : courses) {
            int numSections = sectionsPerCourse != null ? sectionsPerCourse : 1;

            // fetch once
            teachersBySpec.computeIfAbsent(c.specializationId(), lookupRepo::findTeachersBySpecialization);
            roomsBySpec.computeIfAbsent(c.specializationId(), lookupRepo::findRoomsForSpecialization);

            List<Teacher> teachers = teachersBySpec.get(c.specializationId());
            List<Classroom> rooms = roomsBySpec.get(c.specializationId());
            if (teachers.isEmpty() || rooms.isEmpty()) continue; // skip if impossible

            for (int sectionNo = 1; sectionNo <= numSections; sectionNo++) {
                // pick a teacher with minimal total assigned meetings so far
                Teacher selectedTeacher = teachers.get((sectionNo - 1) % teachers.size());
                // pick first room that works
                Classroom selectedRoom = rooms.get((sectionNo - 1) % rooms.size());

                int sectionId = scheduleRepo.insertSection(c.id(), selectedTeacher.id(), selectedRoom.id(), semesterId, sectionNo, Math.min(selectedRoom.capacity(), 10));

                int remainingHours = c.hoursPerWeek();
                int dayIdx = 0;

                outer:
                while (remainingHours > 0 && dayIdx < TimeSlots.days().size()) {
                    int day = TimeSlots.days().get(dayIdx);
                    int placedToday = 0;

                    for (String slot : TimeSlots.SLOTS) {
                        // teacher/day limit 4 hours
                        int teacherKey = selectedTeacher.id() * 10 + day;
                        int currentTeacherHours = teacherHoursToday.getOrDefault(teacherKey, new ArrayList<>(List.of(0))).stream().findFirst().orElse(0);
                        if (currentTeacherHours >= 4) continue;

                        if (!scheduleRepo.teacherBusy(selectedTeacher.id(), semesterId, day, slot)
                                && !scheduleRepo.roomBusy(selectedRoom.id(), semesterId, day, slot)) {

                            scheduleRepo.insertMeeting(sectionId, day, slot, 60);
                            remainingHours -= 1;
                            placedToday += 1;
                            // record teacher hour
                            teacherHoursToday.put(teacherKey, new ArrayList<>(List.of(currentTeacherHours + 1)));
                            if (placedToday >= 2) break; // max 2 consecutive per day for this section
                            if (remainingHours <= 0) break outer;
                        }
                    }
                    dayIdx++;
                }
                createdSections++;
            }
        }
        return createdSections;
    }

    public List<SectionDTO> getSchedule(int semesterId) {
        return scheduleRepo.getSchedule(semesterId);
    }
}
