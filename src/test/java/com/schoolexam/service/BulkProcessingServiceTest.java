package com.schoolexam.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BulkProcessingServiceTest {

    @Test
    public void testRegexRollNumberExtraction() {
        BulkProcessingService service = new BulkProcessingService(null, null, null);

        String ocr1 = "Name: Alice Johnson\nRoll No: STU-2026-991\nSection A answers...";
        assertEquals("STU-2026-991", service.extractRollNumber(ocr1, "DEFAULT"));

        String ocr2 = "Student ID: REG-4402\nCalculations step 1...";
        assertEquals("REG-4402", service.extractRollNumber(ocr2, "DEFAULT"));
    }

    @Test
    public void testRegexNameExtraction() {
        BulkProcessingService service = new BulkProcessingService(null, null, null);

        String ocr = "Student Name: Marcus Vance\nRoll Number: 104\nSolution...";
        assertEquals("Marcus Vance", service.extractStudentName(ocr, "DEFAULT"));
    }
}
