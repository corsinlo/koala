package com.maplewood.scheduler.repository;

import com.maplewood.scheduler.model.StudentProgress;
import com.maplewood.scheduler.model.StudentSchedule;
import com.maplewood.scheduler.model.AvailableSection;
import com.maplewood.scheduler.model.SectionMeeting;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.ArrayList;

@Repository
public class StudentRepository {
    private final JdbcTemplate jdbc;

    public StudentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public StudentProgress progress(int studentId) {
        String sql = """
        SELECT s.id as student_id, s.first_name, s.last_name, s.grade_level,
               COUNT(h.course_id) as courses_taken,
               SUM(CASE WHEN h.status = 'passed' THEN 1 ELSE 0 END) as courses_passed,
               COALESCE(SUM(CASE WHEN h.status = 'passed' THEN c.credits ELSE 0 END), 0) as credits_earned,
               COALESCE(ROUND(
                    CAST(SUM(CASE WHEN h.status = 'passed' THEN c.credits ELSE 0 END) AS FLOAT)
                    / NULLIF(SUM(CASE WHEN h.status IS NOT NULL THEN c.credits ELSE 0 END), 0) * 4.0, 2), 0.0) as gpa
        FROM students s
        LEFT JOIN student_course_history h ON s.id = h.student_id
        LEFT JOIN courses c ON h.course_id = c.id
        WHERE s.id = ?
        GROUP BY s.id
        """;
        return jdbc.queryForObject(sql, (rs, i) -> {
            double creditsEarned = rs.getDouble("credits_earned");
            double creditsRequired = 30.0; // Challenge requirement: 30 credits over 4 years
            double creditsRemaining = Math.max(0, creditsRequired - creditsEarned);
            int gradeLevel = rs.getInt("grade_level");
            int expectedGraduationYear = 2024 + (12 - gradeLevel) + 1; // Based on grade level
            boolean onTrackToGraduate = creditsEarned >= (gradeLevel - 9 + 1) * 7.5; // ~7.5 credits per year
            String graduationStatus = creditsEarned >= creditsRequired ? "Graduated" :
                    onTrackToGraduate ? "On Track" : "At Risk";

            return new StudentProgress(
                    rs.getInt("student_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    gradeLevel,
                    rs.getInt("courses_taken"),
                    rs.getInt("courses_passed"),
                    creditsEarned,
                    rs.getDouble("gpa"),
                    creditsRequired,
                    creditsRemaining,
                    expectedGraduationYear,
                    onTrackToGraduate,
                    graduationStatus
            );
        }, studentId);
    }

    public boolean hasPassedPrerequisite(int studentId, Integer prerequisiteCourseId) {
        if (prerequisiteCourseId == null) return true;
        Integer cnt = jdbc.queryForObject("""
            SELECT COUNT(*) FROM student_course_history
            WHERE student_id = ? AND course_id = ? AND status = 'passed'
        """, Integer.class, studentId, prerequisiteCourseId);
        return cnt != null && cnt > 0;
    }

    public boolean hasTimeConflict(int studentId, int semesterId, int dayOfWeek, String startTime) {
        Integer cnt = jdbc.queryForObject("""
            SELECT COUNT(*) FROM student_enrollments se
            JOIN section_meetings m ON m.section_id = se.section_id
            JOIN sections s ON s.id = se.section_id
            WHERE se.student_id = ? AND s.semester_id = ? AND m.day_of_week = ? AND m.start_time = ?
        """, Integer.class, studentId, semesterId, dayOfWeek, startTime);
        return cnt != null && cnt > 0;
    }

    public int countEnrollmentsForSemester(int studentId, int semesterId) {
        Integer cnt = jdbc.queryForObject("""
            SELECT COUNT(*) FROM student_enrollments se
            JOIN sections s ON s.id = se.section_id
            WHERE se.student_id = ? AND s.semester_id = ?
        """, Integer.class, studentId, semesterId);
        return cnt == null ? 0 : cnt;
    }

    public void enroll(int studentId, int sectionId) {
        jdbc.update("INSERT INTO student_enrollments(student_id, section_id) VALUES (?,?)", studentId, sectionId);
    }

    public StudentSchedule getStudentSchedule(int studentId, int semesterId) {
        // Get student info
        String studentSql = """
            SELECT s.id, s.first_name, s.last_name, sem.name as semester_name
            FROM students s, semesters sem
            WHERE s.id = ? AND sem.id = ?
        """;

        var studentInfo = jdbc.queryForObject(studentSql, (rs, i) -> new Object[]{
            rs.getInt("id"),
            rs.getString("first_name") + " " + rs.getString("last_name"),
            rs.getString("semester_name")
        }, studentId, semesterId);

        // Get enrolled sections
        String sectionsSql = """
            SELECT se.section_id, c.code, c.name, sec.section_number,
                   t.first_name || ' ' || t.last_name as teacher_name,
                   cl.name as room_name, c.credits, sec.capacity,
                   COUNT(se2.id) as enrolled_count
            FROM student_enrollments se
            JOIN sections sec ON se.section_id = sec.id
            JOIN courses c ON sec.course_id = c.id
            JOIN teachers t ON sec.teacher_id = t.id
            JOIN classrooms cl ON sec.room_id = cl.id
            LEFT JOIN student_enrollments se2 ON sec.id = se2.section_id
            WHERE se.student_id = ? AND sec.semester_id = ?
            GROUP BY se.section_id, c.code, c.name, sec.section_number, teacher_name, cl.name, c.credits, sec.capacity
        """;

        List<StudentSchedule.EnrolledSection> enrolledSections = jdbc.query(sectionsSql, (rs, i) -> {
            int sectionId = rs.getInt("section_id");
            List<SectionMeeting> meetings = getMeetingsForSection(sectionId);
            int enrolled = rs.getInt("enrolled_count");
            int capacity = rs.getInt("capacity");

            return new StudentSchedule.EnrolledSection(
                sectionId,
                rs.getString("code"),
                rs.getString("name"),
                rs.getInt("section_number"),
                rs.getString("teacher_name"),
                rs.getString("room_name"),
                rs.getDouble("credits"),
                meetings,
                capacity,
                enrolled,
                capacity - enrolled
            );
        }, studentId, semesterId);

        StudentProgress progress = progress(studentId);

        return new StudentSchedule(
            studentId,
            (String) studentInfo[1],
            semesterId,
            (String) studentInfo[2],
            enrolledSections,
            progress
        );
    }

    public List<AvailableSection> getAvailableSections(int studentId, int semesterId) {
        String sql = """
            SELECT sec.id as section_id, c.code, c.name, sec.section_number,
                   t.first_name || ' ' || t.last_name as teacher_name,
                   cl.name as room_name, c.credits, c.hours_per_week,
                   sec.capacity, COUNT(se.id) as enrolled_count,
                   prereq.code as prerequisite_course,
                   c.grade_level_min, sp.name as specialization_name,
                   (CASE WHEN c.semester_order <= 4 THEN 1 ELSE 0 END) as is_core
            FROM sections sec
            JOIN courses c ON sec.course_id = c.id
            JOIN teachers t ON sec.teacher_id = t.id
            JOIN classrooms cl ON sec.room_id = cl.id
            JOIN specializations sp ON c.specialization_id = sp.id
            LEFT JOIN courses prereq ON c.prerequisite_id = prereq.id
            LEFT JOIN student_enrollments se ON sec.id = se.section_id
            WHERE sec.semester_id = ?
            GROUP BY sec.id, c.code, c.name, sec.section_number, teacher_name, cl.name, c.credits,
                     c.hours_per_week, sec.capacity, prerequisite_course, c.grade_level_min,
                     specialization_name, is_core
            ORDER BY c.code, sec.section_number
        """;

        return jdbc.query(sql, (rs, i) -> {
            int sectionId = rs.getInt("section_id");
            List<SectionMeeting> meetings = getMeetingsForSection(sectionId);

            // Check enrollment eligibility
            boolean canEnroll = true;
            String enrollmentStatus = "available";
            String statusMessage = "Available for enrollment";

            // Check prerequisites
            String prerequisiteCourse = rs.getString("prerequisite_course");
            if (prerequisiteCourse != null) {
                // This would need the actual prerequisite check logic
                Integer prereqId = null; // You'd get this from the course data
                if (!hasPassedPrerequisite(studentId, prereqId)) {
                    canEnroll = false;
                    enrollmentStatus = "prerequisite_missing";
                    statusMessage = "Prerequisite required: " + prerequisiteCourse;
                }
            }

            // Check capacity
            int capacity = rs.getInt("capacity");
            int enrolled = rs.getInt("enrolled_count");
            if (enrolled >= capacity) {
                canEnroll = false;
                enrollmentStatus = "section_full";
                statusMessage = "Section is full";
            }

            // Check semester limit (5 courses max)
            if (countEnrollmentsForSemester(studentId, semesterId) >= 5) {
                canEnroll = false;
                enrollmentStatus = "semester_limit";
                statusMessage = "Maximum 5 courses per semester reached";
            }

            return new AvailableSection(
                sectionId,
                rs.getString("code"),
                rs.getString("name"),
                rs.getInt("section_number"),
                rs.getString("teacher_name"),
                rs.getString("room_name"),
                rs.getDouble("credits"),
                rs.getInt("hours_per_week"),
                meetings,
                capacity,
                enrolled,
                capacity - enrolled,
                canEnroll,
                enrollmentStatus,
                statusMessage,
                prerequisiteCourse,
                rs.getInt("grade_level_min"),
                rs.getString("specialization_name"),
                rs.getBoolean("is_core")
            );
        }, semesterId);
    }

    private List<SectionMeeting> getMeetingsForSection(int sectionId) {
        String sql = """
            SELECT id, section_id, day_of_week, start_time, duration_minutes
            FROM section_meetings
            WHERE section_id = ?
            ORDER BY day_of_week, start_time
        """;

        return jdbc.query(sql, (rs, i) -> new SectionMeeting(
            rs.getInt("id"),
            rs.getInt("section_id"),
            rs.getInt("day_of_week"),
            rs.getString("start_time"),
            rs.getInt("duration_minutes")
        ), sectionId);
    }
}
