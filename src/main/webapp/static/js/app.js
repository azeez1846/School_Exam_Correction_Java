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
            const eval = await res.json();
            currentEvaluation = eval;
            renderEvaluationScorecard(eval);
        }
    } catch (e) {
        console.error('Failed to load evaluation', e);
    }
}

function renderEvaluationScorecard(eval) {
    const card = document.getElementById('scorecardDisplay');
    if (!card || !eval) return;

    const gradeClass = `grade-${eval.letterGrade.replace('+', '-plus')}`;
    let rubricRows = '';
    try {
        const rubrics = JSON.parse(eval.rubricBreakdownJson || '[]');
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
        const strArr = JSON.parse(eval.strengthsJson || '[]');
        strengthsList = strArr.map(s => `<li>${s}</li>`).join('');
    } catch (e) {}

    let improvementsList = '';
    try {
        const impArr = JSON.parse(eval.improvementAreasJson || '[]');
        improvementsList = impArr.map(i => `<li>${i}</li>`).join('');
    } catch (e) {}

    card.innerHTML = `
        <div class="scorecard-header">
            <div class="student-info">
                <h2>Evaluation Scorecard ${eval.isTeacherOverridden ? '<span style="font-size:0.75rem; color:#fbbf24; background:rgba(245,158,11,0.2); padding:0.2rem 0.6rem; border-radius:12px;">MANUALLY OVERRIDDEN</span>' : ''}</h2>
                <p>Model: <strong>${eval.evaluatedByModel}</strong> | Submission #${eval.submissionId}</p>
            </div>
            <div class="grade-badge ${gradeClass}">
                ${eval.letterGrade}
                <span style="font-size:1rem; opacity:0.8;">(${eval.totalMarksObtained}/${eval.maxMarks})</span>
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
                <p style="font-size:0.9rem; color:#e2e8f0; line-height:1.5;">${eval.customTeacherFeedback || 'Good performance.'}</p>
            </div>

            <div style="margin-top:1.5rem; display:flex; gap:1rem;">
                <button class="btn btn-secondary" onclick="openOverrideModal(${eval.submissionId}, ${eval.totalMarksObtained})"><i class="fa-solid fa-pen-to-square"></i> Override Marks</button>
                <a href="/report-card/${eval.submissionId}" target="_blank" class="btn btn-outline"><i class="fa-solid fa-print"></i> Printable Report Card</a>
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
}

function showLoading(show) {
    const loader = document.getElementById('loadingOverlay');
    if (loader) loader.style.display = show ? 'flex' : 'none';
}

function exportCsv() {
    window.location.href = `/api/evaluations/export/csv/${currentExamId}`;
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
