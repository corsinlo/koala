package com.maplewood.scheduler.controller;

import com.maplewood.scheduler.model.Semester;
import com.maplewood.scheduler.repository.LookupRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MetaController {

    private final LookupRepository lookupRepo;

    public MetaController(LookupRepository lookupRepo) {
        this.lookupRepo = lookupRepo;
    }

    @GetMapping("/semesters")
    public List<Semester> semesters() {
        return lookupRepo.findSemesters();
    }

    @GetMapping("/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "ok");
    }
}
