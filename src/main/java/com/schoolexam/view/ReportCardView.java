package com.schoolexam.view;

import com.schoolexam.model.EvaluationResult;
import com.schoolexam.model.PaperSubmission;

public class ReportCardView {

    public static String render(PaperSubmission sub, EvaluationResult eval) {
        String roll = sub != null && sub.getStudentRollNumber() != null ? sub.getStudentRollNumber() : "N/A";
        String name = sub != null && sub.getStudentName() != null ? sub.getStudentName() : "Student";
        String grade = eval != null && eval.getLetterGrade() != null ? eval.getLetterGrade() : "N/A";
        double obtained = eval != null && eval.getTotalMarksObtained() != null ? eval.getTotalMarksObtained() : 0.0;
        double max = eval != null && eval.getMaxMarks() != null ? eval.getMaxMarks() : 100.0;
        double pct = eval != null && eval.getPercentageScore() != null ? eval.getPercentageScore() : 0.0;
        String feedback = eval != null && eval.getCustomTeacherFeedback() != null ? eval.getCustomTeacherFeedback() : "Good effort.";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("  <meta charset=\"UTF-8\">\n");
        sb.append("  <title>Official Student Evaluation Report Card (Pure Java UI)</title>\n");
        sb.append("  <style>\n");
        sb.append("    body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #1e293b; background: #f8fafc; margin: 0; padding: 2rem; }\n");
        sb.append("    .report-card { max-width: 800px; margin: 0 auto; background: #fff; border: 2px solid #e2e8f0; border-radius: 12px; padding: 2.5rem; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }\n");
        sb.append("    .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #6366f1; padding-bottom: 1.5rem; margin-bottom: 2rem; }\n");
        sb.append("    .grade-badge { font-size: 2rem; font-weight: 800; padding: 0.5rem 1.5rem; border-radius: 8px; background: #e0e7ff; color: #4338ca; }\n");
        sb.append("    .meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 2rem; background: #f1f5f9; padding: 1.25rem; border-radius: 8px; }\n");
        sb.append("    .print-btn { background: #6366f1; color: #fff; border: none; padding: 0.75rem 1.5rem; border-radius: 6px; font-weight: 600; cursor: pointer; margin-top: 1.5rem; }\n");
        sb.append("    @media print { .print-btn { display: none; } }\n");
        sb.append("  </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("  <div class=\"report-card\">\n");
        sb.append("    <div class=\"header\">\n");
        sb.append("      <div>\n");
        sb.append("        <h1 style=\"margin:0; font-size:1.75rem; color:#1e1b4b;\">GradePulse AI Report Card</h1>\n");
        sb.append("        <p style=\"margin:0.25rem 0 0 0; color:#64748b;\">Official Student Performance Evaluation</p>\n");
        sb.append("      </div>\n");
        sb.append("      <div class=\"grade-badge\">").append(grade).append("</div>\n");
        sb.append("    </div>\n");
        sb.append("    <div class=\"meta-grid\">\n");
        sb.append("      <div>\n");
        sb.append("        <strong>Student Roll No:</strong> ").append(roll).append("<br>\n");
        sb.append("        <strong>Student Name:</strong> ").append(name).append("\n");
        sb.append("      </div>\n");
        sb.append("      <div>\n");
        sb.append("        <strong>Marks Obtained:</strong> ").append(obtained).append(" / ").append(max).append("<br>\n");
        sb.append("        <strong>Percentage Score:</strong> ").append(pct).append("%\n");
        sb.append("      </div>\n");
        sb.append("    </div>\n");
        sb.append("    <h3>Evaluator & Teacher Notes</h3>\n");
        sb.append("    <p style=\"background:#fafafa; border-left:4px solid #6366f1; padding:1rem; border-radius:4px; line-height:1.6;\">\n");
        sb.append("      ").append(feedback).append("\n");
        sb.append("    </p>\n");
        if (eval != null && Boolean.TRUE.equals(eval.getIsTeacherOverridden())) {
            sb.append("    <div style=\"margin-top:1rem; padding:0.75rem; background:#fffbeb; border:1px solid #fef3c7; color:#b45309; border-radius:6px;\">\n");
            sb.append("      <strong>Teacher Override Note:</strong> ").append(eval.getTeacherNotes() != null ? eval.getTeacherNotes() : "").append("\n");
            sb.append("    </div>\n");
        }
        sb.append("    <button class=\"print-btn\" onclick=\"window.print()\">Print Report Card</button>\n");
        sb.append("  </div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        return sb.toString();
    }
}
