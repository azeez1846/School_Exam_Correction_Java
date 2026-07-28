/* GradePulse AI - Frontend Application Controller */

document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

let currentExamId = 1;
let currentSubmission = null;
let currentEvaluation = null;
let activeLlmProviderKey = "gemini-1.5-flash";

async function initApp() {
    loadLlmConfigs();
    loadExams();
    setupEventListeners();
}

async function loadLlmConfigs() {
    try {
        const res = await fetch('/api/llm-configs');
        if (res.ok) {
            const configs = await res.json();
            const select = document.getElementById('llmProviderSelect');
            if (select) {
                select.innerHTML = '';
                configs.forEach(cfg => {
                    const opt = document.createElement('option');
                    opt.value = cfg.providerKey;
                    opt.textContent = cfg.providerName + (cfg.isDefault ? ' (Active Default)' : '');
                    if (cfg.isDefault) {
                        opt.selected = true;
                        activeLlmProviderKey = cfg.providerKey;
                    }
                    select.appendChild(opt);
                });
            }
        }
    } catch (e) {
        console.error('Failed to load LLM configs', e);
    }
}

async function loadExams() {
    try {
        const res = await fetch('/api/exams');
        if (res.ok) {
            const exams = await res.json();
            const select = document.getElementById('examSelect');
            if (select && exams.length > 0) {
                select.innerHTML = '';
                exams.forEach(ex => {
                    const opt = document.createElement('option');
                    opt.value = ex.id;
                    opt.textContent = `${ex.title} (${ex.subject} - ${ex.gradeLevel})`;
                    select.appendChild(opt);
                });
                currentExamId = exams[0].id;
                loadExamSubmissions(currentExamId);
            }
        }
    } catch (e) {
        console.error('Failed to load exams', e);
    }
}

async function loadExamSubmissions(examId) {
    try {
        const res = await fetch(`/api/submissions?examId=${examId}`);
        if (res.ok) {
            const submissions = await res.json();
            renderSubmissionsList(submissions);
        }
    } catch (e) {
        console.error('Failed to load submissions', e);
    }
}

function renderSubmissionsList(submissions) {
    const container = document.getElementById('submissionsList');
    if (!container) return;
    if (submissions.length === 0) {
        container.innerHTML = '<div style="padding:1.5rem; text-align:center; color:#94a3b8;">No paper submissions uploaded yet.</div>';
        return;
    }

    container.innerHTML = submissions.map(sub => `
        <div class="glass-panel" style="padding:1rem; margin-bottom:0.75rem; display:flex; justify-size:space-between; align-items:center; cursor:pointer;" onclick="viewSubmissionDetails(${sub.id})">
            <div>
                <strong style="color:#fff; font-size:0.95rem;">${sub.studentName}</strong>
                <div style="font-size:0.8rem; color:#94a3b8;">Roll: ${sub.studentRollNumber || 'N/A'} | File: ${sub.originalFileName}</div>
            </div>
            <span class="btn btn-sm btn-outline">View Score</span>
        </div>
    `).join('');
}

async function viewSubmissionDetails(submissionId) {
    try {
        const res = await fetch(`/api/evaluations?submissionId=${submissionId}`);
        if (res.ok) {
            const evalData = await res.json();
            currentEvaluation = evalData;
            renderEvaluationScorecard(evalData);
        }
    } catch (e) {
        console.error('Failed to load evaluation', e);
    }
}

function renderEvaluationScorecard(evalData) {
    const card = document.getElementById('scorecardDisplay');
    if (!card || !evalData) return;

    const gradeClass = `grade-${evalData.letterGrade.replace('+', '-plus')}`;
    let rubricRows = '';
    try {
        const rubrics = JSON.parse(evalData.rubricBreakdownJson || '[]');
        rubricRows = rubrics.map(r => `
            <tr>
                <td><strong>${r.criterion}</strong></td>
                <td>${r.maxMarks}</td>
                <td style="color:#34d399; font-weight:700;">${r.obtainedMarks}</td>
                <td>${r.feedback}</td>
            </tr>
        `).join('');
    } catch (e) {}

    let strengthsList = '';
    try {
        const strArr = JSON.parse(evalData.strengthsJson || '[]');
        strengthsList = strArr.map(s => `<li>${s}</li>`).join('');
    } catch (e) {}

    let improvementsList = '';
    try {
        const impArr = JSON.parse(evalData.improvementAreasJson || '[]');
        improvementsList = impArr.map(i => `<li>${i}</li>`).join('');
    } catch (e) {}

    card.innerHTML = `
        <div class="scorecard-header">
            <div class="student-info">
                <h2>Evaluation Scorecard ${evalData.isTeacherOverridden ? '<span style="font-size:0.75rem; color:#fbbf24; background:rgba(245,158,11,0.2); padding:0.2rem 0.6rem; border-radius:12px;">MANUALLY OVERRIDDEN</span>' : ''}</h2>
                <p>Model: <strong>${evalData.evaluatedByModel}</strong> | Submission #${evalData.submissionId}</p>
            </div>
            <div class="grade-badge ${gradeClass}">
                ${evalData.letterGrade}
                <span style="font-size:1rem; opacity:0.8;">(${evalData.totalMarksObtained}/${evalData.maxMarks})</span>
            </div>
        </div>

        <div class="evaluation-body">
            <h4 style="font-family:var(--font-heading); font-size:1.1rem; margin-bottom:0.5rem;">Rubric Breakdown</h4>
            <table class="rubric-table">
                <thead>
                    <tr>
                        <th>Criterion</th>
                        <th>Max</th>
                        <th>Obtained</th>
                        <th>AI Feedback</th>
                    </tr>
                </thead>
                <tbody>
                    ${rubricRows}
                </tbody>
            </table>

            <div class="strengths-grid">
                <div class="feedback-box strength">
                    <h5><i class="fa-solid fa-circle-check"></i> Key Strengths</h5>
                    <ul>${strengthsList}</ul>
                </div>
                <div class="feedback-box improvement">
                    <h5><i class="fa-solid fa-triangle-exclamation"></i> Focus Improvement Areas</h5>
                    <ul>${improvementsList}</ul>
                </div>
            </div>

            <div style="margin-top:1.5rem; padding:1.25rem; background:rgba(15,23,42,0.6); border-radius:var(--radius-md); border:1px solid var(--border-glass);">
                <h5 style="color:#6366f1; margin-bottom:0.5rem;">Custom Evaluator Feedback</h5>
                <p style="font-size:0.9rem; color:#e2e8f0; line-height:1.5;">${evalData.customTeacherFeedback || 'Good performance.'}</p>
            </div>

            <div style="margin-top:1.5rem; display:flex; flex-wrap:wrap; gap:0.75rem;">
                <button class="btn btn-primary" onclick="openAnnotationModal(${evalData.submissionId})"><i class="fa-solid fa-paintbrush"></i> Annotation & Voice Studio</button>
                <button class="btn btn-secondary" onclick="openOverrideModal(${evalData.submissionId}, ${evalData.totalMarksObtained})"><i class="fa-solid fa-pen-to-square"></i> Override Marks</button>
                <a href="/api/submissions/download-pdf-report?submissionId=${evalData.submissionId}" target="_blank" class="btn btn-outline"><i class="fa-solid fa-file-pdf" style="color:#f43f5e;"></i> Download Official PDF</a>
                <button class="btn btn-secondary" onclick="openEmailModal(${evalData.submissionId})"><i class="fa-solid fa-paper-plane" style="color:#38bdf8;"></i> Email Parent/Student</button>
            </div>
        </div>
    `;
}

function setupEventListeners() {
    const singleForm = document.getElementById('singleUploadForm');
    if (singleForm) {
        singleForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(singleForm);
            formData.append('examId', currentExamId);
            formData.append('providerKey', document.getElementById('llmProviderSelect').value);

            showLoading(true);
            try {
                const res = await fetch('/api/submissions/upload', {
                    method: 'POST',
                    body: formData
                });
                if (res.ok) {
                    const data = await res.json();
                    renderEvaluationScorecard(data.evaluation);
                    loadExamSubmissions(currentExamId);
                }
            } catch (err) {
                console.error(err);
            } finally {
                showLoading(false);
            }
        });
    }

    const bulkForm = document.getElementById('bulkUploadForm');
    if (bulkForm) {
        bulkForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(bulkForm);
            formData.append('examId', currentExamId);
            formData.append('providerKey', document.getElementById('llmProviderSelect').value);

            showLoading(true);
            try {
                const res = await fetch('/api/submissions/bulk', {
                    method: 'POST',
                    body: formData
                });
                if (res.ok) {
                    const evals = await res.json();
                    if (evals.length > 0) {
                        renderEvaluationScorecard(evals[0]);
                    }
                    loadExamSubmissions(currentExamId);
                }
            } catch (err) {
                console.error(err);
            } finally {
                showLoading(false);
            }
        });
    }

    const examSelect = document.getElementById('examSelect');
    if (examSelect) {
        examSelect.addEventListener('change', (e) => {
            currentExamId = e.target.value;
            loadExamSubmissions(currentExamId);
        });
    }

    const splitForm = document.getElementById('splitUploadForm');
    if (splitForm) {
        splitForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const formData = new FormData(splitForm);
            formData.append('examId', currentExamId);

            showLoading(true);
            try {
                const res = await fetch('/api/submissions/split-upload', {
                    method: 'POST',
                    body: formData
                });
                if (res.ok) {
                    const splits = await res.json();
                    alert(`Successfully split into ${splits.length} student papers & auto-processed!`);
                    loadExamSubmissions(currentExamId);
                }
            } catch (err) {
                console.error(err);
            } finally {
                showLoading(false);
            }
        });
    }
}

function showLoading(show) {
    const loader = document.getElementById('loadingOverlay');
    if (loader) loader.style.display = show ? 'flex' : 'none';
}

function exportCsv() {
    window.location.href = `/api/evaluations/export/csv/${currentExamId}`;
}

async function loadDiagnosticInsights() {
    const container = document.getElementById('diagnosticContent');
    if (!container) return;
    try {
        const res = await fetch(`/api/analytics/diagnostic?examId=${currentExamId}`);
        if (res.ok) {
            const data = await res.json();
            const scoreBands = data.scoreBands || {};
            const misconceptions = data.commonMisconceptions || [];
            
            container.innerHTML = `
                <div style="display:grid; grid-template-columns: 1fr 1fr; gap:1rem; margin-bottom:1.5rem;">
                    <div class="glass-panel" style="padding:1.25rem;">
                        <h4 style="color:#6366f1; margin-bottom:0.75rem;"><i class="fa-solid fa-chart-simple"></i> Class Score Bands Distribution</h4>
                        <div style="display:grid; gap:0.5rem;">
                            ${Object.entries(scoreBands).map(([band, count]) => `
                                <div style="display:flex; justify-content:space-between; font-size:0.9rem; color:#e2e8f0;">
                                    <span>${band}</span>
                                    <strong style="color:#34d399;">${count} Students</strong>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                    <div class="glass-panel" style="padding:1.25rem;">
                        <h4 style="color:#fbbf24; margin-bottom:0.75rem;"><i class="fa-solid fa-triangle-exclamation"></i> Common Concept Misconceptions</h4>
                        <ul style="padding-left:1.2rem; color:#cbd5e1; font-size:0.9rem;">
                            ${misconceptions.map(m => `<li>${m}</li>`).join('')}
                        </ul>
                    </div>
                </div>
                <div class="glass-panel" style="padding:1.25rem; background:rgba(99,102,241,0.1); border:1px solid #6366f1;">
                    <h4 style="color:#818cf8; margin-bottom:0.5rem;"><i class="fa-solid fa-compass-drafting"></i> Automated AI Class Remediation Plan</h4>
                    <pre style="white-space:pre-wrap; font-family:inherit; font-size:0.9rem; color:#e2e8f0; line-height:1.5;">${data.remediationPlan || 'No plan needed.'}</pre>
                </div>
            `;
        }
    } catch (e) {
        console.error(e);
    }
}

async function loadPlagiarismAudit() {
    const container = document.getElementById('plagiarismContent');
    if (!container) return;
    try {
        const res = await fetch(`/api/analytics/plagiarism?examId=${currentExamId}`);
        if (res.ok) {
            const pairs = await res.json();
            if (pairs.length === 0) {
                container.innerHTML = '<div class="glass-panel" style="padding:2rem; text-align:center; color:#34d399;"><i class="fa-solid fa-circle-check" style="font-size:2rem; margin-bottom:0.5rem; display:block;"></i> No suspicious peer copying or high similarity detected across submissions.</div>';
                return;
            }
            container.innerHTML = pairs.map(p => `
                <div class="glass-panel" style="padding:1.25rem; margin-bottom:1rem; border-left:4px solid ${p.riskLevel === 'HIGH' ? '#f43f5e' : '#fbbf24'};">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem;">
                        <h4 style="color:#fff;">${p.student1Name} (${p.student1Roll}) &amp; ${p.student2Name} (${p.student2Roll})</h4>
                        <span class="btn btn-sm ${p.riskLevel === 'HIGH' ? 'btn-primary' : 'btn-outline'}" style="background:${p.riskLevel === 'HIGH' ? '#f43f5e' : 'transparent'}">
                            ${p.similarityPercentage}% Similarity [${p.riskLevel} RISK]
                        </span>
                    </div>
                    <p style="font-size:0.85rem; color:#94a3b8; font-style:italic;">Matching Excerpt: "${p.matchingExcerpt}"</p>
                </div>
            `).join('');
        }
    } catch (e) {
        console.error(e);
    }
}

let activeSubIdForAnnotation = null;
let currentTool = 'tick';

function openAnnotationModal(subId) {
    activeSubIdForAnnotation = subId;
    document.getElementById('annotationModal').style.display = 'flex';
    initCanvas();
}

function closeAnnotationModal() {
    document.getElementById('annotationModal').style.display = 'none';
}

function initCanvas() {
    const canvas = document.getElementById('annotationCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#f8fafc';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = '#334155';
    ctx.font = '14px sans-serif';
    ctx.fillText('Scanned Paper Overlay (Draw Annotations)', 40, 230);
    
    let drawing = false;
    canvas.onmousedown = (e) => {
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        if (currentTool === 'tick') {
            ctx.fillStyle = '#16a34a';
            ctx.font = 'bold 24px sans-serif';
            ctx.fillText('✓ (+5)', x, y);
        } else if (currentTool === 'cross') {
            ctx.fillStyle = '#dc2626';
            ctx.font = 'bold 24px sans-serif';
            ctx.fillText('✗ (-2)', x, y);
        } else if (currentTool === 'pen') {
            drawing = true;
            ctx.beginPath();
            ctx.moveTo(x, y);
        }
    };
    canvas.onmousemove = (e) => {
        if (!drawing) return;
        const rect = canvas.getBoundingClientRect();
        ctx.strokeStyle = '#2563eb';
        ctx.lineWidth = 2;
        ctx.lineTo(e.clientX - rect.left, e.clientY - rect.top);
        ctx.stroke();
    };
    canvas.onmouseup = () => { drawing = false; };
}

function setDrawTool(tool) {
    currentTool = tool;
}

function clearCanvas() {
    const canvas = document.getElementById('annotationCanvas');
    if (canvas) {
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
    }
}

async function saveAnnotationsAndAudio() {
    if (!activeSubIdForAnnotation) return;
    const canvas = document.getElementById('annotationCanvas');
    const dataUrl = canvas.toDataURL();

    try {
        await fetch('/api/evaluations/annotation', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ submissionId: activeSubIdForAnnotation, annotationsJson: JSON.stringify({ image: dataUrl }) })
        });
        alert('Annotations and Voice Feedback saved!');
        closeAnnotationModal();
    } catch (e) {
        console.error(e);
    }
}

let mediaRecorder = null;
let audioChunks = [];

async function toggleAudioRecording() {
    const btn = document.getElementById('recordAudioBtn');
    const status = document.getElementById('recordingStatus');

    if (mediaRecorder && mediaRecorder.state === 'recording') {
        mediaRecorder.stop();
        btn.innerHTML = '<i class="fa-solid fa-circle"></i> Start Recording';
        status.textContent = 'Audio recorded!';
    } else {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            mediaRecorder = new MediaRecorder(stream);
            audioChunks = [];
            mediaRecorder.ondataavailable = (e) => audioChunks.push(e.data);
            mediaRecorder.onstop = () => {
                const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
                const player = document.getElementById('audioPreviewPlayer');
                player.src = URL.createObjectURL(audioBlob);
                player.style.display = 'block';
            };
            mediaRecorder.start();
            btn.innerHTML = '<i class="fa-solid fa-square"></i> Stop Recording';
            status.textContent = 'Recording live audio...';
        } catch (e) {
            status.textContent = 'Microphone access denied / simulated audio ready.';
        }
    }
}

function openRubricModal() {
    document.getElementById('rubricModal').style.display = 'flex';
}

function closeRubricModal() {
    document.getElementById('rubricModal').style.display = 'none';
}

async function generateAiRubric() {
    const qText = document.getElementById('rubricQuestionInput').value;
    const mAns = document.getElementById('rubricModelAnswerInput').value;

    try {
        const res = await fetch('/api/exam/generate-rubric', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title: 'Exam', subject: 'Science', questionText: qText, modelAnswer: mAns })
        });
        if (res.ok) {
            const data = await res.json();
            document.getElementById('rubricResultJson').value = data.markingRubricJson;
        }
    } catch (e) {
        console.error(e);
    }
}

function openEmailModal(subId) {
    document.getElementById('emailSubId').value = subId;
    document.getElementById('emailModal').style.display = 'flex';
}

function closeEmailModal() {
    document.getElementById('emailModal').style.display = 'none';
}

async function dispatchReportEmail() {
    const subId = document.getElementById('emailSubId').value;
    const email = document.getElementById('recipientEmailInput').value;

    try {
        const res = await fetch('/api/submissions/send-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ submissionId: subId, email: email })
        });
        if (res.ok) {
            alert('Report card email dispatched successfully to ' + (email || 'parent'));
            closeEmailModal();
        }
    } catch (e) {
        console.error(e);
    }
}

function openOverrideModal(submissionId, currentScore) {
    document.getElementById('overrideSubId').value = submissionId;
    document.getElementById('overrideScoreInput').value = currentScore;
    document.getElementById('overrideModal').style.display = 'flex';
}

function closeOverrideModal() {
    document.getElementById('overrideModal').style.display = 'none';
}

async function submitOverride() {
    const subId = document.getElementById('overrideSubId').value;
    const score = document.getElementById('overrideScoreInput').value;
    const notes = document.getElementById('overrideNotesInput').value;

    try {
        const res = await fetch('/api/evaluations/override', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                submissionId: subId,
                totalMarksObtained: parseFloat(score),
                teacherNotes: notes
            })
        });
        if (res.ok) {
            const updated = await res.json();
            renderEvaluationScorecard(updated);
            closeOverrideModal();
        }
    } catch (e) {
        console.error(e);
    }
}

function openKeyModal() {
    document.getElementById('keyModal').style.display = 'flex';
}

function closeKeyModal() {
    document.getElementById('keyModal').style.display = 'none';
}

async function saveApiKey() {
    const providerKey = document.getElementById('llmProviderSelect').value;
    const apiKey = document.getElementById('modalApiKeyInput').value;

    try {
        const res = await fetch('/api/llm-configs', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                action: 'updateKey',
                providerKey: providerKey,
                apiKey: apiKey
            })
        });
        if (res.ok) {
            alert('API Key updated successfully!');
            closeKeyModal();
        }
    } catch (e) {
        console.error(e);
    }
}
