package com.example.question1.model.dto;

import com.example.question1.model.EnrollRecord;

import java.util.List;

public class Question1ProcessResult {

    private int inputCount;
    private int removedCount;
    private int outputCount;
    private List<EnrollRecord> records;
    private List<String> formattedLines;

    public Question1ProcessResult(int inputCount, int removedCount, List<EnrollRecord> records, List<String> formattedLines) {
        this.inputCount = inputCount;
        this.removedCount = removedCount;
        this.outputCount = records.size();
        this.records = records;
        this.formattedLines = formattedLines;
    }

    public int getInputCount() {
        return inputCount;
    }

    public void setInputCount(int inputCount) {
        this.inputCount = inputCount;
    }

    public int getRemovedCount() {
        return removedCount;
    }

    public void setRemovedCount(int removedCount) {
        this.removedCount = removedCount;
    }

    public int getOutputCount() {
        return outputCount;
    }

    public void setOutputCount(int outputCount) {
        this.outputCount = outputCount;
    }

    public List<EnrollRecord> getRecords() {
        return records;
    }

    public void setRecords(List<EnrollRecord> records) {
        this.records = records;
    }

    public List<String> getFormattedLines() {
        return formattedLines;
    }

    public void setFormattedLines(List<String> formattedLines) {
        this.formattedLines = formattedLines;
    }
}
