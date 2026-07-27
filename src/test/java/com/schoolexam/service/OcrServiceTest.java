package com.schoolexam.service;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class OcrServiceTest {

    @Test
    public void testSimulatedOcrText() {
        OcrService ocrService = new OcrService();
        String text = ocrService.extractTextFromFile(new File("sample_paper.png"));
        assertNotNull(text);
        assertTrue(text.contains("STUDENT ANSWER SHEET"));
        assertTrue(text.contains("Kinetic energy"));
    }
}
