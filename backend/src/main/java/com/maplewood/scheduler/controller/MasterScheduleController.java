package com.maplewood.scheduler.controller;

import com.maplewood.scheduler.model.ApiResponse;
import com.maplewood.scheduler.model.MasterSchedule;
import com.maplewood.scheduler.model.ScheduleRequest;
import com.maplewood.scheduler.service.MasterScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master-schedule")
@CrossOrigin(origins = "*")
public class MasterScheduleController {

    private final MasterScheduleService scheduleService;

    public MasterScheduleController(MasterScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<MasterSchedule>> generateSchedule(@RequestBody ScheduleRequest request) {
        try {
            MasterSchedule schedule = scheduleService.generateScheduleForSemester(request.semesterId());
            return ResponseEntity.ok(new ApiResponse<>(
                "Schedule generated successfully",
                schedule,
                true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                "Failed to generate schedule: " + e.getMessage(),
                null,
                false
            ));
        }
    }

    @GetMapping("/semester/{semesterId}")
    public ResponseEntity<ApiResponse<MasterSchedule>> getSchedule(@PathVariable Integer semesterId) {
        try {
            MasterSchedule schedule = scheduleService.getExistingSchedule(semesterId);
            return ResponseEntity.ok(new ApiResponse<>(
                "Schedule retrieved successfully",
                schedule,
                true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                "Failed to retrieve schedule: " + e.getMessage(),
                null,
                false
            ));
        }
    }
}