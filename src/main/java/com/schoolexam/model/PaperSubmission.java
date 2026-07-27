package com.schoolexam.model;

import java.time.LocalDateTime;

public class PaperSubmission {
    private Long id;
    private Long examId;
    private String studentRollNumber;
    private String studentName;
    private String originalFileName;
    private String storedFilePath;
    private String ocrTextContent;
    private String status; // PENDING, PROCESSING, EVALUATED, FAILED
    private LocalDateTime uploadedAt;

    public PaperSubmission() {}

    public PaperSubmission(Long id, Long examId, String studentRollNumber, String studentName, String originalFileName, String storedFilePath, String ocrTextContent, String status, LocalDateTime uploadedAt) {
        this.id = id;
        this.examId = examId;
        this.studentRollNumber = studentRollNumber;
        this.studentName = studentName;
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath;
        this.ocrTextContent = ocrTextContent;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getStudentRollNumber() { return studentRollNumber; }
    public void setStudentRollNumber(String studentRollNumber) { this.studentRollNumber = studentRollNumber; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoredFilePath() { return storedFilePath; }
    public void setStoredFilePath(String storedFilePath) { this.storedFilePath = storedFilePath; }

    public String getOcrTextContent() { return ocrTextContent; }
    public void setOcrTextContent(String ocrTextContent) { this.ocrTextContent = ocrTextContent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
