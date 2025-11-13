package com.maplewood.scheduler.repository;

import com.maplewood.scheduler.model.*;
import static com.maplewood.scheduler.model.SchedulingModels.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SchedulingRepository {
    private final JdbcTemplate jdbcTemplate;

    public SchedulingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CourseData> getCoursesForSemester(Integer semesterId) {
        String sql = """
            SELECT c.id, c.code, c.name, c.credits, c.hours_per_week,
                   c.specialization_id, c.semester_order, s.name as specialization_name
            FROM courses c
            JOIN specializations s ON c.specialization_id = s.id
            JOIN semesters sem ON sem.id = ?
            WHERE c.semester_order = sem.order_in_year
            ORDER BY c.code
        """;

        return jdbcTemplate.query(sql, courseRowMapper(), semesterId);
    }

    public List<TeacherData> getAvailableTeachers() {
        String sql = """
            SELECT t.id, t.first_name, t.last_name, t.specialization_id,
                   t.max_daily_hours, s.name as specialization_name
            FROM teachers t
            JOIN specializations s ON t.specialization_id = s.id
            ORDER BY t.last_name, t.first_name
        """;

        return jdbcTemplate.query(sql, teacherRowMapper());
    }

    public List<ClassroomData> getAvailableClassrooms() {
        String sql = """
            SELECT c.id, c.name, c.room_type_id, c.capacity,
                   rt.name as room_type_name
            FROM classrooms c
            JOIN room_types rt ON c.room_type_id = rt.id
            ORDER BY c.name
        """;

        return jdbcTemplate.query(sql, classroomRowMapper());
    }

    public void createSection(Integer courseId, Integer teacherId, Integer roomId,
                              Integer semesterId, Integer sectionNumber) {
        String sql = """
            INSERT INTO sections (course_id, teacher_id, room_id, semester_id, section_number, capacity)
            VALUES (?, ?, ?, ?, ?, 10)
        """;

        jdbcTemplate.update(sql, courseId, teacherId, roomId, semesterId, sectionNumber);
    }

    public Integer getLastInsertedSectionId() {
        return jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Integer.class);
    }

    public void createSectionMeeting(Integer sectionId, Integer dayOfWeek,
                                   String startTime, Integer durationMinutes) {
        String sql = """
            INSERT INTO section_meetings (section_id, day_of_week, start_time, duration_minutes)
            VALUES (?, ?, ?, ?)
        """;

        jdbcTemplate.update(sql, sectionId, dayOfWeek, startTime, durationMinutes);
    }

    public List<Section> getSectionsForSemester(Integer semesterId) {
        String sql = """
            SELECT s.id, s.course_id, s.teacher_id, s.room_id, s.semester_id,
                   s.section_number, s.capacity,
                   c.code as course_code, c.name as course_name,
                   (t.first_name || ' ' || t.last_name) as teacher_name,
                   cl.name as room_name,
                   COUNT(se.id) as enrolled_students
            FROM sections s
            JOIN courses c ON s.course_id = c.id
            JOIN teachers t ON s.teacher_id = t.id
            JOIN classrooms cl ON s.room_id = cl.id
            LEFT JOIN student_enrollments se ON s.id = se.section_id
            WHERE s.semester_id = ?
            GROUP BY s.id, s.course_id, s.teacher_id, s.room_id, s.semester_id,
                     s.section_number, s.capacity, c.code, c.name, teacher_name, cl.name
            ORDER BY c.code, s.section_number
        """;

        return jdbcTemplate.query(sql, sectionRowMapper(), semesterId);
    }

    public List<SectionMeeting> getMeetingsForSection(Integer sectionId) {
        String sql = """
            SELECT id, section_id, day_of_week, start_time, duration_minutes
            FROM section_meetings
            WHERE section_id = ?
            ORDER BY day_of_week, start_time
        """;

        return jdbcTemplate.query(sql, meetingRowMapper(), sectionId);
    }

    public void clearScheduleForSemester(Integer semesterId) {
        // Clear meetings first (foreign key constraint)
        jdbcTemplate.update("""
            DELETE FROM section_meetings
            WHERE section_id IN (SELECT id FROM sections WHERE semester_id = ?)
        """, semesterId);

        // Clear enrollments
        jdbcTemplate.update("""
            DELETE FROM student_enrollments
            WHERE section_id IN (SELECT id FROM sections WHERE semester_id = ?)
        """, semesterId);

        // Clear sections
        jdbcTemplate.update("DELETE FROM sections WHERE semester_id = ?", semesterId);
    }

    private RowMapper<CourseData> courseRowMapper() {
        return (rs, rowNum) -> new CourseData(
            rs.getInt("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getDouble("credits"),
            rs.getInt("hours_per_week"),
            rs.getInt("specialization_id"),
            rs.getString("specialization_name")
        );
    }

    private RowMapper<TeacherData> teacherRowMapper() {
        return (rs, rowNum) -> new TeacherData(
            rs.getInt("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getInt("specialization_id"),
            rs.getString("specialization_name"),
            rs.getInt("max_daily_hours")
        );
    }

    private RowMapper<ClassroomData> classroomRowMapper() {
        return (rs, rowNum) -> new ClassroomData(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("room_type_id"),
            rs.getString("room_type_name"),
            rs.getInt("capacity")
        );
    }

    private RowMapper<Section> sectionRowMapper() {
        return (rs, rowNum) -> new Section(
            rs.getInt("id"),
            rs.getInt("course_id"),
            rs.getInt("teacher_id"),
            rs.getInt("room_id"),
            rs.getInt("semester_id"),
            rs.getInt("section_number"),
            rs.getInt("capacity"),
            rs.getString("course_name"),
            rs.getString("course_code"),
            rs.getString("teacher_name"),
            rs.getString("room_name"),
            rs.getInt("enrolled_students")
        );
    }

    private RowMapper<SectionMeeting> meetingRowMapper() {
        return (rs, rowNum) -> new SectionMeeting(
            rs.getInt("id"),
            rs.getInt("section_id"),
            rs.getInt("day_of_week"),
            rs.getString("start_time"),
            rs.getInt("duration_minutes")
        );
    }
}