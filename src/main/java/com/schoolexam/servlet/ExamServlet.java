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

@WebServlet(urlPatterns = {"/api/exams", "/api/exams/*"})
public class ExamServlet extends HttpServlet {
    private final ExamDao examDao = new ExamDao();
    private final AnalyticsService analyticsService = new AnalyticsService();
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
