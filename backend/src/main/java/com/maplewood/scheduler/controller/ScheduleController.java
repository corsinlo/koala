package com.maplewood.scheduler.controller;

import com.maplewood.scheduler.model.SectionDTO;
import com.maplewood.scheduler.service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestParam int semesterId,
                                        @RequestParam(required = false) Integer sectionsPerCourse) {
        int created = scheduleService.generate(semesterId, sectionsPerCourse);
        Map<String, Object> out = new HashMap<>();
        out.put("createdSections", created);
        return out;
    }

    @GetMapping
    public List<SectionDTO> get(@RequestParam int semesterId) {
        return scheduleService.getSchedule(semesterId);
    }
}
