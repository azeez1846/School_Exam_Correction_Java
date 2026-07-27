package com.schoolexam.dao;

import com.schoolexam.config.DatabaseConfig;
import com.schoolexam.model.PaperSubmission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaperSubmissionDao {

    public List<PaperSubmission> findByExamId(Long examId) {
        List<PaperSubmission> list = new ArrayList<>();
        String sql = "SELECT * FROM paper_submissions WHERE exam_id = ? ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapSubmission(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public PaperSubmission findById(Long id) {
        String sql = "SELECT * FROM paper_submissions WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapSubmission(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PaperSubmission create(PaperSubmission sub) {
        String sql = "INSERT INTO paper_submissions (exam_id, student_roll_number, student_name, original_file_name, stored_file_path, ocr_text_content, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, sub.getExamId());
            ps.setString(2, sub.getStudentRollNumber());
            ps.setString(3, sub.getStudentName());
            ps.setString(4, sub.getOriginalFileName());
            ps.setString(5, sub.getStoredFilePath());
            ps.setString(6, sub.getOcrTextContent());
            ps.setString(7, sub.getStatus() != null ? sub.getStatus() : "PENDING");
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                sub.setId(rs.getLong(1));
            }
            return sub;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatusAndOcr(Long id, String status, String ocrText) {
        String sql = "UPDATE paper_submissions SET status = ?, ocr_text_content = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, ocrText);
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PaperSubmission mapSubmission(ResultSet rs) throws SQLException {
        PaperSubmission sub = new PaperSubmission();
        sub.setId(rs.getLong("id"));
        sub.setExamId(rs.getLong("exam_id"));
        sub.setStudentRollNumber(rs.getString("student_roll_number"));
        sub.setStudentName(rs.getString("student_name"));
        sub.setOriginalFileName(rs.getString("original_file_name"));
        sub.setStoredFilePath(rs.getString("stored_file_path"));
        sub.setOcrTextContent(rs.getString("ocr_text_content"));
        sub.setStatus(rs.getString("status"));
        return sub;
    }
}
