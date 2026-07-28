package com.schoolexam.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.EvaluationResult;
import com.schoolexam.model.PaperSubmission;
import com.schoolexam.service.BulkProcessingService;
import com.schoolexam.service.LlmEvaluationService;
import com.schoolexam.service.OcrService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.schoolexam.service.PdfSplitterService;
import com.schoolexam.service.ReportCardExporterService;
import com.schoolexam.service.EmailNotificationService;

@WebServlet(urlPatterns = {"/api/submissions", "/api/submissions/upload", "/api/submissions/bulk", "/api/submissions/split-upload", "/api/submissions/download-pdf-report", "/api/submissions/send-email"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 50, maxRequestSize = 1024 * 1024 * 100)
public class SubmissionServlet extends HttpServlet {

    private final PaperSubmissionDao submissionDao = new PaperSubmissionDao();
    private final OcrService ocrService = new OcrService();
    private final LlmEvaluationService llmEvaluationService = new LlmEvaluationService();
    private final BulkProcessingService bulkProcessingService = new BulkProcessingService();
    private final PdfSplitterService pdfSplitterService = new PdfSplitterService();
    private final ReportCardExporterService reportCardExporterService = new ReportCardExporterService();
    private final EmailNotificationService emailNotificationService = new EmailNotificationService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/api/submissions/download-pdf-report".equals(path)) {
            String subIdStr = req.getParameter("submissionId");
            if (subIdStr != null) {
                try {
                    Long submissionId = Long.parseLong(subIdStr);
                    byte[] pdfBytes = reportCardExporterService.generatePdfReportCard(submissionId);
                    resp.setContentType("application/pdf");
                    resp.setHeader("Content-Disposition", "attachment; filename=\"report_card_sub_" + submissionId + ".pdf\"");
                    resp.setContentLength(pdfBytes.length);
                    resp.getOutputStream().write(pdfBytes);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        resp.setContentType("application/json");
        String examIdStr = req.getParameter("examId");

        if (examIdStr != null && !examIdStr.isEmpty()) {
            Long examId = Long.parseLong(examIdStr);
            List<PaperSubmission> list = submissionDao.findByExamId(examId);
            objectMapper.writeValue(resp.getOutputStream(), list);
        } else {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"examId parameter required\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String path = req.getServletPath();

        String uploadDir = getServletContext().getRealPath("/uploads");
        if (uploadDir == null) {
            uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        }
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) uploadDirFile.mkdirs();

        if ("/api/submissions/send-email".equals(path)) {
            Map<String, Object> body = objectMapper.readValue(req.getInputStream(), Map.class);
            Long submissionId = Long.parseLong(body.get("submissionId").toString());
            String email = body.get("email") != null ? body.get("email").toString() : null;

            boolean success = emailNotificationService.sendReportCardEmail(submissionId, email);
            Map<String, Object> res = Map.of("success", success, "message", "Email notification queued & sent successfully.");
            objectMapper.writeValue(resp.getOutputStream(), res);
            return;
        }

        if ("/api/submissions/split-upload".equals(path)) {
            String examIdStr = req.getParameter("examId");
            Part pdfPart = req.getPart("multiStudentPdf");

            if (pdfPart == null || examIdStr == null) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"Multi-student PDF file and examId are required\"}");
                return;
            }

            Long examId = Long.parseLong(examIdStr);
            File multiPdfFile = new File(uploadDirFile, "multi_student_" + System.currentTimeMillis() + ".pdf");
            try (InputStream input = pdfPart.getInputStream()) {
                Files.copy(input, multiPdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            List<PaperSubmission> splits = pdfSplitterService.splitAndProcessMultiStudentPdf(multiPdfFile, examId, uploadDirFile.getAbsolutePath(), true);
            objectMapper.writeValue(resp.getOutputStream(), splits);
            return;
        }

        if ("/api/submissions/upload".equals(path)) {
            // Single paper upload
            String examIdStr = req.getParameter("examId");
            String rollNumber = req.getParameter("studentRollNumber");
            String studentName = req.getParameter("studentName");
            String providerKey = req.getParameter("providerKey");

            Part filePart = req.getPart("paperFile");
            if (filePart == null || examIdStr == null) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"File and examId are required\"}");
                return;
            }

            Long examId = Long.parseLong(examIdStr);
            String fileName = getFileName(filePart);
            File destFile = new File(uploadDirFile, System.currentTimeMillis() + "_" + fileName);

            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            String ocrText = ocrService.extractTextFromFile(destFile);
            if (rollNumber == null || rollNumber.trim().isEmpty()) {
                rollNumber = bulkProcessingService.extractRollNumber(ocrText, "STU-" + System.currentTimeMillis() % 10000);
            }
            if (studentName == null || studentName.trim().isEmpty()) {
                studentName = bulkProcessingService.extractStudentName(ocrText, "Student " + (System.currentTimeMillis() % 100));
            }

            PaperSubmission sub = new PaperSubmission();
            sub.setExamId(examId);
            sub.setStudentRollNumber(rollNumber);
            sub.setStudentName(studentName);
            sub.setOriginalFileName(fileName);
            sub.setStoredFilePath(destFile.getAbsolutePath());
            sub.setOcrTextContent(ocrText);
            sub.setStatus("PENDING");

            PaperSubmission created = submissionDao.create(sub);
            EvaluationResult eval = llmEvaluationService.evaluateSubmission(created.getId(), providerKey);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("submission", created);
            responseMap.put("evaluation", eval);

            objectMapper.writeValue(resp.getOutputStream(), responseMap);

        } else if ("/api/submissions/bulk".equals(path)) {
            // Bulk zip upload
            String examIdStr = req.getParameter("examId");
            String providerKey = req.getParameter("providerKey");
            Part zipPart = req.getPart("bulkFile");

            if (zipPart == null || examIdStr == null) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"Bulk Zip file and examId are required\"}");
                return;
            }

            Long examId = Long.parseLong(examIdStr);
            File zipFile = new File(uploadDirFile, "bulk_zip_" + System.currentTimeMillis() + ".zip");
            try (InputStream input = zipPart.getInputStream()) {
                Files.copy(input, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            List<EvaluationResult> evals = bulkProcessingService.processBulkZip(examId, zipFile, uploadDirFile.getAbsolutePath(), providerKey);
            objectMapper.writeValue(resp.getOutputStream(), evals);
        }
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String tokens : contentDisp.split(";")) {
            if (tokens.trim().startsWith("filename")) {
                return tokens.substring(tokens.indexOf("=") + 2, tokens.length() - 1);
            }
        }
        return "submission.pdf";
    }
}
