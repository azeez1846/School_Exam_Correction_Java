package com.schoolexam.service;

import com.schoolexam.dao.EvaluationResultDao;
import com.schoolexam.dao.ExamDao;
import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.EvaluationResult;
import com.schoolexam.model.Exam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsService {
    private final EvaluationResultDao evaluationResultDao;
    private final PaperSubmissionDao paperSubmissionDao;
    private final ExamDao examDao;

    public AnalyticsService() {
        this.evaluationResultDao = new EvaluationResultDao();
        this.paperSubmissionDao = new PaperSubmissionDao();
        this.examDao = new ExamDao();
    }

    public AnalyticsService(EvaluationResultDao evaluationResultDao, PaperSubmissionDao paperSubmissionDao, ExamDao examDao) {
        this.evaluationResultDao = evaluationResultDao;
        this.paperSubmissionDao = paperSubmissionDao;
        this.examDao = examDao;
    }

    public Map<String, Object> getExamAnalytics(Long examId) {
        Map<String, Object> stats = new HashMap<>();
        List<EvaluationResult> evals = evaluationResultDao.findByExamId(examId);
        Exam exam = examDao.findById(examId);

        if (evals.isEmpty()) {
            stats.put("totalEvaluated", 0);
            stats.put("classAverage", 0.0);
            stats.put("highestScore", 0.0);
            stats.put("lowestScore", 0.0);
            stats.put("passPercentage", 0.0);
            return stats;
        }

        double sum = 0.0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        int passed = 0;

        Map<String, Integer> gradeCounts = new HashMap<>();

        for (EvaluationResult e : evals) {
            double score = e.getTotalMarksObtained();
            double pct = e.getPercentageScore();
            sum += score;
            if (score > max) max = score;
            if (score < min) min = score;
            if (pct >= 50.0) passed++;

            String grade = e.getLetterGrade() != null ? e.getLetterGrade() : "F";
            gradeCounts.put(grade, gradeCounts.getOrDefault(grade, 0) + 1);
        }

        stats.put("totalEvaluated", evals.size());
        stats.put("classAverage", Math.round((sum / evals.size()) * 10.0) / 10.0);
        stats.put("highestScore", max);
        stats.put("lowestScore", min);
        stats.put("passPercentage", Math.round(((double) passed / evals.size() * 100.0) * 10.0) / 10.0);
        stats.put("gradeCounts", gradeCounts);
        stats.put("examTitle", exam != null ? exam.getTitle() : "Exam");

        return stats;
    }
}
