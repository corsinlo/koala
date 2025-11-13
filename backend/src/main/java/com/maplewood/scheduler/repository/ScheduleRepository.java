package com.maplewood.scheduler.repository;

import com.maplewood.scheduler.model.MeetingDTO;
import com.maplewood.scheduler.model.SectionDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ScheduleRepository {
    private final JdbcTemplate jdbc;

    public ScheduleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void deleteBySemester(int semesterId) {
        jdbc.update("DELETE FROM section_meetings WHERE section_id IN (SELECT id FROM sections WHERE semester_id = ?)", semesterId);
        jdbc.update("DELETE FROM student_enrollments WHERE section_id IN (SELECT id FROM sections WHERE semester_id = ?)", semesterId);
        jdbc.update("DELETE FROM sections WHERE semester_id = ?", semesterId);
    }

    public int insertSection(int courseId, int teacherId, int roomId, int semesterId, int sectionNumber, int capacity) {
        jdbc.update("INSERT INTO sections(course_id, teacher_id, room_id, semester_id, section_number, capacity) VALUES (?,?,?,?,?,?)",
                courseId, teacherId, roomId, semesterId, sectionNumber, capacity);
        Integer id = jdbc.queryForObject("SELECT last_insert_rowid()", Integer.class);
        return id == null ? -1 : id;
    }

    public void insertMeeting(int sectionId, int dayOfWeek, String startTime, int durationMinutes) {
        jdbc.update("INSERT INTO section_meetings(section_id, day_of_week, start_time, duration_minutes) VALUES (?,?,?,?)",
                sectionId, dayOfWeek, startTime, durationMinutes);
    }

    public List<SectionDTO> getSchedule(int semesterId) {
        String sql = """
            SELECT s.id, s.section_number, s.capacity,
                   c.code as course_code, c.name as course_name,
                   t.first_name || ' ' || t.last_name as teacher_name,
                   r.name as room_name,
                   (SELECT COUNT(*) FROM student_enrollments se WHERE se.section_id = s.id) as enrolled
            FROM sections s
            JOIN courses c ON c.id = s.course_id
            JOIN teachers t ON t.id = s.teacher_id
            JOIN classrooms r ON r.id = s.room_id
            WHERE s.semester_id = ?
            ORDER BY c.code, s.section_number
        """;
        var sections = jdbc.query(sql, (rs, i) -> new SectionDTO(
                rs.getInt("id"),
                rs.getString("course_code"),
                rs.getString("course_name"),
                rs.getInt("section_number"),
                rs.getString("teacher_name"),
                rs.getString("room_name"),
                rs.getInt("capacity"),
                rs.getInt("enrolled"),
                List.of()
        ), semesterId);
        for (int i = 0; i < sections.size(); i++) {
            SectionDTO s = sections.get(i);
            var meetings = jdbc.query("SELECT day_of_week, start_time, duration_minutes FROM section_meetings WHERE section_id = ? ORDER BY day_of_week, start_time",
                    (rs, j) -> new MeetingDTO(rs.getInt("day_of_week"), rs.getString("start_time"), rs.getInt("duration_minutes")),
                    s.id());
            sections.set(i, new SectionDTO(s.id(), s.courseCode(), s.courseName(), s.sectionNumber(), s.teacherName(), s.roomName(), s.capacity(), s.enrolled(), meetings));
        }
        return sections;
    }

    public boolean teacherBusy(int teacherId, int semesterId, int dayOfWeek, String startTime) {
        Integer cnt = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM sections s
            JOIN section_meetings m ON m.section_id = s.id
            WHERE s.teacher_id = ? AND s.semester_id = ?
              AND m.day_of_week = ? AND m.start_time = ?
        """, Integer.class, teacherId, semesterId, dayOfWeek, startTime);
        return cnt != null && cnt > 0;
    }

    public boolean roomBusy(int roomId, int semesterId, int dayOfWeek, String startTime) {
        Integer cnt = jdbc.queryForObject("""
            SELECT COUNT(*)
            FROM sections s
            JOIN section_meetings m ON m.section_id = s.id
            WHERE s.room_id = ? AND s.semester_id = ?
              AND m.day_of_week = ? AND m.start_time = ?
        """, Integer.class, roomId, semesterId, dayOfWeek, startTime);
        return cnt != null && cnt > 0;
    }

    public int teacherDailyHours(int teacherId, int semesterId, int dayOfWeek) {
        Integer mins = jdbc.queryForObject("""
            SELECT COALESCE(SUM(m.duration_minutes), 0)
            FROM sections s
            JOIN section_meetings m ON m.section_id = s.id
            WHERE s.teacher_id = ? AND s.semester_id = ? AND m.day_of_week = ?
        """, Integer.class, teacherId, semesterId, dayOfWeek);
        return (mins == null ? 0 : mins) / 60;
    }
}
