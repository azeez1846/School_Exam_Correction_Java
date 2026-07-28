<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>GradePulse AI - Teacher Exam Correction Hub</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/styles.css">
</head>
<body>

    <!-- Header Navbar -->
    <header class="glass-panel navbar">
        <a href="#" class="brand">
            <div class="brand-logo">
                <i class="fa-solid fa-graduation-cap"></i>
            </div>
            <div class="brand-text">
                <h1>GradePulse AI</h1>
                <span>Exam Correction Platform</span>
            </div>
        </a>

        <div class="nav-actions">
            <div class="user-badge">
                <div class="avatar">TJ</div>
                <div>
                    <strong style="display:block; font-size:0.88rem; color:#fff;">Prof. Sarah Jenkins</strong>
                    <span style="font-size:0.75rem; color:var(--text-muted);">Lead Evaluator</span>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-sm btn-secondary">
                <i class="fa-solid fa-arrow-right-from-bracket"></i> Sign Out
            </a>
        </div>
    </header>

    <!-- Main Container -->
    <div class="container">
        <div class="grid-main">

            <!-- Sidebar Controls -->
            <aside class="sidebar">
                <!-- Exam Selector Card -->
                <div class="glass-panel control-card">
                    <h3><i class="fa-solid fa-book-bookmark" style="color:var(--primary-indigo);"></i> Selected Exam</h3>
                    <div class="form-group">
                        <label>Active Assessment</label>
                        <select id="examSelect" class="form-select"></select>
                    </div>
                    <button class="btn btn-sm btn-outline" style="width:100%; margin-bottom:0.5rem;" onclick="exportCsv()">
                        <i class="fa-solid fa-file-csv"></i> Download CSV Gradebook
                    </button>
                    <button class="btn btn-sm btn-secondary" style="width:100%; color:#a855f7;" onclick="openRubricModal()">
                        <i class="fa-solid fa-wand-magic-sparkles"></i> AI Rubric Assistant
                    </button>
                </div>

                <!-- LLM Provider Dropdown Card -->
                <div class="glass-panel control-card">
                    <h3><i class="fa-solid fa-robot" style="color:#34d399;"></i> LLM Evaluation Model</h3>
                    <div class="form-group">
                        <label>Active Free-Tier Model</label>
                        <select id="llmProviderSelect" class="form-select"></select>
                    </div>
                    <button class="btn btn-sm btn-secondary" style="width:100%;" onclick="openKeyModal()">
                        <i class="fa-solid fa-key"></i> Configure API Key
                    </button>
                </div>
            </aside>

            <!-- Main Content Panel -->
            <main>
                <div class="glass-panel" style="padding:1.75rem;">
                    
                    <div class="tabs-header">
                        <button class="tab-btn active" onclick="switchTab('single')"><i class="fa-solid fa-file-arrow-up"></i> Single Paper</button>
                        <button class="tab-btn" onclick="switchTab('split')"><i class="fa-solid fa-scissors"></i> Auto-Split PDF</button>
                        <button class="tab-btn" onclick="switchTab('bulk')"><i class="fa-solid fa-file-zipper"></i> Bulk ZIP</button>
                        <button class="tab-btn" onclick="switchTab('diagnostic')"><i class="fa-solid fa-chart-pie"></i> Diagnostic AI</button>
                        <button class="tab-btn" onclick="switchTab('plagiarism')"><i class="fa-solid fa-shield-halved"></i> Plagiarism Audit</button>
                        <button class="tab-btn" onclick="switchTab('history')"><i class="fa-solid fa-list-check"></i> Gradebook</button>
                    </div>

                    <!-- Single Upload View -->
                    <div id="tabSingle" class="tab-content">
                        <form id="singleUploadForm">
                            <div class="dropzone" onclick="document.getElementById('paperFileInput').click()">
                                <div class="dropzone-icon">
                                    <i class="fa-solid fa-cloud-arrow-up"></i>
                                </div>
                                <h4>Drop Student Answer Sheet (PDF or Image)</h4>
                                <p>Supports scanned PDFs, PNG, JPG, WEBP. OCR Vision parsing enabled.</p>
                                <input type="file" id="paperFileInput" name="paperFile" accept=".pdf,.png,.jpg,.jpeg,.webp" style="display:none;" onchange="updateFileName(this)">
                                <div id="selectedFileName" style="margin-top:0.75rem; font-weight:600; color:var(--primary-indigo);"></div>
                            </div>

                            <div style="display:grid; grid-template-columns: 1fr 1fr; gap:1rem; margin-top:1.25rem;">
                                <div class="form-group">
                                    <label>Student Roll Number (Optional)</label>
                                    <input type="text" name="studentRollNumber" class="form-input" placeholder="Auto-detected if left empty">
                                </div>
                                <div class="form-group">
                                    <label>Student Name (Optional)</label>
                                    <input type="text" name="studentName" class="form-input" placeholder="Auto-detected if left empty">
                                </div>
                            </div>

                            <button type="submit" class="btn btn-primary" style="width:100%; margin-top:0.75rem;">
                                <i class="fa-solid fa-wand-magic-sparkles"></i> Process OCR & Evaluate Paper
                            </button>
                        </form>

                        <!-- Scorecard Display -->
                        <div id="scorecardDisplay" class="glass-panel" style="margin-top:2rem; overflow:hidden;">
                            <div style="padding:3rem; text-align:center; color:var(--text-muted);">
                                <i class="fa-solid fa-clipboard-check" style="font-size:3rem; color:var(--border-glass); margin-bottom:1rem; display:block;"></i>
                                Upload an answer sheet paper to view instant AI grading and feedback.
                            </div>
                        </div>
                    </div>

                    <!-- Multi-Student Single PDF Auto-Splitter View -->
                    <div id="tabSplit" class="tab-content" style="display:none;">
                        <form id="splitUploadForm">
                            <div class="dropzone" onclick="document.getElementById('splitFileInput').click()">
                                <div class="dropzone-icon">
                                    <i class="fa-solid fa-scissors" style="color:#6366f1;"></i>
                                </div>
                                <h4>Upload Multi-Student Merged PDF Document</h4>
                                <p>Auto-detects page boundaries, splits pages per student, extracts Roll Nos, and creates submissions.</p>
                                <input type="file" id="splitFileInput" name="multiStudentPdf" accept=".pdf" style="display:none;" onchange="updateSplitFileName(this)">
                                <div id="selectedSplitFileName" style="margin-top:0.75rem; font-weight:600; color:#6366f1;"></div>
                            </div>

                            <button type="submit" class="btn btn-primary" style="width:100%; margin-top:1.5rem;">
                                <i class="fa-solid fa-scissors"></i> Split PDF & Batch Process All Students
                            </button>
                        </form>
                    </div>

                    <!-- Bulk Upload View -->
                    <div id="tabBulk" class="tab-content" style="display:none;">
                        <form id="bulkUploadForm">
                            <div class="dropzone" onclick="document.getElementById('bulkFileInput').click()">
                                <div class="dropzone-icon">
                                    <i class="fa-solid fa-file-zipper"></i>
                                </div>
                                <h4>Drop Bulk ZIP Archive of Student Papers</h4>
                                <p>Upload a .zip folder containing multiple PDF/image answer papers.</p>
                                <input type="file" id="bulkFileInput" name="bulkFile" accept=".zip" style="display:none;" onchange="updateBulkFileName(this)">
                                <div id="selectedBulkFileName" style="margin-top:0.75rem; font-weight:600; color:#34d399;"></div>
                            </div>

                            <button type="submit" class="btn btn-primary" style="width:100%; margin-top:1.5rem;">
                                <i class="fa-solid fa-layer-group"></i> Run Batch Bulk Correction
                            </button>
                        </form>
                    </div>

                    <!-- Diagnostic AI Insights View -->
                    <div id="tabDiagnostic" class="tab-content" style="display:none;">
                        <h3 style="font-family:var(--font-heading); margin-bottom:1rem; color:#fff;"><i class="fa-solid fa-lightbulb" style="color:#fbbf24;"></i> AI Diagnostic Misconceptions & Remediation</h3>
                        <div id="diagnosticContent"></div>
                    </div>

                    <!-- Plagiarism Audit View -->
                    <div id="tabPlagiarism" class="tab-content" style="display:none;">
                        <h3 style="font-family:var(--font-heading); margin-bottom:1rem; color:#fff;"><i class="fa-solid fa-shield-cat" style="color:#f43f5e;"></i> Peer Similarity & Copying Audit</h3>
                        <div id="plagiarismContent"></div>
                    </div>

                    <!-- History View -->
                    <div id="tabHistory" class="tab-content" style="display:none;">
                        <h3 style="font-family:var(--font-heading); margin-bottom:1rem; color:#fff;">Evaluated Submissions Gradebook</h3>
                        <div id="submissionsList"></div>
                    </div>

                </div>
            </main>
        </div>
    </div>

    <!-- Canvas Annotation & Voice Feedback Studio Modal -->
    <div id="annotationModal" class="modal-overlay">
        <div class="glass-panel modal-content" style="max-width:900px; width:90%;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
                <h3 style="font-family:var(--font-heading); color:#fff;"><i class="fa-solid fa-paintbrush" style="color:#6366f1;"></i> Split-Screen Paper Annotation & Audio Studio</h3>
                <button class="btn btn-sm btn-secondary" onclick="closeAnnotationModal()"><i class="fa-solid fa-xmark"></i></button>
            </div>
            
            <div style="display:grid; grid-template-columns:1fr 1fr; gap:1rem;">
                <!-- Left: Canvas overlay paper preview -->
                <div style="background:#0f172a; padding:0.5rem; border-radius:var(--radius-md); text-align:center;">
                    <div style="margin-bottom:0.5rem; display:flex; gap:0.5rem; justify-content:center;">
                        <button class="btn btn-sm btn-secondary" onclick="setDrawTool('tick')">✓ Tick</button>
                        <button class="btn btn-sm btn-secondary" onclick="setDrawTool('cross')">✗ Cross</button>
                        <button class="btn btn-sm btn-secondary" onclick="setDrawTool('pen')">✏ Pen</button>
                        <button class="btn btn-sm btn-secondary" onclick="clearCanvas()">Clear Canvas</button>
                    </div>
                    <canvas id="annotationCanvas" width="380" height="460" style="border:1px dashed #475569; background:#ffffff; border-radius:4px; cursor:crosshair;"></canvas>
                </div>

                <!-- Right: Voice note recorder & saved feedback -->
                <div style="display:flex; flex-direction:column; justify-content:space-between;">
                    <div>
                        <h4 style="color:#e2e8f0; margin-bottom:0.75rem;"><i class="fa-solid fa-microphone" style="color:#34d399;"></i> Teacher Voice Note Feedback</h4>
                        <p style="font-size:0.85rem; color:#94a3b8; margin-bottom:1rem;">Record voice audio notes to send directly to students & parents.</p>
                        <div style="display:flex; gap:0.75rem; margin-bottom:1rem;">
                            <button id="recordAudioBtn" class="btn btn-sm btn-primary" onclick="toggleAudioRecording()"><i class="fa-solid fa-circle"></i> Start Recording</button>
                            <span id="recordingStatus" style="font-size:0.85rem; color:#fbbf24; align-self:center;"></span>
                        </div>
                        <audio id="audioPreviewPlayer" controls style="width:100%; display:none; margin-bottom:1rem;"></audio>
                    </div>

                    <div style="display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1rem;">
                        <button class="btn btn-secondary" onclick="closeAnnotationModal()">Cancel</button>
                        <button class="btn btn-primary" onclick="saveAnnotationsAndAudio()"><i class="fa-solid fa-floppy-disk"></i> Save Annotations & Audio</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- AI Rubric Assistant Modal -->
    <div id="rubricModal" class="modal-overlay">
        <div class="glass-panel modal-content" style="max-width:600px; width:90%;">
            <h3 style="font-family:var(--font-heading); margin-bottom:1rem; color:#fff;"><i class="fa-solid fa-wand-magic-sparkles" style="color:#a855f7;"></i> AI Rubric Assistant</h3>
            <div class="form-group">
                <label>Question Text</label>
                <textarea id="rubricQuestionInput" class="form-input" rows="2" placeholder="e.g. Derive kinetic energy formula and explain units..."></textarea>
            </div>
            <div class="form-group">
                <label>Model Answer / Key</label>
                <textarea id="rubricModelAnswerInput" class="form-input" rows="3" placeholder="e.g. KE = 0.5 * m * v^2..."></textarea>
            </div>
            <button class="btn btn-sm btn-primary" onclick="generateAiRubric()" style="width:100%; margin-bottom:1rem;"><i class="fa-solid fa-robot"></i> Generate Rubric JSON</button>
            <div class="form-group">
                <label>Generated Rubric JSON</label>
                <textarea id="rubricResultJson" class="form-input" rows="4" readonly></textarea>
            </div>
            <div style="display:flex; justify-content:flex-end; gap:1rem; margin-top:1rem;">
                <button class="btn btn-secondary" onclick="closeRubricModal()">Close</button>
            </div>
        </div>
    </div>

    <!-- Email Modal -->
    <div id="emailModal" class="modal-overlay">
        <div class="glass-panel modal-content">
            <h3 style="font-family:var(--font-heading); margin-bottom:1rem; color:#fff;"><i class="fa-solid fa-envelope" style="color:#38bdf8;"></i> Send Report Card to Parent/Student</h3>
            <input type="hidden" id="emailSubId">
            <div class="form-group">
                <label>Recipient Email Address</label>
                <input type="email" id="recipientEmailInput" class="form-input" placeholder="parent@school.edu">
            </div>
            <div style="display:flex; justify-content:flex-end; gap:1rem; margin-top:1.5rem;">
                <button class="btn btn-secondary" onclick="closeEmailModal()">Cancel</button>
                <button class="btn btn-primary" onclick="dispatchReportEmail()">Send Email</button>
            </div>
        </div>
    </div>

    <!-- Manual Score Override Modal -->
    <div id="overrideModal" class="modal-overlay">
        <div class="glass-panel modal-content">
            <h3 style="font-family:var(--font-heading); margin-bottom:1rem; color:#fff;">Override Marks & Evaluation</h3>
            <input type="hidden" id="overrideSubId">
            <div class="form-group">
                <label>New Total Marks Obtained</label>
                <input type="number" step="0.5" id="overrideScoreInput" class="form-input">
            </div>
            <div class="form-group">
                <label>Teacher Override Justification Notes</label>
                <textarea id="overrideNotesInput" class="form-input" rows="3" placeholder="Explain score adjustment justification..."></textarea>
            </div>
            <div style="display:flex; justify-content:flex-end; gap:1rem; margin-top:1.5rem;">
                <button class="btn btn-secondary" onclick="closeOverrideModal()">Cancel</button>
                <button class="btn btn-primary" onclick="submitOverride()">Save Override</button>
            </div>
        </div>
    </div>

    <!-- Configure API Key Modal -->
    <div id="keyModal" class="modal-overlay">
        <div class="glass-panel modal-content">
            <h3 style="font-family:var(--font-heading); margin-bottom:1rem; color:#fff;">Configure Model API Key</h3>
            <div class="form-group">
                <label>API Key for selected model</label>
                <input type="password" id="modalApiKeyInput" class="form-input" placeholder="Paste your API key here...">
            </div>
            <div style="display:flex; justify-content:flex-end; gap:1rem; margin-top:1.5rem;">
                <button class="btn btn-secondary" onclick="closeKeyModal()">Cancel</button>
                <button class="btn btn-primary" onclick="saveApiKey()">Save API Key</button>
            </div>
        </div>
    </div>

    <!-- Loading Spinner -->
    <div id="loadingOverlay" class="modal-overlay">
        <div style="text-align:center;">
            <div class="spinner"></div>
            <p style="margin-top:1rem; font-weight:600; color:#fff;">Running OCR & LLM Paper Correction...</p>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/static/js/app.js"></script>
    <script>
        function switchTab(tab) {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.style.display = 'none');
            
            if (tab === 'single') {
                document.getElementById('tabSingle').style.display = 'block';
            } else if (tab === 'split') {
                document.getElementById('tabSplit').style.display = 'block';
            } else if (tab === 'bulk') {
                document.getElementById('tabBulk').style.display = 'block';
            } else if (tab === 'diagnostic') {
                document.getElementById('tabDiagnostic').style.display = 'block';
                loadDiagnosticInsights();
            } else if (tab === 'plagiarism') {
                document.getElementById('tabPlagiarism').style.display = 'block';
                loadPlagiarismAudit();
            } else if (tab === 'history') {
                document.getElementById('tabHistory').style.display = 'block';
            }
            if (event && event.target) {
                event.target.classList.add('active');
            }
        }

        function updateFileName(input) {
            if (input.files.length > 0) {
                document.getElementById('selectedFileName').textContent = "Selected: " + input.files[0].name;
            }
        }

        function updateSplitFileName(input) {
            if (input.files.length > 0) {
                document.getElementById('selectedSplitFileName').textContent = "Selected: " + input.files[0].name;
            }
        }

        function updateBulkFileName(input) {
            if (input.files.length > 0) {
                document.getElementById('selectedBulkFileName').textContent = "Selected: " + input.files[0].name;
            }
        }
    </script>
</body>
</html>
