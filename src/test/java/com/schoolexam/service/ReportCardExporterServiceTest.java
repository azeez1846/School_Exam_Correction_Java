package com.schoolexam.service;

import com.schoolexam.dao.EvaluationResultDao;
import com.schoolexam.dao.ExamDao;
import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.EvaluationResult;
import com.schoolexam.model.Exam;
import com.schoolexam.model.PaperSubmission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReportCardExporterServiceTest {

    @Test
    public void testGeneratePdfReportCard() throws Exception {
        PaperSubmission sub = new PaperSubmission();
        sub.setId(1L);
        sub.setExamId(10L);
        sub.setStudentName("Jane Doe");
        sub.setStudentRollNumber("STD-005");

        Exam exam = new Exam();
        exam.setId(10L);
        exam.setTitle("Physics Final Exam");
        exam.setSubject("Physics");
        exam.setGradeLevel("Grade 10");

        EvaluationResult eval = new EvaluationResult();
        eval.setSubmissionId(1L);
        eval.setTotalMarksObtained(88.0);
        eval.setMaxMarks(100.0);
        eval.setPercentageScore(88.0);
        eval.setLetterGrade("A");
        eval.setCustomTeacherFeedback("Great analytical performance!");

        EvaluationResultDao evalDao = new EvaluationResultDao() {
            @Override
            public EvaluationResult findBySubmissionId(Long submissionId) {
                return eval;
            }
        };

        PaperSubmissionDao subDao = new PaperSubmissionDao() {
            @Override
            public PaperSubmission findById(Long id) {
                return sub;
            }
        };

        ExamDao examDao = new ExamDao() {
            @Override
            public Exam findById(Long id) {
                return exam;
            }
        };

        ReportCardExporterService service = new ReportCardExporterService(evalDao, subDao, examDao);
        byte[] pdfBytes = service.generatePdfReportCard(1L);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
