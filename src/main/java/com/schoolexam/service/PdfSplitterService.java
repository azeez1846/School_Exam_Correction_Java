package com.schoolexam.service;

import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.PaperSubmission;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfSplitterService {

    private final PaperSubmissionDao paperSubmissionDao;
    private final OcrService ocrService;
    private final LlmEvaluationService llmEvaluationService;

    public PdfSplitterService() {
        this.paperSubmissionDao = new PaperSubmissionDao();
        this.ocrService = new OcrService();
        this.llmEvaluationService = new LlmEvaluationService();
    }

    public PdfSplitterService(PaperSubmissionDao paperSubmissionDao, OcrService ocrService, LlmEvaluationService llmEvaluationService) {
        this.paperSubmissionDao = paperSubmissionDao;
        this.ocrService = ocrService;
        this.llmEvaluationService = llmEvaluationService;
    }

    public List<PaperSubmission> splitAndProcessMultiStudentPdf(File inputPdfFile, Long examId, String uploadDirPath, boolean autoEvaluate) {
        List<PaperSubmission> results = new ArrayList<>();
        if (inputPdfFile == null || !inputPdfFile.exists() || examId == null) {
            return results;
        }

        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        try (PDDocument document = PDDocument.load(inputPdfFile)) {
            int totalPages = document.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();

            // Group pages into student documents. Default 2 pages per student unless header marker detected.
            int pagesPerStudent = 2;
            int studentCount = 1;

            for (int i = 0; i < totalPages; i += pagesPerStudent) {
                int endPage = Math.min(i + pagesPerStudent - 1, totalPages - 1);

                try (PDDocument subDoc = new PDDocument()) {
                    for (int pageIdx = i; pageIdx <= endPage; pageIdx++) {
                        subDoc.addPage(document.getPage(pageIdx));
                    }

                    String fileName = "split_std" + studentCount + "_" + System.currentTimeMillis() + ".pdf";
                    File destFile = new File(uploadDir, fileName);
                    subDoc.save(destFile);

                    // Extract text for student details
                    stripper.setStartPage(i + 1);
                    stripper.setEndPage(endPage + 1);
                    String pageText = stripper.getText(document);

                    String rollNumber = extractPattern(pageText, "(?i)(?:roll|id|student\\s*no)\\s*[:#-]?\\s*([A-Za-z0-9-]+)");
                    if (rollNumber == null || rollNumber.isEmpty()) {
                        rollNumber = "STD-" + String.format("%03d", studentCount);
                    }

                    String studentName = extractPattern(pageText, "(?i)(?:name|student)\\s*[:#-]?\\s*([A-Za-z\\s]{3,30})");
                    if (studentName == null || studentName.isEmpty()) {
                        studentName = "Student " + studentCount;
                    } else {
                        studentName = studentName.trim();
                    }

                    // Perform OCR / Text Extraction
                    String ocrText = ocrService.extractTextFromFile(destFile);

                    PaperSubmission sub = new PaperSubmission();
                    sub.setExamId(examId);
                    sub.setStudentRollNumber(rollNumber);
                    sub.setStudentName(studentName);
                    sub.setOriginalFileName(inputPdfFile.getName() + " [Split Part " + studentCount + "]");
                    sub.setStoredFilePath(destFile.getAbsolutePath());
                    sub.setOcrTextContent(ocrText);
                    sub.setStatus("SUBMITTED");

                    sub = paperSubmissionDao.create(sub);

                    if (autoEvaluate && sub != null && sub.getId() != null) {
                        llmEvaluationService.evaluateSubmission(sub.getId(), null);
                    }

                    results.add(sub);
                    studentCount++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private String extractPattern(String text, String regex) {
        if (text == null) return null;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
