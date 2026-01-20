// Compiled JavaScript from TypeScript for Insurance Claim Underwriter
"use strict";

class ClaimUnderwriterApp {
    constructor() {
        this.baseUrl = '';
        this.policyModal = document.getElementById('policyModal');
        this.helpdeskModal = document.getElementById('helpdeskModal');
        this.loader = document.getElementById('loader');
        this.resultSection = document.getElementById('resultSection');
        this.resultContent = document.getElementById('resultContent');
        this.lastClaimContext = null;
        
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        var _a, _b, _c;
        // Open policy modal
        (_a = document.getElementById('openPolicyModal')) === null || _a === void 0 ? void 0 : _a.addEventListener('click', () => {
            this.resetPolicyForm();
            this.policyModal.style.display = 'flex';
        });

        // Close policy modal
        (_b = this.policyModal ? this.policyModal.querySelector('.close') : null) === null || _b === void 0 ? void 0 : _b.addEventListener('click', () => {
            this.policyModal.style.display = 'none';
            this.resetPolicyForm();
        });

        // Close modal with cancel button
        (_c = this.policyModal ? this.policyModal.querySelector('.btn-cancel') : null) === null || _c === void 0 ? void 0 : _c.addEventListener('click', () => {
            this.policyModal.style.display = 'none';
            this.resetPolicyForm();
        });

        // Close modal on outside click (backdrop)
        window.addEventListener('click', (event) => {
            if (event.target.classList.contains('modal-backdrop') || event.target === this.policyModal) {
                this.policyModal.style.display = 'none';
                this.resetPolicyForm();
            }
            if (event.target.classList.contains('modal-backdrop') || event.target === this.helpdeskModal) {
                this.helpdeskModal.style.display = 'none';
            }
        });

        // Open help desk
        const openHelpDeskBtn = document.getElementById('openHelpDesk');
        if (openHelpDeskBtn) {
            openHelpDeskBtn.addEventListener('click', () => {
                this.openHelpDesk();
            });
        }

        // Close help desk (header close button)
        const helpDeskClose = this.helpdeskModal ? this.helpdeskModal.querySelector('.close') : null;
        if (helpDeskClose) {
            helpDeskClose.addEventListener('click', () => {
                this.helpdeskModal.style.display = 'none';
            });
        }

        // Close help desk (footer close)
        const hdCancel = document.getElementById('hdCancel');
        if (hdCancel) {
            hdCancel.addEventListener('click', () => {
                this.helpdeskModal.style.display = 'none';
            });
        }

        // Clear transcript
        const hdClear = document.getElementById('hdClear');
        if (hdClear) {
            hdClear.addEventListener('click', () => {
                this.clearHelpDeskTranscript();
            });
        }

        // Help desk submit
        const helpdeskForm = document.getElementById('helpdeskForm');
        if (helpdeskForm) {
            helpdeskForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitHelpDesk();
            });
        }

        // Policy form submission
        const policyForm = document.getElementById('policyForm');
        if (policyForm) {
            policyForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handlePolicySubmission();
            });
        }

        // Claim form submission
        const claimForm = document.getElementById('claimForm');
        if (claimForm) {
            claimForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handleClaimSubmission();
            });
        }
    }

    async handlePolicySubmission() {
        const policyId = document.getElementById('policyId').value;
        const customerId = document.getElementById('customerId').value.toUpperCase();
        const policyNumber = document.getElementById('policyNumberModal').value;
        const policyFileInput = document.getElementById('policyFileUpload');

        // Validate file selection
        if (!policyFileInput.files || policyFileInput.files.length === 0) {
            this.showErrorMessage('Please select a policy document to upload');
            return;
        }

        // Create FormData with multipart fields matching @RequestPart parameters
        const formData = new FormData();
        formData.append('policyId', policyId);
        formData.append('customerId', customerId);
        formData.append('policyNumber', policyNumber);
        formData.append('policy', policyFileInput.files[0]);

        this.showLoader();

        try {
            const response = await fetch(`${this.baseUrl}/ingestion/savePolicyDocument`, {
                method: 'POST',
                body: formData // No Content-Type header - browser sets it with boundary
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.text();
            this.hideLoader();
            this.showSuccessMessage('Policy ingested successfully!');
            this.policyModal.style.display = 'none';
            this.resetPolicyForm();
        } catch (error) {
            this.hideLoader();
            this.showErrorMessage(`Failed to ingest policy: ${error}`);
        }
    }

    async handleClaimSubmission() {
        const policyNumber = document.getElementById('policyNumber').value;
        const inputMethod = document.querySelector('input[name="inputMethod"]:checked');
        const submitBtn = document.getElementById('submitBtn');
        const processingIndicator = document.getElementById('processingIndicator');

        // Validate policy number
        if (!policyNumber || !policyNumber.trim()) {
            this.showErrorMessage('Please enter a policy number');
            return;
        }

        // Validate input method selection
        if (!inputMethod) {
            this.showErrorMessage('Please select an input method (Text or File)');
            return;
        }

        // Show processing indicator and disable button
        if (processingIndicator) {
            processingIndicator.style.display = 'block';
        }
        if (submitBtn) {
            submitBtn.disabled = true;
        }
        this.resultSection.style.display = 'none';

        try {
            let response;
            
            const patientName = document.getElementById('patientName').value;
            
            if (!patientName.trim()) {
                if (processingIndicator) processingIndicator.style.display = 'none';
                if (submitBtn) submitBtn.disabled = false;
                this.showErrorMessage('Please enter patient name');
                return;
            }
            
            if (inputMethod.value === 'text') {
                // Text input mode - use process-react endpoint
                const invoiceText = document.getElementById('invoiceText').value;
                
                if (!invoiceText.trim()) {
                    if (processingIndicator) processingIndicator.style.display = 'none';
                    if (submitBtn) submitBtn.disabled = false;
                    this.showErrorMessage('Please enter invoice details');
                    return;
                }

                const extractRequest = {
                    invoiceText: invoiceText
                };

                response = await fetch(`${this.baseUrl}/claims/process-react?policyNumber=${encodeURIComponent(policyNumber)}&userName=${encodeURIComponent(patientName)}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(extractRequest)
                });
            } else {
                // File input mode - use process-claim endpoint
                const fileInput = document.getElementById('claimFileUpload');
                
                if (!fileInput.files || fileInput.files.length === 0) {
                    if (processingIndicator) processingIndicator.style.display = 'none';
                    if (submitBtn) submitBtn.disabled = false;
                    this.showErrorMessage('Please select a claim file');
                    return;
                }

                const formData = new FormData();
                formData.append('file', fileInput.files[0]);

                response = await fetch(`${this.baseUrl}/claims/process-claim?policyNumber=${encodeURIComponent(policyNumber)}&patientName=${encodeURIComponent(patientName)}`, {
                    method: 'POST',
                    body: formData
                });
            }

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            
            // Hide processing indicator and enable button
            if (processingIndicator) processingIndicator.style.display = 'none';
            if (submitBtn) submitBtn.disabled = false;
            
            this.displayResult(result);
        } catch (error) {
            // Hide processing indicator and enable button
            if (processingIndicator) processingIndicator.style.display = 'none';
            if (submitBtn) submitBtn.disabled = false;
            
            this.showErrorMessage(`Failed to process claim: ${error}`);
        }
    }

    displayResult(result) {
        var _a, _b, _c, _d, _e;
        this.resultSection.style.display = 'block';
        
        if (result.status === 'error') {
            this.resultContent.innerHTML = `
                <div class="message message-error">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="15" y1="9" x2="9" y2="15"></line>
                        <line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>
                    <div>
                        <strong>Error Processing Claim</strong>
                        <p>${result.errorMessage || 'Unknown error occurred'}</p>
                    </div>
                </div>
            `;
            return;
        }

        const decisionClass = ((_a = result.decision) === null || _a === void 0 ? void 0 : _a.toLowerCase()) || 'error';
        const decisionBadge = this.getDecisionBadge(result.decision);
        const itemizedHtml = this.generateItemizedDecisionsHtml(result.itemizedDecisions);
        const totalRequested = this.calculateTotalRequested(result.itemizedDecisions);

        this.lastClaimContext = {
            claimId: result.claimId || '',
            policyNumber: result.policyNumber || ''
        };

        this.resultContent.innerHTML = `
            ${decisionBadge}

            <div class="result-summary">
                <div class="summary-grid">
                    <div class="summary-item">
                        <div class="summary-label">Claim ID</div>
                        <div class="summary-value">#${result.claimId || 'N/A'}</div>
                    </div>
                    <div class="summary-item">
                        <div class="summary-label">Policy Number</div>
                        <div class="summary-value">${result.policyNumber || 'N/A'}</div>
                    </div>
                    <div class="summary-item">
                        <div class="summary-label">Claim Amount</div>
                        <div class="summary-value">₹${totalRequested.toLocaleString('en-IN')}</div>
                    </div>
                    <div class="summary-item">
                        <div class="summary-label">Approved Amount</div>
                        <div class="summary-value amount">₹${((_b = result.payableAmount) === null || _b === void 0 ? void 0 : _b.toLocaleString('en-IN')) || '0'}</div>
                    </div>
                </div>
            </div>

            ${result.reasons && result.reasons.length > 0 ? `
                <div class="itemized-section" style="overflow-y: auto; max-height: 300px;">
                    <div class="expandable-section">
                        <div class="expandable-header" onclick="window.claimApp.toggleDecisionSummary()">
                            <div class="expandable-title">
                                <div class="expandable-icon">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <circle cx="12" cy="12" r="10"></circle>
                                        <line x1="12" y1="16" x2="12" y2="12"></line>
                                        <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                    </svg>
                                </div>
                                <div>
                                    <div style="font-weight: 600; margin-bottom: 2px;">Decision Summary</div>
                                    <div style="font-size: 13px; color: var(--color-text-secondary); font-weight: 400;">
                                        ${result.reasons.length} key point${result.reasons.length !== 1 ? 's' : ''}
                                    </div>
                                </div>
                            </div>
                            <div class="expandable-toggle">
                                <span id="summaryToggleText">View Details</span>
                                <div class="toggle-arrow" id="summaryToggleArrow">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <polyline points="6 9 12 15 18 9"></polyline>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="expandable-content" id="summaryContent">
                            <div class="expandable-body">
                                <div style="background: var(--color-bg); padding: 16px; border-radius: 8px;">
                                    ${result.reasons.map(r => `
                                        <div style="display: flex; align-items: start; gap: 8px; margin-bottom: 8px;">
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink: 0; margin-top: 2px;">
                                                <polyline points="20 6 9 17 4 12"></polyline>
                                            </svg>
                                            <span style="font-size: 14px; color: var(--color-text-secondary);">${r}</span>
                                        </div>
                                    `).join('')}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            ` : ''}

            ${itemizedHtml}

            ${result.letter ? `
                <div class="itemized-section">
                    <div class="expandable-section">
                        <div class="expandable-header" onclick="window.claimApp.toggleDecisionLetter()">
                            <div class="expandable-title">
                                <div class="expandable-icon">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                        <polyline points="14 2 14 8 20 8"></polyline>
                                    </svg>
                                </div>
                                <div>
                                    <div style="font-weight: 600; margin-bottom: 2px;">Decision Letter</div>
                                    <div style="font-size: 13px; color: var(--color-text-secondary); font-weight: 400;">
                                        Official claim adjudication letter
                                    </div>
                                </div>
                            </div>
                            <div class="expandable-toggle">
                                <span id="letterToggleText">View Letter</span>
                                <div class="toggle-arrow" id="letterToggleArrow">
                                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <polyline points="6 9 12 15 18 9"></polyline>
                                    </svg>
                                </div>
                            </div>
                        </div>
                        <div class="expandable-content" id="letterContent">
                            <div class="expandable-body">
                                <div class="letter-container-inner">
                                    <div class="letter-header-inner">
                                        <div class="letter-header-spacer" aria-hidden="true"></div>
                                        <h3 class="letter-title">DECISION LETTER</h3>
                                        <div class="letter-actions" aria-label="Decision letter actions">
                                            <button class="letter-action-btn" data-tooltip="Save" onclick="window.claimApp.downloadLetter()" aria-label="Save">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                                                    <polyline points="7 10 12 15 17 10"></polyline>
                                                    <line x1="12" y1="15" x2="12" y2="3"></line>
                                                </svg>
                                            </button>
                                            <button class="letter-action-btn" data-tooltip="Print" onclick="window.print()" aria-label="Print">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                    <polyline points="6 9 6 2 18 2 18 9"></polyline>
                                                    <path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"></path>
                                                    <rect x="6" y="14" width="12" height="8"></rect>
                                                </svg>
                                            </button>
                                            <button class="letter-action-btn" data-tooltip="Email" onclick="window.claimApp.emailLetter()" aria-label="Email">
                                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                                                    <polyline points="22,6 12,13 2,6"></polyline>
                                                </svg>
                                            </button>
                                        </div>
                                    </div>
                                    <div class="letter-content" id="letterText">${this.formatLetterWithBullets(result.letter)}</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            ` : ''}
        `;

        this.resultSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    openHelpDesk() {
        if (!this.helpdeskModal) {
            return;
        }

        // Prefill with latest known claim context (only if user hasn't typed)
        const claimIdEl = document.getElementById('hdClaimId');
        const policyEl = document.getElementById('hdPolicyNumber');
        if (this.lastClaimContext) {
            if (claimIdEl && !claimIdEl.value) {
                claimIdEl.value = this.lastClaimContext.claimId || '';
            }
            if (policyEl && !policyEl.value) {
                policyEl.value = this.lastClaimContext.policyNumber || '';
            }
        }

        this.helpdeskModal.style.display = 'flex';

        const issueEl = document.getElementById('hdIssue');
        if (issueEl) {
            issueEl.focus();
        }
    }

    clearHelpDeskTranscript() {
        const transcript = document.getElementById('hdTranscript');
        if (!transcript) {
            return;
        }
        transcript.innerHTML = `
            <div class="helpdesk-empty">
                <div class="helpdesk-empty-title">How can I help?</div>
                <div class="helpdesk-empty-sub">Submit an issue on the left to get an AI response here.</div>
            </div>
        `;

        const aiBox = document.getElementById('hdAiResponse');
        if (aiBox) {
            aiBox.innerHTML = `
                <div class="empty-response-unified">
                    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <circle cx="12" cy="12" r="10"></circle>
                        <path d="M8 14s1.5 2 4 2 4-2 4-2"></path>
                        <line x1="9" y1="9" x2="9.01" y2="9"></line>
                        <line x1="15" y1="9" x2="15.01" y2="9"></line>
                    </svg>
                    <p>Your AI response will appear here</p>
                </div>
            `;
            aiBox.classList.remove('has-response');
        }
    }

    setAiResponseBox(text) {
        const aiBox = document.getElementById('hdAiResponse');
        if (aiBox) {
            if (text === 'No response yet.' || text === 'Sending…') {
                aiBox.innerHTML = `
                    <div class="empty-response-unified">
                        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <circle cx="12" cy="12" r="10"></circle>
                            <path d="M8 14s1.5 2 4 2 4-2 4-2"></path>
                            <line x1="9" y1="9" x2="9.01" y2="9"></line>
                            <line x1="15" y1="9" x2="15.01" y2="9"></line>
                        </svg>
                        <p>${text}</p>
                    </div>
                `;
                aiBox.classList.remove('has-response');
            } else {
                aiBox.textContent = text;
                aiBox.classList.add('has-response');
            }
        }
    }

    appendChatBubble(role, text) {
        const transcript = document.getElementById('hdTranscript');
        if (!transcript) {
            return;
        }

        const bubble = document.createElement('div');
        bubble.className = `chat-bubble ${role}`;
        bubble.textContent = text;

        transcript.appendChild(bubble);
        transcript.scrollTop = transcript.scrollHeight;

        if (role === 'ai') {
            const aiBox = document.getElementById('hdAiResponse');
            if (aiBox) {
                aiBox.textContent = text;
                aiBox.classList.add('has-response');
            }
        }
    }

    setHelpDeskSubmitting(isSubmitting) {
        const submitBtn = document.getElementById('hdSubmit');
        const clearBtn = document.getElementById('hdClear');
        const form = document.getElementById('helpdeskForm');
        if (submitBtn) {
            submitBtn.disabled = isSubmitting;
        }
        if (clearBtn) {
            clearBtn.disabled = isSubmitting;
        }
        if (form) {
            const inputs = form.querySelectorAll('input, textarea, button');
            inputs.forEach((el) => {
                if (el.id !== 'hdCancel') {
                    el.disabled = isSubmitting;
                }
            });
        }
    }

    showHelpDeskTyping() {
        const transcript = document.getElementById('hdTranscript');
        if (!transcript) {
            return null;
        }

        const typing = document.createElement('div');
        typing.className = 'chat-bubble ai';
        typing.innerHTML = `
            <div class="helpdesk-loading" aria-label="AI is responding">
                <span class="helpdesk-dot"></span>
                <span class="helpdesk-dot"></span>
                <span class="helpdesk-dot"></span>
                <span>AI is responding…</span>
            </div>
        `;

        transcript.appendChild(typing);
        transcript.scrollTop = transcript.scrollHeight;
        return typing;
    }

    async submitHelpDesk() {
        const customerName = document.getElementById('hdCustomerName')?.value?.trim();
        const claimId = document.getElementById('hdClaimId')?.value?.trim();
        const policyNumber = document.getElementById('hdPolicyNumber')?.value?.trim();
        const issueDescription = document.getElementById('hdIssue')?.value?.trim();

        if (!customerName || !issueDescription) {
            this.setAiResponseBox('Please provide Customer Name and Issue Description.');
            return;
        }

        const payload = {
            claimId: claimId || '',
            issueDescription,
            customerName,
            policyNumber: policyNumber || ''
        };

        this.appendChatBubble('user', issueDescription);
        this.setAiResponseBox('Sending…');
        this.setHelpDeskSubmitting(true);
        const typingEl = this.showHelpDeskTyping();

        try {
            const response = await fetch(`${this.baseUrl}/api/helpdesk-call/helpUser`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const answer = await response.text();
            if (typingEl) {
                typingEl.remove();
            }
            this.appendChatBubble('ai', answer || '');
            this.setAiResponseBox(answer || '');

            const issueEl = document.getElementById('hdIssue');
            if (issueEl) {
                issueEl.value = '';
            }
        } catch (error) {
            if (typingEl) {
                typingEl.remove();
            }
            const msg = `Sorry, I couldn't process that right now. ${error}`;
            this.appendChatBubble('ai', msg);
            this.setAiResponseBox(msg);
        } finally {
            this.setHelpDeskSubmitting(false);
        }
    }

    getDecisionBadge(decision) {
        const badges = {
            'APPROVED': `
                <div class="result-badge badge-approved">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                    Claim Approved
                </div>
            `,
            'PARTIAL': `
                <div class="result-badge badge-partial">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="12" y1="8" x2="12" y2="12"></line>
                        <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    Partially Approved
                </div>
            `,
            'DENIED': `
                <div class="result-badge badge-denied">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="15" y1="9" x2="9" y2="15"></line>
                        <line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>
                    Claim Denied
                </div>
            `
        };
        return badges[decision] || badges['DENIED'];
    }

    calculateTotalRequested(itemizedDecisions) {
        if (!itemizedDecisions || itemizedDecisions.length === 0) {
            return 0;
        }
        return itemizedDecisions.reduce((total, item) => total + (item.amount || 0), 0);
    }

    generateItemizedDecisionsHtml(itemizedDecisions) {
        if (!itemizedDecisions || itemizedDecisions.length === 0) {
            return '';
        }

        const covered = itemizedDecisions.filter(item => item.covered);
        const notCovered = itemizedDecisions.filter(item => !item.covered);
        const totalItems = itemizedDecisions.length;

        let html = `
            <div class="itemized-section">
                <div class="expandable-section">
                    <div class="expandable-header" onclick="window.claimApp.toggleItemizedServices()">
                        <div class="expandable-title">
                            <div class="expandable-icon">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                                    <line x1="9" y1="9" x2="15" y2="9"></line>
                                    <line x1="9" y1="15" x2="15" y2="15"></line>
                                </svg>
                            </div>
                            <div>
                                <div style="font-weight: 600; margin-bottom: 2px;">Itemized Services</div>
                                <div style="font-size: 13px; color: var(--color-text-secondary); font-weight: 400;">
                                    ${totalItems} service${totalItems !== 1 ? 's' : ''} • ${covered.length} covered • ${notCovered.length} excluded
                                </div>
                            </div>
                        </div>
                        <div class="expandable-toggle">
                            <span id="toggleText">View Details</span>
                            <div class="toggle-arrow" id="toggleArrow">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <polyline points="6 9 12 15 18 9"></polyline>
                                </svg>
                            </div>
                        </div>
                    </div>
                    <div class="expandable-content" id="itemizedContent">
                        <div class="expandable-body">
                            <div class="services-grid">
        `;

        if (covered.length > 0) {
            html += `
                <div class="service-category">
                    <div class="category-header">
                        <span class="category-badge covered">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <polyline points="20 6 9 17 4 12"></polyline>
                            </svg>
                            Covered Services
                        </span>
                        <span class="category-count">${covered.length} item${covered.length !== 1 ? 's' : ''}</span>
                    </div>
            `;

            covered.forEach(item => {
                var _a;
                html += `
                    <div class="decision-item covered">
                        <div class="decision-info">
                            <div class="decision-service">${item.service}</div>
                            ${item.coPayment ? `
                                <div class="decision-reason">Copayment: ₹${item.coPayment.toLocaleString('en-IN')}</div>
                            ` : ''}
                        </div>
                        <div class="decision-amount">₹${((_a = item.amount) === null || _a === void 0 ? void 0 : _a.toLocaleString('en-IN')) || '0'}</div>
                    </div>
                `;
            });

            html += '</div>';
        }

        if (notCovered.length > 0) {
            html += `
                <div class="service-category">
                    <div class="category-header">
                        <span class="category-badge excluded">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="6" x2="18" y2="18"></line>
                            </svg>
                            Excluded Services
                        </span>
                        <span class="category-count">${notCovered.length} item${notCovered.length !== 1 ? 's' : ''}</span>
                    </div>
            `;

            notCovered.forEach(item => {
                var _a;
                html += `
                    <div class="decision-item not-covered">
                        <div class="decision-info">
                            <div class="decision-service">${item.service}</div>
                            <div class="decision-reason">${item.reason || 'Not covered'}</div>
                        </div>
                        <div class="decision-amount">₹${((_a = item.amount) === null || _a === void 0 ? void 0 : _a.toLocaleString('en-IN')) || '0'}</div>
                    </div>
                `;
            });

            html += '</div>';
        }

        html += `
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        return html;
    }

    toggleItemizedServices() {
        const content = document.getElementById('itemizedContent');
        const arrow = document.getElementById('toggleArrow');
        const toggleText = document.getElementById('toggleText');

        if (!content || !arrow || !toggleText) return;

        const isExpanded = content.classList.contains('expanded');

        if (isExpanded) {
            content.classList.remove('expanded');
            arrow.classList.remove('expanded');
            toggleText.textContent = 'View Details';
        } else {
            content.classList.add('expanded');
            arrow.classList.add('expanded');
            toggleText.textContent = 'Hide Details';
        }
    }

    toggleDecisionSummary() {
        const content = document.getElementById('summaryContent');
        const arrow = document.getElementById('summaryToggleArrow');
        const toggleText = document.getElementById('summaryToggleText');

        if (!content || !arrow || !toggleText) return;

        const isExpanded = content.classList.contains('expanded');

        if (isExpanded) {
            content.classList.remove('expanded');
            arrow.classList.remove('expanded');
            toggleText.textContent = 'View Details';
        } else {
            content.classList.add('expanded');
            arrow.classList.add('expanded');
            toggleText.textContent = 'Hide Details';
        }
    }

    toggleDecisionLetter() {
        const content = document.getElementById('letterContent');
        const arrow = document.getElementById('letterToggleArrow');
        const toggleText = document.getElementById('letterToggleText');

        if (!content || !arrow || !toggleText) return;

        const isExpanded = content.classList.contains('expanded');

        if (isExpanded) {
            content.classList.remove('expanded');
            arrow.classList.remove('expanded');
            toggleText.textContent = 'View Letter';
        } else {
            content.classList.add('expanded');
            arrow.classList.add('expanded');
            toggleText.textContent = 'Hide Letter';
        }
    }

    formatLetterWithBullets(letterText) {
        // Convert "Covered Services:" section to use bullet points
        let formatted = letterText.replace(/Covered Services:\n((?:- [^\n]+\n)+)/g, (match, services) => {
            const bulletServices = services
                .split('\n')
                .filter(line => line.trim())
                .map(line => line.replace(/^- /, '• '))
                .join('\n');
            return `Covered Services:\n${bulletServices}\n`;
        });

        // Also handle "Excluded Services:" section
        formatted = formatted.replace(/Excluded Services:\n((?:- [^\n]+\n)+)/g, (match, services) => {
            const bulletServices = services
                .split('\n')
                .filter(line => line.trim())
                .map(line => line.replace(/^- /, '• '))
                .join('\n');
            return `Excluded Services:\n${bulletServices}\n`;
        });

        return formatted;
    }

    resetPolicyForm() {
        const policyForm = document.getElementById('policyForm');
        if (policyForm) {
            policyForm.reset();
        }
        // Clear file input
        const policyFileInput = document.getElementById('policyFileUpload');
        if (policyFileInput) {
            policyFileInput.value = '';
        }
        // Hide file info display and show upload area
        const fileInfo = document.getElementById('fileInfo');
        if (fileInfo) {
            fileInfo.style.display = 'none';
        }
        const uploadArea = document.getElementById('uploadArea');
        if (uploadArea) {
            uploadArea.style.display = 'block';
        }
    }

    showLoader() {
        if (this.loader) {
            this.loader.style.display = 'flex';
            console.log('Loader shown');
        } else {
            console.error('Loader element not found');
        }
    }

    hideLoader() {
        if (this.loader) {
            this.loader.style.display = 'none';
            console.log('Loader hidden');
        }
    }

    showSuccessMessage(message) {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'success-message';
        messageDiv.textContent = message;
        messageDiv.style.position = 'fixed';
        messageDiv.style.top = '20px';
        messageDiv.style.right = '20px';
        messageDiv.style.zIndex = '3000';
        document.body.appendChild(messageDiv);

        setTimeout(() => {
            messageDiv.remove();
        }, 3000);
    }

    showErrorMessage(message) {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'error-message';
        messageDiv.textContent = message;
        messageDiv.style.position = 'fixed';
        messageDiv.style.top = '20px';
        messageDiv.style.right = '20px';
        messageDiv.style.zIndex = '3000';
        document.body.appendChild(messageDiv);

        setTimeout(() => {
            messageDiv.remove();
        }, 5000);
    }

    downloadLetter() {
        const letterContent = document.getElementById('letterText');
        if (!letterContent) {
            this.showErrorMessage('No decision letter available to download');
            return;
        }

        const letterText = letterContent.textContent;
        const blob = new Blob([letterText], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `claim-decision-letter-${new Date().getTime()}.txt`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        this.showSuccessMessage('Decision letter downloaded successfully');
    }

    emailLetter() {
        const letterContent = document.getElementById('letterText');
        if (!letterContent) {
            this.showErrorMessage('No decision letter available to email');
            return;
        }

        const subject = 'Insurance Claim Decision Letter';
        const body = encodeURIComponent(letterContent.textContent);
        const mailtoLink = `mailto:?subject=${encodeURIComponent(subject)}&body=${body}`;

        window.location.href = mailtoLink;
    }

    // Added dynamic content loading for better performance
    async loadDynamicContent(url, targetElementId) {
        try {
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const content = await response.text();
            document.getElementById(targetElementId).innerHTML = content;
        } catch (error) {
            this.showErrorMessage(`Failed to load content: ${error}`);
        }
    }
}

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.claimApp = new ClaimUnderwriterApp();
    initializeFileUpload();
    initializeClaimInputMethod();
    initializeClaimFileUpload();
});

// Initialize input method radio buttons
function initializeClaimInputMethod() {
    const textRadio = document.getElementById('inputMethodText');
    const fileRadio = document.getElementById('inputMethodFile');
    const textSection = document.getElementById('textInputSection');
    const fileSection = document.getElementById('fileInputSection');

    if (textRadio && fileRadio && textSection && fileSection) {
        textRadio.addEventListener('change', () => {
            if (textRadio.checked) {
                textSection.style.display = 'block';
                fileSection.style.display = 'none';
                document.getElementById('invoiceText').required = true;
                document.getElementById('claimFileUpload').required = false;
                
                // Don't reset file upload UI - preserve the selected file
            }
        });

        fileRadio.addEventListener('change', () => {
            if (fileRadio.checked) {
                textSection.style.display = 'none';
                document.getElementById('invoiceText').required = false;
                document.getElementById('claimFileUpload').required = true;
                
                const fileInput = document.getElementById('claimFileUpload');
                
                // Check if a file is already selected
                if (fileInput && fileInput.files && fileInput.files.length > 0) {
                    // File exists, just show the file section with existing file
                    fileSection.style.display = 'block';
                } else {
                    // No file selected, reset UI and open picker
                    fileSection.style.display = 'none';
                    resetFileUploadUI();
                    
                    // Automatically trigger file picker dialog
                    setTimeout(() => {
                        document.getElementById('claimFileUpload').click();
                    }, 100);
                }
            }
        });
    }
}

// Reset file upload UI to initial state
function resetFileUploadUI() {
    const fileInput = document.getElementById('claimFileUpload');
    const uploadArea = document.getElementById('claimUploadArea');
    const filePreview = document.getElementById('claimFilePreview');
    
    if (fileInput) fileInput.value = '';
    if (uploadArea) uploadArea.style.display = 'block';
    if (filePreview) filePreview.style.display = 'none';
}

// Initialize claim file upload drag-and-drop
function initializeClaimFileUpload() {
    const fileInput = document.getElementById('claimFileUpload');
    const uploadArea = document.getElementById('claimUploadArea');
    const filePreview = document.getElementById('claimFilePreview');
    const fileName = document.getElementById('claimFileName');

    if (!fileInput || !uploadArea) return;

    // Click to upload
    uploadArea.addEventListener('click', () => {
        fileInput.click();
    });

    // File selected
    fileInput.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            // Show the file section when file is selected
            const fileSection = document.getElementById('fileInputSection');
            if (fileSection) {
                fileSection.style.display = 'block';
            }
            showClaimFile(file);
        }
    });

    // Drag and drop
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        
        const file = e.dataTransfer.files[0];
        if (file) {
            fileInput.files = e.dataTransfer.files;
            showClaimFile(file);
        }
    });

    function showClaimFile(file) {
        if (fileName && filePreview) {
            fileName.textContent = file.name;
            filePreview.style.display = 'block';
            uploadArea.style.display = 'none';
        }
    }
}

// Remove claim file
function removeClaimFile() {
    const fileInput = document.getElementById('claimFileUpload');
    const uploadArea = document.getElementById('claimUploadArea');
    const filePreview = document.getElementById('claimFilePreview');
    const fileSection = document.getElementById('fileInputSection');
    const fileRadio = document.getElementById('inputMethodFile');
    
    if (fileInput) fileInput.value = '';
    if (filePreview) filePreview.style.display = 'none';
    if (uploadArea) uploadArea.style.display = 'block';
    if (fileSection) fileSection.style.display = 'none';  // Hide entire file section
    if (fileRadio) fileRadio.checked = false;  // Uncheck radio so user can select again
}


// File upload functionality
function initializeFileUpload() {
    const fileInput = document.getElementById('policyFileUpload');
    const uploadArea = document.getElementById('uploadArea');
    const fileInfo = document.getElementById('fileInfo');
    const fileName = document.getElementById('fileName');
    const cloudBtn = document.querySelector('.btn-cloud');

    if (!fileInput || !uploadArea) return;

    // Cloud upload button
    if (cloudBtn) {
        cloudBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            handleCloudUpload();
        });
    }

    // Click to upload
    uploadArea.addEventListener('click', (e) => {
        if (!e.target.closest('.btn-cloud') && !e.target.closest('.btn-upload')) {
            fileInput.click();
        }
    });

    // File input change
    fileInput.addEventListener('change', (e) => {
        handleFileSelect(e.target.files[0]);
    });

    // Drag and drop
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = 'var(--color-secondary)';
        uploadArea.style.background = 'linear-gradient(135deg, rgba(230, 240, 255, 0.8) 0%, rgba(209, 250, 229, 0.8) 100%)';
    });

    uploadArea.addEventListener('dragleave', (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = 'var(--color-primary)';
        uploadArea.style.background = 'linear-gradient(135deg, rgba(230, 240, 255, 0.4) 0%, rgba(209, 250, 229, 0.4) 100%)';
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.style.borderColor = 'var(--color-primary)';
        uploadArea.style.background = 'linear-gradient(135deg, rgba(230, 240, 255, 0.4) 0%, rgba(209, 250, 229, 0.4) 100%)';
        
        const file = e.dataTransfer.files[0];
        handleFileSelect(file);
    });

    function handleFileSelect(file) {
        if (!file) return;

        // Validate file type
        const allowedTypes = ['.pdf', '.doc', '.docx', '.txt'];
        const fileExtension = '.' + file.name.split('.').pop().toLowerCase();
        
        if (!allowedTypes.includes(fileExtension)) {
            alert('Please upload a valid file (PDF, DOC, DOCX, or TXT)');
            return;
        }

        // Validate file size (10MB max)
        if (file.size > 10 * 1024 * 1024) {
            alert('File size must be less than 10MB');
            return;
        }

        // Display file info
        fileName.textContent = file.name;
        fileInfo.style.display = 'flex';
        uploadArea.style.display = 'none';
    }
}

function removeFile() {
    const fileInput = document.getElementById('policyFileUpload');
    const uploadArea = document.getElementById('uploadArea');
    const fileInfo = document.getElementById('fileInfo');

    if (fileInput) fileInput.value = '';
    if (fileInfo) fileInfo.style.display = 'none';
    if (uploadArea) uploadArea.style.display = 'block';
}

function handleCloudUpload() {
    // Create a modal/menu for cloud storage options
    const cloudOptions = `
        <div class="cloud-menu" id="cloudMenu" style="
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: white;
            padding: 24px;
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            z-index: 10000;
            min-width: 320px;
            animation: slideDown 0.3s ease;
        ">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h3 style="margin: 0; font-size: 18px; font-weight: 600;">Choose Cloud Storage</h3>
                <button onclick="closeCloudMenu()" style="
                    background: none;
                    border: none;
                    cursor: pointer;
                    padding: 4px;
                ">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="18" y1="6" x2="6" y2="18"></line>
                        <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                </button>
            </div>
            <div class="cloud-providers">
                <button class="cloud-provider-btn" onclick="selectCloudProvider('Google Drive')" style="
                    width: 100%;
                    padding: 14px;
                    margin-bottom: 12px;
                    border: 2px solid #e5e7eb;
                    border-radius: 12px;
                    background: white;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    transition: all 0.3s ease;
                    font-size: 15px;
                    font-weight: 500;
                ">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                        <path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z" fill="#4285F4"/>
                    </svg>
                    Google Drive
                </button>
                <button class="cloud-provider-btn" onclick="selectCloudProvider('Dropbox')" style="
                    width: 100%;
                    padding: 14px;
                    margin-bottom: 12px;
                    border: 2px solid #e5e7eb;
                    border-radius: 12px;
                    background: white;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    transition: all 0.3s ease;
                    font-size: 15px;
                    font-weight: 500;
                ">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                        <path d="M12 2L2 9l10 7 10-7-10-7z" fill="#0061FF"/>
                        <path d="M2 16l10 7 10-7-10-7-10 7z" fill="#0061FF" opacity="0.7"/>
                    </svg>
                    Dropbox
                </button>
                <button class="cloud-provider-btn" onclick="selectCloudProvider('OneDrive')" style="
                    width: 100%;
                    padding: 14px;
                    border: 2px solid #e5e7eb;
                    border-radius: 12px;
                    background: white;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    gap: 12px;
                    transition: all 0.3s ease;
                    font-size: 15px;
                    font-weight: 500;
                ">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                        <path d="M3 12.5l9-9 9 9-9 9-9-9z" fill="#0078D4"/>
                    </svg>
                    OneDrive
                </button>
            </div>
        </div>
        <div class="cloud-backdrop" onclick="closeCloudMenu()" style="
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            backdrop-filter: blur(4px);
            z-index: 9999;
        "></div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', cloudOptions);
    
    // Add hover effect
    document.querySelectorAll('.cloud-provider-btn').forEach(btn => {
        btn.addEventListener('mouseenter', function() {
            this.style.borderColor = '#667EEA';
            this.style.transform = 'translateY(-2px)';
            this.style.boxShadow = '0 4px 12px rgba(102, 126, 234, 0.2)';
        });
        btn.addEventListener('mouseleave', function() {
            this.style.borderColor = '#e5e7eb';
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = 'none';
        });
    });
}

function closeCloudMenu() {
    const menu = document.getElementById('cloudMenu');
    const backdrop = document.querySelector('.cloud-backdrop');
    if (menu) menu.remove();
    if (backdrop) backdrop.remove();
}

function selectCloudProvider(provider) {
    closeCloudMenu();
    
    // Show a message (in production, this would integrate with actual cloud APIs)
    const message = document.createElement('div');
    message.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
        color: white;
        padding: 16px 24px;
        border-radius: 12px;
        box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
        z-index: 10000;
        font-size: 15px;
        font-weight: 500;
        animation: slideDown 0.3s ease;
    `;
    message.innerHTML = `
        <div style="display: flex; align-items: center; gap: 12px;">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
            Opening ${provider}... (Feature coming soon)
        </div>
    `;
    
    document.body.appendChild(message);
    
    setTimeout(() => {
        message.remove();
    }, 3000);
}

// Toggle conversation history in Help Desk
function toggleConversation() {
    const toggle = document.querySelector('.conversation-toggle-unified');
    const history = document.getElementById('hdTranscript');
    
    if (!toggle || !history) return;
    
    if (history.style.display === 'none') {
        history.style.display = 'block';
        toggle.classList.add('expanded');
        toggle.querySelector('span').textContent = 'Hide Conversation History';
    } else {
        history.style.display = 'none';
        toggle.classList.remove('expanded');
        toggle.querySelector('span').textContent = 'View Conversation History';
    }
}
