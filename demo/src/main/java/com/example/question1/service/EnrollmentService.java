package com.example.question1.service;

import com.example.question1.model.EnrollRecord;
import com.example.question1.model.dto.EnrollmentCategoryResult;
import com.example.question1.model.dto.EnrollmentSearchResult;
import com.example.question1.model.dto.Question1ProcessResult;
import com.example.question1.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private static final List<String> COURSE_TYPES = List.of("公共课", "专业课", "选修课");

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<EnrollRecord> findRawRecords() {
        return enrollmentRepository.findAllForProcessing();
    }

    public Question1ProcessResult processFromDatabase() {
        return process(enrollmentRepository.findAllForProcessing());
    }

    public EnrollmentCategoryResult classifyProcessedRecords() {
        List<EnrollRecord> records = processFromDatabase().getRecords();
        Map<String, List<EnrollRecord>> categories = new LinkedHashMap<>();
        COURSE_TYPES.forEach(courseType -> categories.put(courseType, new ArrayList<>()));

        records.forEach(record -> {
            String courseType = recognizeCourseType(record);
            record.setCourseType(courseType);
            categories.computeIfAbsent(courseType, ignored -> new ArrayList<>()).add(record);
        });

        return new EnrollmentCategoryResult(categories);
    }

    public EnrollmentSearchResult search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new EnrollmentSearchResult(false, keyword, "无匹配选课记录", List.of());
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        List<EnrollRecord> matchedRecords = processFromDatabase().getRecords().stream()
                .filter(record -> containsIgnoreCase(record.getStudentId(), normalizedKeyword)
                        || containsIgnoreCase(record.getCourseId(), normalizedKeyword)
                        || containsIgnoreCase(record.getCourseName(), normalizedKeyword)
                        || containsIgnoreCase(recognizeCourseType(record), normalizedKeyword))
                .toList();

        if (matchedRecords.isEmpty()) {
            return new EnrollmentSearchResult(false, keyword, "无匹配选课记录", matchedRecords);
        }
        return new EnrollmentSearchResult(true, keyword, "检索成功", matchedRecords);
    }

    public Question1ProcessResult process(List<EnrollRecord> records) {
        Map<String, EnrollRecord> distinctRecords = records.stream()
                .collect(Collectors.toMap(
                        this::buildUniqueKey,
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        List<EnrollRecord> processedRecords = new ArrayList<>(distinctRecords.values());
        processedRecords.sort(Comparator
                .comparing(EnrollRecord::getStudentId)
                .thenComparing(EnrollRecord::getCourseId));

        List<String> formattedLines = processedRecords.stream()
                .map(EnrollRecord::toString)
                .toList();
        formattedLines.forEach(System.out::println);

        return new Question1ProcessResult(
                records.size(),
                records.size() - processedRecords.size(),
                processedRecords,
                formattedLines
        );
    }

    private String buildUniqueKey(EnrollRecord record) {
        return record.getStudentId() + "#" + record.getCourseId();
    }

    private String recognizeCourseType(EnrollRecord record) {
        if (StringUtils.hasText(record.getCourseType())) {
            return record.getCourseType();
        }
        String courseName = record.getCourseName();
        if (!StringUtils.hasText(courseName)) {
            return "选修课";
        }
        if (courseName.contains("高等数学") || courseName.contains("英语") || courseName.contains("体育")) {
            return "公共课";
        }
        if (courseName.contains("Java") || courseName.contains("数据库") || courseName.contains("操作系统")) {
            return "专业课";
        }
        return "选修课";
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return StringUtils.hasText(value) && value.toLowerCase().contains(normalizedKeyword);
    }
}
