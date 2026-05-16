package com.example.question1.controller;

import com.example.question1.model.EnrollRecord;
import com.example.question1.model.dto.EnrollmentCategoryResult;
import com.example.question1.model.dto.EnrollmentSearchResult;
import com.example.question1.model.dto.Question1ProcessResult;
import com.example.question1.service.EnrollmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/question1/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/raw")
    public List<EnrollRecord> raw() {
        return enrollmentService.findRawRecords();
    }

    @GetMapping("/process")
    public Question1ProcessResult process() {
        return enrollmentService.processFromDatabase();
    }

    @GetMapping("/categories")
    public EnrollmentCategoryResult categories() {
        return enrollmentService.classifyProcessedRecords();
    }

    @GetMapping("/search")
    public EnrollmentSearchResult search(@RequestParam String keyword) {
        return enrollmentService.search(keyword);
    }
}
