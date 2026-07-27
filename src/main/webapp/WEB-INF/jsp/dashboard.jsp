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
                    <button class="btn btn-sm btn-outline" style="width:100%;" onclick="exportCsv()">
                        <i class="fa-solid fa-file-csv"></i> Download CSV Gradebook
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
                        <button class="tab-btn active" onclick="switchTab('single')"><i class="fa-solid fa-file-arrow-up"></i> Single Paper Correction</button>
                        <button class="tab-btn" onclick="switchTab('bulk')"><i class="fa-solid fa-folder-tree"></i> Bulk Upload Scanner</button>
                        <button class="tab-btn" onclick="switchTab('history')"><i class="fa-solid fa-list-check"></i> Submissions Gradebook</button>
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

                    <!-- History View -->
                    <div id="tabHistory" class="tab-content" style="display:none;">
                        <h3 style="font-family:var(--font-heading); margin-bottom:1rem;">Evaluated Submissions</h3>
                        <div id="submissionsList"></div>
                    </div>

                </div>
            </main>
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
                event.target.classList.add('active');
            } else if (tab === 'bulk') {
                document.getElementById('tabBulk').style.display = 'block';
                event.target.classList.add('active');
            } else if (tab === 'history') {
                document.getElementById('tabHistory').style.display = 'block';
                event.target.classList.add('active');
            }
        }

        function updateFileName(input) {
            if (input.files.length > 0) {
                document.getElementById('selectedFileName').textContent = "Selected: " + input.files[0].name;
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
