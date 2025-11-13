package com.maplewood.scheduler.repository;

import com.maplewood.scheduler.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LookupRepository {
    private final JdbcTemplate jdbc;

    public LookupRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Semester> findSemesters() {
        return jdbc.query(
                "SELECT id, name, year, order_in_year, COALESCE(is_active, 0) as is_active FROM semesters ORDER BY year DESC, order_in_year DESC",
                (rs, i) -> new Semester(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("year"),
                        rs.getInt("order_in_year"),
                        rs.getInt("is_active") == 1
                ));
    }

    public List<Course> findCoursesForSemesterOrder(int semesterOrder) {
        return jdbc.query("""
                SELECT id, code, name, credits, hours_per_week, specialization_id, prerequisite_id, semester_order, grade_level_min, grade_level_max
                FROM courses
                WHERE semester_order = ?
                ORDER BY code
            """, (rs, i) -> new Course(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("name"),
                    rs.getInt("credits"),
                    rs.getInt("hours_per_week"),
                    rs.getInt("specialization_id"),
                    (Integer) rs.getObject("prerequisite_id"),
                    rs.getInt("semester_order"),
                    rs.getInt("grade_level_min"),
                    rs.getInt("grade_level_max")
            ), semesterOrder);
    }

    public List<Teacher> findTeachersBySpecialization(int specializationId) {
        return jdbc.query(
                "SELECT id, first_name, last_name, specialization_id FROM teachers WHERE specialization_id = ? ORDER BY last_name",
                (rs, i) -> new Teacher(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("specialization_id")
                ), specializationId);
    }

    public List<Classroom> findRoomsForSpecialization(int specializationId) {
        return jdbc.query("""
            SELECT c.id, c.name, c.room_type_id, c.capacity
            FROM classrooms c
            JOIN specializations s ON s.room_type_id = c.room_type_id
            WHERE s.id = ?
            ORDER BY c.name
        """, (rs, i) -> new Classroom(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("room_type_id"),
                rs.getInt("capacity")
        ), specializationId);
    }

    public int getSemesterOrderById(int semesterId) {
        Integer order = jdbc.queryForObject("SELECT order_in_year FROM semesters WHERE id = ?", Integer.class, semesterId);
        if (order == null) throw new IllegalArgumentException("Semester not found: " + semesterId);
        return order;
    }
}
