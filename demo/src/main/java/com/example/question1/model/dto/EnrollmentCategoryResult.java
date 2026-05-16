package com.example.question1.model.dto;

import com.example.question1.model.EnrollRecord;

import java.util.List;
import java.util.Map;

public class EnrollmentCategoryResult {

    private Map<String, List<EnrollRecord>> categories;

    public EnrollmentCategoryResult(Map<String, List<EnrollRecord>> categories) {
        this.categories = categories;
    }

    public Map<String, List<EnrollRecord>> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, List<EnrollRecord>> categories) {
        this.categories = categories;
    }
}
