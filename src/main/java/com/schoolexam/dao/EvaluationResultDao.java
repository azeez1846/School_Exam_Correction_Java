package com.schoolexam.dao;

import com.schoolexam.config.DatabaseConfig;
import com.schoolexam.model.EvaluationResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationResultDao {

    public EvaluationResult findBySubmissionId(Long submissionId) {
        String sql = "SELECT * FROM evaluation_results WHERE submission_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, submissionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapEvaluation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<EvaluationResult> findByExamId(Long examId) {
        List<EvaluationResult> list = new ArrayList<>();
        String sql = "SELECT e.* FROM evaluation_results e " +
                     "JOIN paper_submissions s ON e.submission_id = s.id " +
                     "WHERE s.exam_id = ? ORDER BY e.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapEvaluation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public EvaluationResult saveOrUpdate(EvaluationResult eval) {
        EvaluationResult existing = findBySubmissionId(eval.getSubmissionId());
        if (existing == null) {
            String sql = "INSERT INTO evaluation_results (submission_id, total_marks_obtained, max_marks, percentage_score, letter_grade, rubric_breakdown_json, strengths_json, improvement_areas_json, custom_teacher_feedback, evaluated_by_model, is_teacher_overridden, teacher_notes, annotations_json, audio_feedback_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, eval.getSubmissionId());
                ps.setDouble(2, eval.getTotalMarksObtained() != null ? eval.getTotalMarksObtained() : 0.0);
                ps.setDouble(3, eval.getMaxMarks() != null ? eval.getMaxMarks() : 100.0);
                ps.setDouble(4, eval.getPercentageScore() != null ? eval.getPercentageScore() : 0.0);
                ps.setString(5, eval.getLetterGrade() != null ? eval.getLetterGrade() : "F");
                ps.setString(6, eval.getRubricBreakdownJson());
                ps.setString(7, eval.getStrengthsJson());
                ps.setString(8, eval.getImprovementAreasJson());
                ps.setString(9, eval.getCustomTeacherFeedback());
                ps.setString(10, eval.getEvaluatedByModel());
                ps.setInt(11, Boolean.TRUE.equals(eval.getIsTeacherOverridden()) ? 1 : 0);
                ps.setString(12, eval.getTeacherNotes());
                ps.setString(13, eval.getAnnotationsJson());
                ps.setString(14, eval.getAudioFeedbackPath());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    eval.setId(rs.getLong(1));
                }
                return eval;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String sql = "UPDATE evaluation_results SET total_marks_obtained = ?, max_marks = ?, percentage_score = ?, letter_grade = ?, rubric_breakdown_json = ?, strengths_json = ?, improvement_areas_json = ?, custom_teacher_feedback = ?, evaluated_by_model = ?, is_teacher_overridden = ?, teacher_notes = ?, annotations_json = ?, audio_feedback_path = ?, evaluated_at = CURRENT_TIMESTAMP WHERE submission_id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, eval.getTotalMarksObtained() != null ? eval.getTotalMarksObtained() : 0.0);
                ps.setDouble(2, eval.getMaxMarks() != null ? eval.getMaxMarks() : 100.0);
                ps.setDouble(3, eval.getPercentageScore() != null ? eval.getPercentageScore() : 0.0);
                ps.setString(4, eval.getLetterGrade() != null ? eval.getLetterGrade() : "F");
                ps.setString(5, eval.getRubricBreakdownJson());
                ps.setString(6, eval.getStrengthsJson());
                ps.setString(7, eval.getImprovementAreasJson());
                ps.setString(8, eval.getCustomTeacherFeedback());
                ps.setString(9, eval.getEvaluatedByModel());
                ps.setInt(10, Boolean.TRUE.equals(eval.getIsTeacherOverridden()) ? 1 : 0);
                ps.setString(11, eval.getTeacherNotes());
                ps.setString(12, eval.getAnnotationsJson());
                ps.setString(13, eval.getAudioFeedbackPath());
                ps.setLong(14, eval.getSubmissionId());
                ps.executeUpdate();
                eval.setId(existing.getId());
                return eval;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private EvaluationResult mapEvaluation(ResultSet rs) throws SQLException {
        EvaluationResult eval = new EvaluationResult();
        eval.setId(rs.getLong("id"));
        eval.setSubmissionId(rs.getLong("submission_id"));
        eval.setTotalMarksObtained(rs.getDouble("total_marks_obtained"));
        eval.setMaxMarks(rs.getDouble("max_marks"));
        eval.setPercentageScore(rs.getDouble("percentage_score"));
        eval.setLetterGrade(rs.getString("letter_grade"));
        eval.setRubricBreakdownJson(rs.getString("rubric_breakdown_json"));
        eval.setStrengthsJson(rs.getString("strengths_json"));
        eval.setImprovementAreasJson(rs.getString("improvement_areas_json"));
        eval.setCustomTeacherFeedback(rs.getString("custom_teacher_feedback"));
        eval.setEvaluatedByModel(rs.getString("evaluated_by_model"));
        eval.setIsTeacherOverridden(rs.getInt("is_teacher_overridden") == 1);
        eval.setTeacherNotes(rs.getString("teacher_notes"));
        try { eval.setAnnotationsJson(rs.getString("annotations_json")); } catch (Exception ignored) {}
        try { eval.setAudioFeedbackPath(rs.getString("audio_feedback_path")); } catch (Exception ignored) {}
        return eval;
    }
}
