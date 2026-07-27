package com.schoolexam.service;

import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.EvaluationResult;
import com.schoolexam.model.PaperSubmission;
import org.apache.commons.fileupload.FileItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BulkProcessingService {
    private final PaperSubmissionDao submissionDao;
    private final OcrService ocrService;
    private final LlmEvaluationService llmEvaluationService;

    private static final Pattern ROLL_NO_PATTERN = Pattern.compile("(?i)(?:roll\\s*(?:no|number)?|student\\s*id|id|reg)\\s*[:#-]?\\s*([A-Z0-9-]+)");
    private static final Pattern NAME_PATTERN = Pattern.compile("(?i)(?:student\\s*name|name)\\s*[:#-]?\\s*([A-Za-z\\s]{3,25})");

    public BulkProcessingService() {
        this.submissionDao = new PaperSubmissionDao();
        this.ocrService = new OcrService();
        this.llmEvaluationService = new LlmEvaluationService();
    }

    public BulkProcessingService(PaperSubmissionDao submissionDao, OcrService ocrService, LlmEvaluationService llmEvaluationService) {
        this.submissionDao = submissionDao;
        this.ocrService = ocrService;
        this.llmEvaluationService = llmEvaluationService;
    }

    public List<EvaluationResult> processBulkZip(Long examId, File zipFile, String uploadDir, String providerKey) {
        List<EvaluationResult> results = new ArrayList<>();
        if (zipFile == null || !zipFile.exists()) return results;

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            int counter = 1;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && !entry.getName().startsWith("__MACOSX")) {
                    String fileName = new File(entry.getName()).getName();
                    File destFile = new File(uploadDir, "bulk_" + System.currentTimeMillis() + "_" + fileName);
                    try (OutputStream os = new FileOutputStream(destFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                    }

                    // Extract OCR text
                    String ocrText = ocrService.extractTextFromFile(destFile);

                    // Detect Roll & Name
                    String roll = extractRollNumber(ocrText, "STU-2026-" + String.format("%03d", counter));
                    String name = extractStudentName(ocrText, "Student " + counter);

                    PaperSubmission sub = new PaperSubmission();
                    sub.setExamId(examId);
                    sub.setStudentRollNumber(roll);
                    sub.setStudentName(name);
                    sub.setOriginalFileName(fileName);
                    sub.setStoredFilePath(destFile.getAbsolutePath());
                    sub.setOcrTextContent(ocrText);
                    sub.setStatus("PENDING");

                    PaperSubmission created = submissionDao.create(sub);
                    if (created != null) {
                        EvaluationResult eval = llmEvaluationService.evaluateSubmission(created.getId(), providerKey);
                        if (eval != null) results.add(eval);
                    }
                    counter++;
                }
                zis.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public String extractRollNumber(String text, String defaultRoll) {
        if (text != null) {
            Matcher m = ROLL_NO_PATTERN.matcher(text);
            if (m.find()) {
                String matched = m.group(1).split("\r?\n")[0].trim();
                if (!matched.isEmpty()) return matched;
            }
        }
        return defaultRoll;
    }

    public String extractStudentName(String text, String defaultName) {
        if (text != null) {
            Matcher m = NAME_PATTERN.matcher(text);
            if (m.find()) {
                String matched = m.group(1).split("\r?\n")[0].trim();
                if (!matched.isEmpty()) return matched;
            }
        }
        return defaultName;
    }
}
