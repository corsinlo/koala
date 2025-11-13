package com.maplewood.scheduler;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SchemaInitializer {

    @Bean
    ApplicationRunner initSchema(JdbcTemplate jdbc) {
        return args -> {
            // Create minimal extra tables for scheduling & enrollment
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS sections (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  course_id INTEGER NOT NULL,
                  teacher_id INTEGER NOT NULL,
                  room_id INTEGER NOT NULL,
                  semester_id INTEGER NOT NULL,
                  section_number INTEGER NOT NULL,
                  capacity INTEGER NOT NULL DEFAULT 10
                );
            """);
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS section_meetings (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  section_id INTEGER NOT NULL,
                  day_of_week INTEGER NOT NULL, -- 1 = Monday .. 5 = Friday
                  start_time TEXT NOT NULL,     -- 'HH:mm'
                  duration_minutes INTEGER NOT NULL
                );
            """);
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS student_enrollments (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  student_id INTEGER NOT NULL,
                  section_id INTEGER NOT NULL
                );
            """);
            // Helpful indexes
            jdbc.execute("CREATE INDEX IF NOT EXISTS idx_sections_semester ON sections(semester_id);");
            jdbc.execute("CREATE INDEX IF NOT EXISTS idx_meetings_section ON section_meetings(section_id);");
            jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_section ON sections(course_id, semester_id, section_number);");
        };
    }
}
