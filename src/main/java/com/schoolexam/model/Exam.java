package com.schoolexam.model;

import java.time.LocalDateTime;

public class Exam {
    private Long id;
    private String title;
    private String subject;
    private String gradeLevel;
    private Integer totalMaxMarks;
    private String markingRubricJson;
    private Long teacherId;
    private LocalDateTime createdAt;

    public Exam() {}

    public Exam(Long id, String title, String subject, String gradeLevel, Integer totalMaxMarks, String markingRubricJson, Long teacherId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.gradeLevel = gradeLevel;
        this.totalMaxMarks = totalMaxMarks;
        this.markingRubricJson = markingRubricJson;
        this.teacherId = teacherId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }

    public Integer getTotalMaxMarks() { return totalMaxMarks; }
    public void setTotalMaxMarks(Integer totalMaxMarks) { this.totalMaxMarks = totalMaxMarks; }

    public String getMarkingRubricJson() { return markingRubricJson; }
    public void setMarkingRubricJson(String markingRubricJson) { this.markingRubricJson = markingRubricJson; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
