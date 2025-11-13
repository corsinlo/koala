package com.maplewood.scheduler.controller;

import com.maplewood.scheduler.model.ApiResponse;
import com.maplewood.scheduler.model.StudentProgress;
import com.maplewood.scheduler.model.StudentSchedule;
import com.maplewood.scheduler.model.AvailableSection;
import com.maplewood.scheduler.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{id}/progress")
    public StudentProgress progress(@PathVariable int id) {
        return studentService.progress(id);
    }

    @PostMapping("/{id}/enroll")
    public ApiResponse<String> enroll(@PathVariable int id, @RequestParam int sectionId) {
        return studentService.enroll(id, sectionId);
    }

    @GetMapping("/{id}/schedule")
    public StudentSchedule getStudentSchedule(@PathVariable int id, @RequestParam int semesterId) {
        return studentService.getStudentSchedule(id, semesterId);
    }

    @GetMapping("/{id}/available-sections")
    public List<AvailableSection> getAvailableSections(@PathVariable int id, @RequestParam int semesterId) {
        return studentService.getAvailableSections(id, semesterId);
    }
}
