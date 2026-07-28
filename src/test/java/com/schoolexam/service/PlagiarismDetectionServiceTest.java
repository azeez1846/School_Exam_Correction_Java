package com.schoolexam.service;

import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.PaperSubmission;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlagiarismDetectionServiceTest {

    @Test
    public void testJaccardSimilarityIdenticalText() {
        PlagiarismDetectionService service = new PlagiarismDetectionService();
        String text1 = "Kinetic energy is mass times velocity squared divided by two in physics formula.";
        String text2 = "Kinetic energy is mass times velocity squared divided by two in physics formula.";

        double score = service.calculateJaccardSimilarity(text1, text2);
        assertEquals(100.0, score, 0.01);
    }

    @Test
    public void testJaccardSimilarityDifferentText() {
        PlagiarismDetectionService service = new PlagiarismDetectionService();
        String text1 = "Photosynthesis process uses solar radiation energy to split water molecules.";
        String text2 = "Quadratic equation factorization yields real roots using discriminant formula.";

        double score = service.calculateJaccardSimilarity(text1, text2);
        assertTrue(score < 20.0);
    }

    @Test
    public void testAnalyzeExamPlagiarismWithStubDao() {
        PaperSubmission sub1 = new PaperSubmission();
        sub1.setId(1L);
        sub1.setStudentName("Alice Smith");
        sub1.setStudentRollNumber("STU-101");
        sub1.setOcrTextContent("Kinetic energy equals half mass times velocity squared derivation.");

        PaperSubmission sub2 = new PaperSubmission();
        sub2.setId(2L);
        sub2.setStudentName("Bob Johnson");
        sub2.setStudentRollNumber("STU-102");
        sub2.setOcrTextContent("Kinetic energy equals half mass times velocity squared derivation.");

        PaperSubmissionDao stubDao = new PaperSubmissionDao() {
            @Override
            public List<PaperSubmission> findByExamId(Long examId) {
                return List.of(sub1, sub2);
            }
        };

        PlagiarismDetectionService service = new PlagiarismDetectionService(stubDao);
        List<PlagiarismDetectionService.SimilarityPair> flagged = service.analyzeExamPlagiarism(1L);

        assertNotNull(flagged);
        assertEquals(1, flagged.size());
        assertEquals(100.0, flagged.get(0).getSimilarityPercentage(), 0.01);
        assertEquals("HIGH", flagged.get(0).getRiskLevel());
    }
}
