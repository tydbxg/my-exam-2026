package com.example.question1.model.dto;

public class EnrollmentImportTaskResult {

    private String taskId;
    private String status;
    private String message;
    private int totalCount;
    private int processedCount;
    private Question1ProcessResult result;

    public EnrollmentImportTaskResult(String taskId, String status, String message) {
        this.taskId = taskId;
        this.status = status;
        this.message = message;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }

    public Question1ProcessResult getResult() {
        return result;
    }

    public void setResult(Question1ProcessResult result) {
        this.result = result;
    }
}
