package com.example.question1.repository;

import com.example.question1.model.EnrollRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EnrollmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public EnrollmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<EnrollRecord> findAllForProcessing() {
        String sql = """
                SELECT e.student_id, e.course_id, c.course_name, c.course_type
                FROM enrollments e
                JOIN courses c ON e.course_id = c.course_id
                ORDER BY e.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new EnrollRecord(
                rs.getString("student_id"),
                rs.getString("course_id"),
                rs.getString("course_name"),
                rs.getString("course_type")
        ));
    }
}
