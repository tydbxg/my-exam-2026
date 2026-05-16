package com.example.question1.service;

import com.example.question1.model.EnrollRecord;
import com.example.question1.model.dto.EnrollmentCategoryResult;
import com.example.question1.model.dto.EnrollmentImportSubmitResult;
import com.example.question1.model.dto.EnrollmentImportTaskResult;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private static final int BATCH_SIZE = 100;
    private static final List<String> COURSE_TYPES = List.of("公共课", "专业课", "选修课");

    private final EnrollmentRepository enrollmentRepository;
    private final ExecutorService importExecutor = Executors.newFixedThreadPool(4);
    private final Map<String, EnrollmentImportTaskResult> importTasks = new ConcurrentHashMap<>();

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

    public Question1ProcessResult importCsv(String csvText) {
        if (!StringUtils.hasText(csvText)) {
            return process(List.of());
        }
        return process(parseCsvLines(csvText.lines().toList(), new AtomicInteger()));
    }

    public EnrollmentImportSubmitResult submitImportCsv(String csvText) {
        String taskId = UUID.randomUUID().toString();
        EnrollmentImportTaskResult task = new EnrollmentImportTaskResult(taskId, "PROCESSING", "CSV批量导入处理中");
        List<String> lines = normalizeCsvLines(csvText);
        task.setTotalCount(lines.size());
        importTasks.put(taskId, task);

        importExecutor.submit(() -> runImportTask(taskId, lines));
        return new EnrollmentImportSubmitResult(taskId, "PROCESSING", "任务已提交，支持500条以上数据异步分批导入");
    }

    public EnrollmentImportTaskResult getImportTask(String taskId) {
        EnrollmentImportTaskResult task = importTasks.get(taskId);
        if (task == null) {
            return new EnrollmentImportTaskResult(taskId, "NOT_FOUND", "导入任务不存在");
        }
        return task;
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

    private void runImportTask(String taskId, List<String> lines) {
        EnrollmentImportTaskResult task = importTasks.get(taskId);
        AtomicInteger processedCount = new AtomicInteger();
        try {
            List<Future<List<EnrollRecord>>> futures = new ArrayList<>();
            for (int start = 0; start < lines.size(); start += BATCH_SIZE) {
                int end = Math.min(start + BATCH_SIZE, lines.size());
                List<String> batchLines = lines.subList(start, end);
                futures.add(importExecutor.submit(() -> parseCsvLines(batchLines, processedCount)));
            }

            List<EnrollRecord> importedRecords = new ArrayList<>(lines.size());
            for (Future<List<EnrollRecord>> future : futures) {
                importedRecords.addAll(future.get());
                task.setProcessedCount(processedCount.get());
            }

            Question1ProcessResult result = process(importedRecords);
            task.setResult(result);
            task.setProcessedCount(lines.size());
            task.setStatus("COMPLETED");
            task.setMessage("CSV批量导入完成");
        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setMessage("CSV批量导入失败：" + e.getMessage());
        }
    }

    private List<String> normalizeCsvLines(String csvText) {
        if (!StringUtils.hasText(csvText)) {
            return List.of();
        }
        return csvText.lines()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private List<EnrollRecord> parseCsvLines(List<String> lines, AtomicInteger processedCount) {
        List<EnrollRecord> records = new ArrayList<>(lines.size());
        for (String line : lines) {
            records.add(parseCsvLine(line));
            processedCount.incrementAndGet();
        }
        return records;
    }

    private String buildUniqueKey(EnrollRecord record) {
        return record.getStudentId() + "#" + record.getCourseId();
    }

    private EnrollRecord parseCsvLine(String line) {
        String[] values = line.split(",", -1);
        if (values.length != 4) {
            throw new IllegalArgumentException("CSV每行必须包含4列：学生ID,课程ID,课程名称,课程类型");
        }
        return new EnrollRecord(
                values[0].trim(),
                values[1].trim(),
                values[2].trim(),
                values[3].trim()
        );
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
