package com.schoolexam.service;

import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.PaperSubmission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PdfSplitterServiceTest {

    @Test
    public void testPdfSplitterServiceInitialization() {
        PdfSplitterService service = new PdfSplitterService();
        assertNotNull(service);
    }

    @Test
    public void testSplitWithNullFile(@TempDir Path tempDir) {
        PdfSplitterService service = new PdfSplitterService();
        List<PaperSubmission> results = service.splitAndProcessMultiStudentPdf(null, 1L, tempDir.toString(), false);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
