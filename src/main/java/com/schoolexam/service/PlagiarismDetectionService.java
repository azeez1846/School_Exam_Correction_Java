package com.schoolexam.service;

import com.schoolexam.dao.PaperSubmissionDao;
import com.schoolexam.model.PaperSubmission;

import java.util.*;

public class PlagiarismDetectionService {

    private final PaperSubmissionDao paperSubmissionDao;

    public PlagiarismDetectionService() {
        this.paperSubmissionDao = new PaperSubmissionDao();
    }

    public PlagiarismDetectionService(PaperSubmissionDao paperSubmissionDao) {
        this.paperSubmissionDao = paperSubmissionDao;
    }

    public static class SimilarityPair {
        private Long submissionId1;
        private String student1Name;
        private String student1Roll;
        private Long submissionId2;
        private String student2Name;
        private String student2Roll;
        private double similarityPercentage;
        private String matchingExcerpt;
        private String riskLevel;

        public SimilarityPair() {}

        public SimilarityPair(Long submissionId1, String student1Name, String student1Roll, Long submissionId2, String student2Name, String student2Roll, double similarityPercentage, String matchingExcerpt, String riskLevel) {
            this.submissionId1 = submissionId1;
            this.student1Name = student1Name;
            this.student1Roll = student1Roll;
            this.submissionId2 = submissionId2;
            this.student2Name = student2Name;
            this.student2Roll = student2Roll;
            this.similarityPercentage = similarityPercentage;
            this.matchingExcerpt = matchingExcerpt;
            this.riskLevel = riskLevel;
        }

        public Long getSubmissionId1() { return submissionId1; }
        public String getStudent1Name() { return student1Name; }
        public String getStudent1Roll() { return student1Roll; }
        public Long getSubmissionId2() { return submissionId2; }
        public String getStudent2Name() { return student2Name; }
        public String getStudent2Roll() { return student2Roll; }
        public double getSimilarityPercentage() { return similarityPercentage; }
        public String getMatchingExcerpt() { return matchingExcerpt; }
        public String getRiskLevel() { return riskLevel; }
    }

    public List<SimilarityPair> analyzeExamPlagiarism(Long examId) {
        List<SimilarityPair> flaggedPairs = new ArrayList<>();
        List<PaperSubmission> submissions = paperSubmissionDao.findByExamId(examId);

        if (submissions == null || submissions.size() < 2) {
            return flaggedPairs;
        }

        for (int i = 0; i < submissions.size(); i++) {
            for (int j = i + 1; j < submissions.size(); j++) {
                PaperSubmission sub1 = submissions.get(i);
                PaperSubmission sub2 = submissions.get(j);

                String text1 = sub1.getOcrTextContent() != null ? sub1.getOcrTextContent() : "";
                String text2 = sub2.getOcrTextContent() != null ? sub2.getOcrTextContent() : "";

                double similarity = calculateJaccardSimilarity(text1, text2);
                if (similarity >= 35.0) { // Flag pairs with >35% text overlap
                    String excerpt = findLongestCommonSubstring(text1, text2);
                    String riskLevel = similarity >= 75.0 ? "HIGH" : (similarity >= 50.0 ? "MEDIUM" : "LOW");

                    flaggedPairs.add(new SimilarityPair(
                            sub1.getId(), sub1.getStudentName(), sub1.getStudentRollNumber(),
                            sub2.getId(), sub2.getStudentName(), sub2.getStudentRollNumber(),
                            Math.round(similarity * 10.0) / 10.0,
                            excerpt,
                            riskLevel
                    ));
                }
            }
        }

        flaggedPairs.sort((a, b) -> Double.compare(b.getSimilarityPercentage(), a.getSimilarityPercentage()));
        return flaggedPairs;
    }

    public double calculateJaccardSimilarity(String t1, String t2) {
        Set<String> set1 = tokenize(t1);
        Set<String> set2 = tokenize(t2);

        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return ((double) intersection.size() / union.size()) * 100.0;
    }

    private Set<String> tokenize(String text) {
        Set<String> words = new HashSet<>();
        if (text == null) return words;
        String[] tokens = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
        for (String t : tokens) {
            if (t.length() > 2) {
                words.add(t);
            }
        }
        return words;
    }

    private String findLongestCommonSubstring(String s1, String s2) {
        if (s1 == null || s2 == null) return "No matching text";
        String[] words1 = s1.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
        String[] words2 = s2.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");

        int maxLen = 0;
        int endIdx = 0;
        int[][] table = new int[words1.length + 1][words2.length + 1];

        for (int i = 1; i <= words1.length; i++) {
            for (int j = 1; j <= words2.length; j++) {
                if (words1[i - 1].equalsIgnoreCase(words2[j - 1])) {
                    table[i][j] = table[i - 1][j - 1] + 1;
                    if (table[i][j] > maxLen) {
                        maxLen = table[i][j];
                        endIdx = i;
                    }
                }
            }
        }

        if (maxLen < 3) return "General conceptual similarity";

        StringBuilder sb = new StringBuilder();
        for (int i = endIdx - maxLen; i < endIdx; i++) {
            sb.append(words1[i]).append(" ");
        }
        return sb.toString().trim();
    }
}
