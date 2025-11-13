package com.maplewood.scheduler.service;

import com.maplewood.scheduler.model.ApiResponse;
import com.maplewood.scheduler.model.StudentProgress;
import com.maplewood.scheduler.model.StudentSchedule;
import com.maplewood.scheduler.model.AvailableSection;
import com.maplewood.scheduler.repository.LookupRepository;
import com.maplewood.scheduler.repository.ScheduleRepository;
import com.maplewood.scheduler.repository.StudentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepo;
    private final LookupRepository lookupRepo;
    private final ScheduleRepository scheduleRepo;
    private final JdbcTemplate jdbc;

    public StudentService(StudentRepository studentRepo, LookupRepository lookupRepo, ScheduleRepository scheduleRepo, JdbcTemplate jdbc) {
        this.studentRepo = studentRepo;
        this.lookupRepo = lookupRepo;
        this.scheduleRepo = scheduleRepo;
        this.jdbc = jdbc;
    }

    public StudentProgress progress(int studentId) {
        return studentRepo.progress(studentId);
    }

    @Transactional
    public ApiResponse<String> enroll(int studentId, int sectionId) {
        // Fetch context
        Map<String, Object> row = jdbc.queryForMap("""
            SELECT s.semester_id, c.prerequisite_id
            FROM sections s
            JOIN courses c ON c.id = s.course_id
            WHERE s.id = ?
        """, sectionId);
        int semesterId = (Integer) row.get("semester_id");
        Integer prereqId = (Integer) row.get("prerequisite_id");

        if (!studentRepo.hasPassedPrerequisite(studentId, prereqId)) {
            return new ApiResponse<>("Cannot enroll: prerequisite not satisfied", null, false);
        }
        // Check max 5 courses / semester
        int count = studentRepo.countEnrollmentsForSemester(studentId, semesterId);
        if (count >= 5) {
            return new ApiResponse<>("Cannot enroll: maximum 5 courses per semester reached", null, false);
        }
        // Conflict check: any meeting of the target section conflicts with existing selections
        var meetings = jdbc.query("SELECT day_of_week, start_time FROM section_meetings WHERE section_id = ?", (rs,i)->
                new Object[]{rs.getInt(1), rs.getString(2)}, sectionId);
        for (var m : meetings) {
            int day = (int) m[0];
            String start = (String) m[1];
            if (studentRepo.hasTimeConflict(studentId, semesterId, day, start)) {
                return new ApiResponse<>("Cannot enroll: time conflict on day " + day + " at " + start, null, false);
            }
        }
        // Capacity check
        Integer capacity = jdbc.queryForObject("SELECT capacity FROM sections WHERE id = ?", Integer.class, sectionId);
        Integer enrolled = jdbc.queryForObject("SELECT COUNT(*) FROM student_enrollments WHERE section_id = ?", Integer.class, sectionId);
        if (capacity != null && enrolled != null && enrolled >= capacity) {
            return new ApiResponse<>("Cannot enroll: section full", null, false);
        }

        studentRepo.enroll(studentId, sectionId);
        return new ApiResponse<>("Enrolled", "Successfully enrolled in section", true);
    }

    public StudentSchedule getStudentSchedule(int studentId, int semesterId) {
        return studentRepo.getStudentSchedule(studentId, semesterId);
    }

    public List<AvailableSection> getAvailableSections(int studentId, int semesterId) {
        return studentRepo.getAvailableSections(studentId, semesterId);
    }
}
