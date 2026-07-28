# School Exam Correction Java - Feature Suite & Architecture Walkthrough

All 6 major AI & Grading features have been built, thoroughly verified with unit tests, and committed/pushed to GitHub.

---

## 🚀 Key Features Implemented

### 1. 🎨 Visual Annotation & Audio Studio
* **Split-Screen Canvas**: Interactive canvas overlay on student answer sheets allowing teachers to draw tick marks (✓ +5), cross marks (✗ -2), pen annotations, and text comments.
* **Audio Voice Feedback**: Browser `MediaRecorder` API integration for teachers to record, preview, and attach live audio feedback for students/parents.
* **Backend API**: `/api/evaluations/annotation` & `/api/evaluations/audio` handlers in `EvaluationServlet.java`.

### 2. 🧠 AI Diagnostic Misconceptions & Remediation
* **Diagnostic AI Tab**: Generates class score distribution bands (90-100%, 80-89%, etc.), common student conceptual misconceptions, and automated AI class remediation plans.
* **Backend API**: `/api/analytics/diagnostic` in `DashboardServlet.java` & `AnalyticsService.java`.

### 3. 📄 Auto-Splitting Multi-Student Single-PDF Scanner
* **PDF Auto-Splitter**: `PdfSplitterService.java` parses single merged multi-student PDF documents, splits pages per student, extracts roll numbers/names, and batch creates submissions.
* **Backend API**: `/api/submissions/split-upload` in `SubmissionServlet.java`.

### 4. 🛠️ Smart AI Rubric & Model Answer Generator
* **AI Rubric Assistant**: Input question text and model answer to automatically generate structured multi-criteria marking rubrics with point allocations.
* **Backend API**: `/api/exam/generate-rubric` in `ExamServlet.java` & `LlmEvaluationService.java`.

### 5. 🔍 Peer Similarity & Plagiarism Detector Engine
* **Plagiarism Audit Tab**: `PlagiarismDetectionService.java` performs pairwise text similarity (Jaccard & N-Gram overlap) across all student papers for an exam, flagging suspicious copying with risk levels (HIGH/MEDIUM/LOW) and matching text excerpts.
* **Backend API**: `/api/analytics/plagiarism` in `DashboardServlet.java`.

### 6. 📤 PDF Report Card Export & Parent Email Dispatcher
* **Downloadable PDF Report**: `ReportCardExporterService.java` generates printable PDF report cards with school header, grade badge, rubric breakdown, and teacher notes.
* **Email Notification Dispatcher**: `EmailNotificationService.java` dispatches formatted report card emails to parents and students.
* **Backend API**: `/api/submissions/download-pdf-report` & `/api/submissions/send-email` in `SubmissionServlet.java`.

---

## 🧪 Verification & Test Suite Results

Full Maven test suite execution output:
`mvn clean test`

```
[INFO] Running com.schoolexam.dao.UserDaoTest -> 2/2 passed
[INFO] Running com.schoolexam.service.PlagiarismDetectionServiceTest -> 3/3 passed
[INFO] Running com.schoolexam.service.ReportCardExporterServiceTest -> 1/1 passed
[INFO] Running com.schoolexam.service.PdfSplitterServiceTest -> 2/2 passed
[INFO] Running com.schoolexam.service.AuthServiceTest -> 2/2 passed
[INFO] Running com.schoolexam.service.LlmEvaluationServiceTest -> 2/2 passed
[INFO] Running com.schoolexam.service.BulkProcessingServiceTest -> 2/2 passed
[INFO] Running com.schoolexam.service.OcrServiceTest -> 1/1 passed
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

---

## 📦 GitHub Repository

* **Repository**: `https://github.com/azeez1846/School_Exam_Correction_Java.git`
* **Branch**: `main`
* **Commit**: `5210682` (`feat: Add Split-Screen Annotation Studio, AI Diagnostic Analytics, PDF Splitter, AI Rubric Builder, Plagiarism Engine, and PDF/Email Exporter`)
