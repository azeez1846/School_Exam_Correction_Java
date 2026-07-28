package com.schoolexam.service;

import com.schoolexam.dao.EvaluationResultDao;
import com.schoolexam.dao.ExamDao;
import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.EvaluationResult;
import com.schoolexam.model.Exam;
import com.schoolexam.model.PaperSubmission;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ReportCardExporterService {

    private final EvaluationResultDao evaluationResultDao;
    private final PaperSubmissionDao paperSubmissionDao;
    private final ExamDao examDao;

    public ReportCardExporterService() {
        this.evaluationResultDao = new EvaluationResultDao();
        this.paperSubmissionDao = new PaperSubmissionDao();
        this.examDao = new ExamDao();
    }

    public ReportCardExporterService(EvaluationResultDao evaluationResultDao, PaperSubmissionDao paperSubmissionDao, ExamDao examDao) {
        this.evaluationResultDao = evaluationResultDao;
        this.paperSubmissionDao = paperSubmissionDao;
        this.examDao = examDao;
    }

    public byte[] generatePdfReportCard(Long submissionId) throws Exception {
        EvaluationResult eval = evaluationResultDao.findBySubmissionId(submissionId);
        if (eval == null) return new byte[0];

        PaperSubmission sub = paperSubmissionDao.findById(submissionId);
        Exam exam = sub != null ? examDao.findById(sub.getExamId()) : null;

        String studentName = sub != null && sub.getStudentName() != null ? sub.getStudentName() : "Student";
        String rollNumber = sub != null && sub.getStudentRollNumber() != null ? sub.getStudentRollNumber() : "N/A";
        String examTitle = exam != null ? exam.getTitle() : "Exam Assessment";
        String subject = exam != null ? exam.getSubject() + " (Grade " + exam.getGradeLevel() + ")" : "General Subject";

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Header Banner
                cs.setNonStrokingColor(30, 41, 59); // dark slate blue
                cs.addRect(0, 720, 612, 72);
                cs.fill();

                cs.setNonStrokingColor(255, 255, 255);
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 22);
                cs.newLineAtOffset(30, 745);
                cs.showText("GradePulse AI - Official Performance Report");
                cs.endText();

                // Student Metadata Card
                cs.setNonStrokingColor(15, 23, 42);
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(30, 680);
                cs.showText("Student Name: " + studentName);
                cs.newLineAtOffset(0, -20);
                cs.showText("Roll Number: " + rollNumber);
                cs.newLineAtOffset(0, -20);
                cs.showText("Assessment: " + examTitle);
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(0, -18);
                cs.showText("Subject: " + subject);
                cs.endText();

                // Grade Badge Box
                cs.setNonStrokingColor(99, 102, 241); // indigo
                cs.addRect(420, 630, 160, 65);
                cs.fill();

                cs.setNonStrokingColor(255, 255, 255);
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.newLineAtOffset(435, 672);
                cs.showText("FINAL GRADE: " + eval.getLetterGrade());
                cs.newLineAtOffset(0, -18);
                cs.showText("Score: " + eval.getTotalMarksObtained() + " / " + eval.getMaxMarks());
                cs.newLineAtOffset(0, -16);
                cs.showText("Percentage: " + eval.getPercentageScore() + "%");
                cs.endText();

                // Horizontal Divider
                cs.setStrokingColor(203, 213, 225);
                cs.setLineWidth(1);
                cs.moveTo(30, 605);
                cs.lineTo(582, 605);
                cs.stroke();

                // Feedback & Remarks Section
                cs.setNonStrokingColor(15, 23, 42);
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(30, 575);
                cs.showText("Evaluator Feedback & AI Remarks:");
                cs.endText();

                String feedback = eval.getCustomTeacherFeedback() != null ? eval.getCustomTeacherFeedback() : "Good performance.";
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 11);
                cs.newLineAtOffset(30, 550);
                cs.showText(sanitizeText(feedback));
                cs.endText();

                if (eval.getTeacherNotes() != null && !eval.getTeacherNotes().isEmpty()) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    cs.newLineAtOffset(30, 520);
                    cs.showText("Teacher Overridden Notes: " + sanitizeText(eval.getTeacherNotes()));
                    cs.endText();
                }

                // Footer
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
                cs.setNonStrokingColor(100, 116, 139);
                cs.newLineAtOffset(30, 40);
                cs.showText("Generated automatically by GradePulse AI System - Evaluated model: " + eval.getEvaluatedByModel());
                cs.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private String sanitizeText(String input) {
        if (input == null) return "";
        return input.replaceAll("[^\\x00-\\x7F]", "");
    }
}
