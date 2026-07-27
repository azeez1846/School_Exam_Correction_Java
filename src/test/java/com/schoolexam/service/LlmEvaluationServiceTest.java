package com.schoolexam.service;

import com.schoolexam.config.DatabaseConfig;
import com.schoolexam.config.SchemaInitializer;
import com.schoolexam.model.EvaluationResult;
import com.schoolexam.model.Exam;
import com.schoolexam.model.PaperSubmission;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LlmEvaluationServiceTest {

    @BeforeAll
    public static void setup() {
        DatabaseConfig.setDbPath("target/test_school_exam.db");
        SchemaInitializer.initialize();
    }

    @Test
    public void testHeuristicEvaluationAndGradeCalculation() {
        LlmEvaluationService service = new LlmEvaluationService();

        Exam exam = new Exam(1L, "Science Assessment", "Physics", "10", 100, "[]", 1L, null);
        PaperSubmission sub = new PaperSubmission(1L, 1L, "STU-001", "John Doe", "paper.pdf", "/tmp/paper.pdf", "Energy is mass times velocity squared. Step 1: PE = mgh = 980 J", "PENDING", null);

        EvaluationResult result = service.runHeuristicEvaluation(sub, exam, "Local Rule Engine");
        assertNotNull(result);
        assertTrue(result.getTotalMarksObtained() > 50.0);
        assertNotNull(result.getLetterGrade());
        assertEquals("Local Rule Engine", result.getEvaluatedByModel());
    }

    @Test
    public void testGradeCalculation() {
        LlmEvaluationService service = new LlmEvaluationService();
        assertEquals("A+", service.calculateGrade(95.0));
        assertEquals("A", service.calculateGrade(85.0));
        assertEquals("B", service.calculateGrade(75.0));
        assertEquals("C", service.calculateGrade(65.0));
        assertEquals("F", service.calculateGrade(45.0));
    }
}
