package com.schoolexam.dao;

import com.schoolexam.config.DatabaseConfig;
import com.schoolexam.model.Exam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDao {

    public List<Exam> findAll() {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT * FROM exams ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                exams.add(mapExam(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exams;
    }

    public Exam findById(Long id) {
        String sql = "SELECT * FROM exams WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapExam(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Exam create(Exam exam) {
        String sql = "INSERT INTO exams (title, subject, grade_level, total_max_marks, marking_rubric_json, teacher_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, exam.getTitle());
            ps.setString(2, exam.getSubject());
            ps.setString(3, exam.getGradeLevel());
            ps.setInt(4, exam.getTotalMaxMarks() != null ? exam.getTotalMaxMarks() : 100);
            ps.setString(5, exam.getMarkingRubricJson());
            ps.setLong(6, exam.getTeacherId() != null ? exam.getTeacherId() : 1L);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                exam.setId(rs.getLong(1));
            }
            return exam;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Exam mapExam(ResultSet rs) throws SQLException {
        Exam exam = new Exam();
        exam.setId(rs.getLong("id"));
        exam.setTitle(rs.getString("title"));
        exam.setSubject(rs.getString("subject"));
        exam.setGradeLevel(rs.getString("grade_level"));
        exam.setTotalMaxMarks(rs.getInt("total_max_marks"));
        exam.setMarkingRubricJson(rs.getString("marking_rubric_json"));
        exam.setTeacherId(rs.getLong("teacher_id"));
        return exam;
    }
}
