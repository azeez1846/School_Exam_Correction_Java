package com.schoolexam.servlet;

import com.schoolexam.model.User;
import com.schoolexam.view.DashboardView;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolexam.service.AnalyticsService;

import java.util.Map;

@WebServlet(urlPatterns = {"/", "/dashboard", "/api/analytics/diagnostic", "/api/analytics/plagiarism"})
public class DashboardServlet extends HttpServlet {

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/api/analytics/diagnostic".equals(path)) {
            resp.setContentType("application/json");
            String examIdStr = req.getParameter("examId");
            Long examId = examIdStr != null ? Long.parseLong(examIdStr) : 1L;
            Map<String, Object> data = analyticsService.getDiagnosticAnalytics(examId);
            objectMapper.writeValue(resp.getOutputStream(), data);
            return;
        }

        if ("/api/analytics/plagiarism".equals(path)) {
            resp.setContentType("application/json");
            String examIdStr = req.getParameter("examId");
            Long examId = examIdStr != null ? Long.parseLong(examIdStr) : 1L;
            var data = analyticsService.getPlagiarismReport(examId);
            objectMapper.writeValue(resp.getOutputStream(), data);
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");
        HttpSession session = req.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;
        String html = DashboardView.render(user);
        resp.getWriter().write(html);
    }
}
