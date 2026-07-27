<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Official Student Evaluation Report Card</title>
    <style>
        body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #1e293b; background: #f8fafc; margin: 0; padding: 2rem; }
        .report-card { max-width: 800px; margin: 0 auto; background: #fff; border: 2px solid #e2e8f0; border-radius: 12px; padding: 2.5rem; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
        .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #6366f1; padding-bottom: 1.5rem; margin-bottom: 2rem; }
        .grade-badge { font-size: 2rem; font-weight: 800; padding: 0.5rem 1.5rem; border-radius: 8px; background: #e0e7ff; color: #4338ca; }
        .meta-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 2rem; background: #f1f5f9; padding: 1.25rem; border-radius: 8px; }
        .print-btn { background: #6366f1; color: #fff; border: none; padding: 0.75rem 1.5rem; border-radius: 6px; font-weight: 600; cursor: pointer; margin-top: 1.5rem; }
        @media print { .print-btn { display: none; } }
    </style>
</head>
<body>

    <div class="report-card">
        <div class="header">
            <div>
                <h1 style="margin:0; font-size:1.75rem; color:#1e1b4b;">GradePulse AI Report Card</h1>
                <p style="margin:0.25rem 0 0 0; color:#64748b;">Official Student Performance Evaluation</p>
            </div>
            <div class="grade-badge">
                ${evaluation.letterGrade}
            </div>
        </div>

        <div class="meta-grid">
            <div>
                <strong>Student Roll No:</strong> ${submission.studentRollNumber}<br>
                <strong>Student Name:</strong> ${submission.studentName}
            </div>
            <div>
                <strong>Marks Obtained:</strong> ${evaluation.totalMarksObtained} / ${evaluation.maxMarks}<br>
                <strong>Percentage Score:</strong> ${evaluation.percentageScore}%
            </div>
        </div>

        <h3>Evaluator & Teacher Notes</h3>
        <p style="background:#fafafa; border-left:4px solid #6366f1; padding:1rem; border-radius:4px; line-height:1.6;">
            ${evaluation.customTeacherFeedback}
        </p>

        <c:if test="${evaluation.isTeacherOverridden}">
            <div style="margin-top:1rem; padding:0.75rem; background:#fffbeb; border:1px solid #fef3c7; color:#b45309; border-radius:6px;">
                <strong>Teacher Override Note:</strong> ${evaluation.teacherNotes}
            </div>
        </c:if>

        <button class="print-btn" onclick="window.print()">Print Report Card</button>
    </div>

</body>
</html>
