package com.schoolexam.config;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SchemaInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT DEFAULT 'TEACHER', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // 2. Exams table
            stmt.execute("CREATE TABLE IF NOT EXISTS exams (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "subject TEXT NOT NULL, " +
                    "grade_level TEXT NOT NULL, " +
                    "total_max_marks INTEGER DEFAULT 100, " +
                    "marking_rubric_json TEXT, " +
                    "teacher_id INTEGER, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // 3. Paper Submissions table
            stmt.execute("CREATE TABLE IF NOT EXISTS paper_submissions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "exam_id INTEGER NOT NULL, " +
                    "student_roll_number TEXT, " +
                    "student_name TEXT, " +
                    "original_file_name TEXT, " +
                    "stored_file_path TEXT, " +
                    "ocr_text_content TEXT, " +
                    "status TEXT DEFAULT 'PENDING', " +
                    "uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(exam_id) REFERENCES exams(id) ON DELETE CASCADE)");

            // 4. Evaluation Results table
            stmt.execute("CREATE TABLE IF NOT EXISTS evaluation_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "submission_id INTEGER UNIQUE NOT NULL, " +
                    "total_marks_obtained REAL DEFAULT 0.0, " +
                    "max_marks REAL DEFAULT 100.0, " +
                    "percentage_score REAL DEFAULT 0.0, " +
                    "letter_grade TEXT DEFAULT 'F', " +
                    "rubric_breakdown_json TEXT, " +
                    "strengths_json TEXT, " +
                    "improvement_areas_json TEXT, " +
                    "custom_teacher_feedback TEXT, " +
                    "evaluated_by_model TEXT, " +
                    "is_teacher_overridden INTEGER DEFAULT 0, " +
                    "teacher_notes TEXT, " +
                    "evaluated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(submission_id) REFERENCES paper_submissions(id) ON DELETE CASCADE)");

            // 5. LLM Configs table
            stmt.execute("CREATE TABLE IF NOT EXISTS llm_configs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "provider_key TEXT UNIQUE NOT NULL, " +
                    "provider_name TEXT NOT NULL, " +
                    "api_key TEXT, " +
                    "api_endpoint TEXT, " +
                    "is_default INTEGER DEFAULT 0, " +
                    "is_active INTEGER DEFAULT 1, " +
                    "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // Seed default teacher if not existing
            try (PreparedStatement checkUser = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?")) {
                checkUser.setString(1, "teacher@school.edu");
                ResultSet rs = checkUser.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    String hashed = BCrypt.hashpw("teacher123", BCrypt.gensalt());
                    try (PreparedStatement insertUser = conn.prepareStatement(
                            "INSERT INTO users (email, password_hash, full_name, role) VALUES (?, ?, ?, ?)")) {
                        insertUser.setString(1, "teacher@school.edu");
                        insertUser.setString(2, hashed);
                        insertUser.setString(3, "Prof. Sarah Jenkins");
                        insertUser.setString(4, "TEACHER");
                        insertUser.executeUpdate();
                    }
                }
            }

            // Seed default sample exam if not existing
            try (Statement checkExam = conn.createStatement();
                 ResultSet rs = checkExam.executeQuery("SELECT COUNT(*) FROM exams")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String defaultRubric = "[{" +
                            "\"criterion\":\"Conceptual Understanding & Definitions\"," +
                            "\"maxMarks\":40," +
                            "\"description\":\"Accurate definitions of key terms, core principles, and domain concepts.\"" +
                            "},{" +
                            "\"criterion\":\"Problem Solving & Step-by-Step Logic\"," +
                            "\"maxMarks\":40," +
                            "\"description\":\"Logical derivation, intermediate calculation steps, and correct reasoning.\"" +
                            "},{" +
                            "\"criterion\":\"Diagrams, Layout & Presentation\"," +
                            "\"maxMarks\":20," +
                            "\"description\":\"Clarity of diagrams, neatness of solution structure, and legibility.\"" +
                            "}]";

                    try (PreparedStatement insertExam = conn.prepareStatement(
                            "INSERT INTO exams (title, subject, grade_level, total_max_marks, marking_rubric_json, teacher_id) VALUES (?, ?, ?, ?, ?, ?)")) {
                        insertExam.setString(1, "Midterm Science & Mathematics Assessment");
                        insertExam.setString(2, "Integrated Sciences");
                        insertExam.setString(3, "Grade 10");
                        insertExam.setInt(4, 100);
                        insertExam.setString(5, defaultRubric);
                        insertExam.setLong(6, 1L);
                        insertExam.executeUpdate();
                    }
                }
            }

            // Seed LLM providers
            seedLlmConfig(conn, "gemini-1.5-flash", "Google Gemini 1.5 Flash (Free Tier)", "", "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent", 1);
            seedLlmConfig(conn, "gemini-2.0-flash", "Google Gemini 2.0 Flash (Experimental Free)", "", "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent", 0);
            seedLlmConfig(conn, "groq-llama3", "Groq Llama 3 8B (Free Tier API)", "", "https://api.groq.com/openai/v1/chat/completions", 0);
            seedLlmConfig(conn, "huggingface-mistral", "HuggingFace Mistral 7B (Free Tier)", "", "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2", 0);
            seedLlmConfig(conn, "local-rule-engine", "Local Intelligent Heuristic Rule Engine (Offline Fallback)", "NONE", "LOCAL", 0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void seedLlmConfig(Connection conn, String key, String name, String apiKey, String endpoint, int isDefault) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM llm_configs WHERE provider_key = ?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO llm_configs (provider_key, provider_name, api_key, api_endpoint, is_default, is_active) VALUES (?, ?, ?, ?, ?, 1)")) {
                    insert.setString(1, key);
                    insert.setString(2, name);
                    insert.setString(3, apiKey);
                    insert.setString(4, endpoint);
                    insert.setInt(5, isDefault);
                    insert.executeUpdate();
                }
            }
        }
    }
}
