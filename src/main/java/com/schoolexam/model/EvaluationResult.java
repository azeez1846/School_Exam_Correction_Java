package com.schoolexam.model;

import java.time.LocalDateTime;

public class EvaluationResult {
    private Long id;
    private Long submissionId;
    private Double totalMarksObtained;
    private Double maxMarks;
    private Double percentageScore;
    private String letterGrade;
    private String rubricBreakdownJson;
    private String strengthsJson;
    private String improvementAreasJson;
    private String customTeacherFeedback;
    private String evaluatedByModel;
    private Boolean isTeacherOverridden;
    private String teacherNotes;
    private LocalDateTime evaluatedAt;

    public EvaluationResult() {}

    public EvaluationResult(Long id, Long submissionId, Double totalMarksObtained, Double maxMarks, Double percentageScore, String letterGrade, String rubricBreakdownJson, String strengthsJson, String improvementAreasJson, String customTeacherFeedback, String evaluatedByModel, Boolean isTeacherOverridden, String teacherNotes, LocalDateTime evaluatedAt) {
        this.id = id;
        this.submissionId = submissionId;
        this.totalMarksObtained = totalMarksObtained;
        this.maxMarks = maxMarks;
        this.percentageScore = percentageScore;
        this.letterGrade = letterGrade;
        this.rubricBreakdownJson = rubricBreakdownJson;
        this.strengthsJson = strengthsJson;
        this.improvementAreasJson = improvementAreasJson;
        this.customTeacherFeedback = customTeacherFeedback;
        this.evaluatedByModel = evaluatedByModel;
        this.isTeacherOverridden = isTeacherOverridden;
        this.teacherNotes = teacherNotes;
        this.evaluatedAt = evaluatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Double getTotalMarksObtained() { return totalMarksObtained; }
    public void setTotalMarksObtained(Double totalMarksObtained) { this.totalMarksObtained = totalMarksObtained; }

    public Double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Double maxMarks) { this.maxMarks = maxMarks; }

    public Double getPercentageScore() { return percentageScore; }
    public void setPercentageScore(Double percentageScore) { this.percentageScore = percentageScore; }

    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    public String getRubricBreakdownJson() { return rubricBreakdownJson; }
    public void setRubricBreakdownJson(String rubricBreakdownJson) { this.rubricBreakdownJson = rubricBreakdownJson; }

    public String getStrengthsJson() { return strengthsJson; }
    public void setStrengthsJson(String strengthsJson) { this.strengthsJson = strengthsJson; }

    public String getImprovementAreasJson() { return improvementAreasJson; }
    public void setImprovementAreasJson(String improvementAreasJson) { this.improvementAreasJson = improvementAreasJson; }

    public String getCustomTeacherFeedback() { return customTeacherFeedback; }
    public void setCustomTeacherFeedback(String customTeacherFeedback) { this.customTeacherFeedback = customTeacherFeedback; }

    public String getEvaluatedByModel() { return evaluatedByModel; }
    public void setEvaluatedByModel(String evaluatedByModel) { this.evaluatedByModel = evaluatedByModel; }

    public Boolean getIsTeacherOverridden() { return isTeacherOverridden; }
    public void setIsTeacherOverridden(Boolean teacherOverridden) { isTeacherOverridden = teacherOverridden; }

    public String getTeacherNotes() { return teacherNotes; }
    public void setTeacherNotes(String teacherNotes) { this.teacherNotes = teacherNotes; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
