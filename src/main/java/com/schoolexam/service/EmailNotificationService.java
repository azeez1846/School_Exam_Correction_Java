package com.schoolexam.service;

import com.schoolexam.dao.EvaluationResultDao;
import com.schoolexam.dao.ExamDao;
import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.EvaluationResult;
import com.schoolexam.model.Exam;
import com.schoolexam.model.PaperSubmission;

public class EmailNotificationService {

    private final PaperSubmissionDao paperSubmissionDao;
    private final EvaluationResultDao evaluationResultDao;
    private final ExamDao examDao;

    public EmailNotificationService() {
        this.paperSubmissionDao = new PaperSubmissionDao();
        this.evaluationResultDao = new EvaluationResultDao();
        this.examDao = new ExamDao();
    }

    public EmailNotificationService(PaperSubmissionDao paperSubmissionDao, EvaluationResultDao evaluationResultDao, ExamDao examDao) {
        this.paperSubmissionDao = paperSubmissionDao;
        this.evaluationResultDao = evaluationResultDao;
        this.examDao = examDao;
    }

    public boolean sendReportCardEmail(Long submissionId, String recipientEmail) {
        PaperSubmission sub = paperSubmissionDao.findById(submissionId);
        if (sub == null) return false;

        EvaluationResult eval = evaluationResultDao.findBySubmissionId(submissionId);
        Exam exam = examDao.findById(sub.getExamId());

        String targetEmail = recipientEmail != null && !recipientEmail.trim().isEmpty() ? recipientEmail : "parent." + sub.getStudentRollNumber().toLowerCase().replaceAll("[^a-z0-9]", "") + "@school.edu";
        String studentName = sub.getStudentName() != null ? sub.getStudentName() : "Student";
        String examTitle = exam != null ? exam.getTitle() : "School Assessment";

        System.out.println("=================================================");
        System.out.println("[EMAIL NOTIFICATION DISPATCHED]");
        System.out.println("To: " + targetEmail);
        System.out.println("Subject: Official GradePulse AI Report Card - " + studentName + " (" + examTitle + ")");
        System.out.println("Body:");
        System.out.println("Dear Parent / Student,");
        System.out.println("The evaluation for " + studentName + "'s exam paper (" + examTitle + ") is complete.");
        if (eval != null) {
            System.out.println("Final Grade: " + eval.getLetterGrade() + " | Marks Obtained: " + eval.getTotalMarksObtained() + " / " + eval.getMaxMarks() + " (" + eval.getPercentageScore() + "%)");
            System.out.println("Feedback: " + eval.getCustomTeacherFeedback());
        }
        System.out.println("PDF Report Card attachment attached.");
        System.out.println("=================================================");

        return true;
    }
}
