package com.example.question1;

import com.example.question1.model.EnrollRecord;
import com.example.question1.model.dto.EnrollmentSearchResult;
import com.example.question1.model.dto.Question1ProcessResult;
import com.example.question1.repository.EnrollmentRepository;
import com.example.question1.service.EnrollmentService;
import com.example.question1.support.EnrollmentSampleData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentService(enrollmentRepository);
    }

    @Test
    void shouldProcessFromDatabase() {
        when(enrollmentRepository.findAllForProcessing())
                .thenReturn(EnrollmentSampleData.question1ExamInput());

        Question1ProcessResult result = enrollmentService.processFromDatabase();

        assertEquals(5, result.getInputCount());
        assertEquals(1, result.getRemovedCount());
        assertEquals(4, result.getOutputCount());
        assertEquals("数据库", result.getDataSource());
        assertEquals("学生ID：S000001，课程ID：C000001，课程名称：Java程序设计",
                result.getFormattedLines().get(0));
    }

    @Test
    void shouldReturnNotFoundWhenNoMatch() {
        when(enrollmentRepository.findAllForProcessing())
                .thenReturn(EnrollmentSampleData.question1ExamInput());
        enrollmentService.processFromDatabase();
        EnrollmentSearchResult search = enrollmentService.search("NOT_EXIST");
        assertFalse(search.isMatched());
        assertEquals("无匹配选课记录", search.getMessage());
    }

    @Test
    void shouldProcessOneThousandRecordsWithinOneSecond() {
        List<EnrollRecord> records = new ArrayList<>(1000);
        IntStream.range(0, 1000).forEach(i ->
                records.add(new EnrollRecord(
                        String.format("S%06d", i % 200),
                        String.format("C%06d", i % 50),
                        "课程" + i,
                        "专业课"
                )));
        when(enrollmentRepository.findAllForProcessing()).thenReturn(records);

        Question1ProcessResult result = enrollmentService.processFromDatabase();
        assertTrue(result.getProcessTimeMs() < 1000);
        assertTrue(result.getOutputCount() <= 1000);
    }
}
