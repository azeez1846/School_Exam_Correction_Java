package com.schoolexam.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolexam.dao.ExamDao;
import com.schoolexam.model.Exam;
import com.schoolexam.model.User;
import com.schoolexam.service.AnalyticsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.schoolexam.service.LlmEvaluationService;

@WebServlet(urlPatterns = {"/api/exams", "/api/exams/*", "/api/exam/generate-rubric"})
public class ExamServlet extends HttpServlet {
    private final ExamDao examDao = new ExamDao();
    private final AnalyticsService analyticsService = new AnalyticsService();
    private final LlmEvaluationService llmEvaluationService = new LlmEvaluationService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String path = req.getPathInfo();

        if (path == null || "/".equals(path)) {
            List<Exam> exams = examDao.findAll();
            objectMapper.writeValue(resp.getOutputStream(), exams);
        } else if (path.endsWith("/analytics")) {
            String[] parts = path.split("/");
            Long examId = Long.parseLong(parts[1]);
            Map<String, Object> stats = analyticsService.getExamAnalytics(examId);
            objectMapper.writeValue(resp.getOutputStream(), stats);
        } else {
            Long id = Long.parseLong(path.substring(1));
            Exam exam = examDao.findById(id);
            if (exam != null) {
                objectMapper.writeValue(resp.getOutputStream(), exam);
            } else {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\":\"Exam not found\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String URI = req.getRequestURI();

        if (URI.contains("/generate-rubric")) {
            Map<String, Object> body = objectMapper.readValue(req.getInputStream(), Map.class);
            String title = body.get("title") != null ? body.get("title").toString() : "Assessment";
            String subject = body.get("subject") != null ? body.get("subject").toString() : "Science";
            String question = body.get("questionText") != null ? body.get("questionText").toString() : "";
            String modelAnswer = body.get("modelAnswer") != null ? body.get("modelAnswer").toString() : "";

            String rubricJson = llmEvaluationService.generateRubricFromModelAnswer(title, subject, question, modelAnswer);
            Map<String, String> res = Map.of("markingRubricJson", rubricJson);
            objectMapper.writeValue(resp.getOutputStream(), res);
            return;
        }

        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        Exam exam = objectMapper.readValue(req.getInputStream(), Exam.class);
        if (user != null) {
            exam.setTeacherId(user.getId());
        }
        Exam created = examDao.create(exam);
        objectMapper.writeValue(resp.getOutputStream(), created);
    }
}
