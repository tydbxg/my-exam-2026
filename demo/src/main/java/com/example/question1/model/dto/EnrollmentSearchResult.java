package com.example.question1.model.dto;

import com.example.question1.model.EnrollRecord;

import java.util.List;

public class EnrollmentSearchResult {

    private boolean matched;
    private String keyword;
    private String message;
    private List<EnrollRecord> records;

    public EnrollmentSearchResult(boolean matched, String keyword, String message, List<EnrollRecord> records) {
        this.matched = matched;
        this.keyword = keyword;
        this.message = message;
        this.records = records;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<EnrollRecord> getRecords() {
        return records;
    }

    public void setRecords(List<EnrollRecord> records) {
        this.records = records;
    }
}
