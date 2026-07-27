package com.schoolexam.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolexam.dao.EvaluationResultDao;
import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.EvaluationResult;
import com.schoolexam.model.PaperSubmission;
import com.schoolexam.service.LlmEvaluationService;
import com.schoolexam.view.ReportCardView;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/evaluations", "/api/evaluations/*", "/api/evaluations/export/csv/*", "/report-card/*"})
public class EvaluationServlet extends HttpServlet {

    private final EvaluationResultDao evaluationResultDao = new EvaluationResultDao();
    private final PaperSubmissionDao paperSubmissionDao = new PaperSubmissionDao();
    private final LlmEvaluationService llmEvaluationService = new LlmEvaluationService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URI = req.getRequestURI();

        if (URI.contains("/export/csv/")) {
            String[] parts = URI.split("/");
            Long examId = Long.parseLong(parts[parts.length - 1]);
            String csv = llmEvaluationService.generateGradebookCsv(examId);

            resp.setContentType("text/csv");
            resp.setHeader("Content-Disposition", "attachment; filename=\"exam_gradebook_" + examId + ".csv\"");
            resp.getWriter().write(csv);
            return;
        }

        if (URI.startsWith(req.getContextPath() + "/report-card/") || URI.contains("/report-card/")) {
            String[] parts = URI.split("/");
            Long submissionId = Long.parseLong(parts[parts.length - 1]);
            PaperSubmission sub = paperSubmissionDao.findById(submissionId);
            EvaluationResult eval = evaluationResultDao.findBySubmissionId(submissionId);

            resp.setContentType("text/html;charset=UTF-8");
            String html = ReportCardView.render(sub, eval);
            resp.getWriter().write(html);
            return;
        }

        resp.setContentType("application/json");
        String examIdStr = req.getParameter("examId");
        String subIdStr = req.getParameter("submissionId");

        if (subIdStr != null) {
            Long subId = Long.parseLong(subIdStr);
            EvaluationResult eval = evaluationResultDao.findBySubmissionId(subId);
            objectMapper.writeValue(resp.getOutputStream(), eval);
        } else if (examIdStr != null) {
            Long examId = Long.parseLong(examIdStr);
            List<EvaluationResult> list = evaluationResultDao.findByExamId(examId);
            objectMapper.writeValue(resp.getOutputStream(), list);
        } else {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Parameter submissionId or examId required\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String path = req.getServletPath();

        if ("/api/evaluations/override".equals(path) || req.getRequestURI().contains("/override")) {
            Map<String, Object> body = objectMapper.readValue(req.getInputStream(), Map.class);
            Long submissionId = Long.parseLong(body.get("submissionId").toString());
            double newScore = Double.parseDouble(body.get("totalMarksObtained").toString());
            String notes = body.get("teacherNotes") != null ? body.get("teacherNotes").toString() : "";
            String breakdownJson = body.get("rubricBreakdownJson") != null ? body.get("rubricBreakdownJson").toString() : null;

            EvaluationResult updated = llmEvaluationService.overrideTeacherMarks(submissionId, newScore, notes, breakdownJson);
            objectMapper.writeValue(resp.getOutputStream(), updated);
        }
    }
}
