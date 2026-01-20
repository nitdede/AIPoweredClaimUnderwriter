// TypeScript source file for Insurance Claim Underwriter

interface PolicyMetaData {
    policyId: string;
    customerId: string;
    policyNumber: string;
}

interface ExtractRequest {
    invoiceText: string;
}

interface ItemizedDecision {
    service: string;
    amount: number;
    covered: boolean;
    reason?: string;
    coPayment?: number;
}

interface ClaimProcessingResult {
    status: string;
    claimId?: number;
    policyNumber?: string;
    decision?: string;
    payableAmount?: number;
    reasons?: string[];
    itemizedDecisions?: ItemizedDecision[];
    letter?: string;
    errorMessage?: string;
}

class ClaimUnderwriterApp {
    private baseUrl: string = '';
    private policyModal: HTMLElement;
    private loader: HTMLElement;
    private resultSection: HTMLElement;
    private resultContent: HTMLElement;

    constructor() {
        this.policyModal = document.getElementById('policyModal')!;
        this.loader = document.getElementById('loader')!;
        this.resultSection = document.getElementById('resultSection')!;
        this.resultContent = document.getElementById('resultContent')!;
        
        this.initializeEventListeners();
    }

    private initializeEventListeners(): void {
        // Open policy modal
        document.getElementById('openPolicyModal')?.addEventListener('click', () => {
            this.resetPolicyForm();
            this.policyModal.style.display = 'block';
        });

        // Close policy modal
        document.querySelector('.close')?.addEventListener('click', () => {
            this.policyModal.style.display = 'none';
            this.resetPolicyForm();
        });

        // Close policy modal with cancel button
        this.policyModal.querySelector('.btn-cancel')?.addEventListener('click', () => {
            this.policyModal.style.display = 'none';
            this.resetPolicyForm();
        });

        // Close modal on outside click
        window.addEventListener('click', (event) => {
            if (event.target === this.policyModal) {
                this.policyModal.style.display = 'none';
                this.resetPolicyForm();
            }
        });

        // Policy form submission
        document.getElementById('policyForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handlePolicySubmission();
        });

        // Claim form submission
        document.getElementById('claimForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleClaimSubmission();
        });

        // Dynamic content loading example
        const dynamicContentButton = document.getElementById('loadDynamicContent');
        if (dynamicContentButton) {
            dynamicContentButton.addEventListener('click', () => {
                this.loadDynamicContent('/dynamic-content-url', 'dynamicContentContainer');
            });
        }
    }

    private async handlePolicySubmission(): Promise<void> {
        const policyId = (document.getElementById('policyId') as HTMLInputElement).value;
        const customerId = (document.getElementById('customerId') as HTMLInputElement).value.toUpperCase();
        const policyNumber = (document.getElementById('policyNumberModal') as HTMLInputElement).value;
        const policyFileInput = document.getElementById('policyFileUpload') as HTMLInputElement;

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

    private async handleClaimSubmission(): Promise<void> {
        const policyNumber = (document.getElementById('policyNumber') as HTMLInputElement).value;
        const invoiceText = (document.getElementById('invoiceText') as HTMLTextAreaElement).value;

        const extractRequest: ExtractRequest = {
            invoiceText
        };

        this.showLoader();
        this.resultSection.style.display = 'none';

        try {
            const response = await fetch(`${this.baseUrl}/claims/process-react?policyNumber=${encodeURIComponent(policyNumber)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(extractRequest)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result: ClaimProcessingResult = await response.json();
            this.hideLoader();
            this.displayResult(result);
        } catch (error) {
            this.hideLoader();
            this.showErrorMessage(`Failed to process claim: ${error}`);
        }
    }

    private displayResult(result: ClaimProcessingResult): void {
        this.resultSection.style.display = 'block';
        
        if (result.status === 'error') {
            this.resultContent.innerHTML = `
                <div class="error-message">
                    <strong>Error:</strong> ${result.errorMessage || 'Unknown error occurred'}
                </div>
            `;
            return;
        }

        const decisionClass = result.decision?.toLowerCase() || 'error';
        const itemizedHtml = this.generateItemizedDecisionsHtml(result.itemizedDecisions);

        this.resultContent.innerHTML = `
            <div class="result-status status-${decisionClass}">
                ${result.decision || 'UNKNOWN'}
            </div>
            
            <div class="result-details">
                <div class="result-item">
                    <strong>Claim ID:</strong> ${result.claimId || 'N/A'}
                </div>
                <div class="result-item">
                    <strong>Policy Number:</strong> ${result.policyNumber || 'N/A'}
                </div>
                <div class="result-item">
                    <strong>Payable Amount:</strong> ₹${result.payableAmount?.toFixed(2) || '0.00'}
                </div>
                ${result.reasons && result.reasons.length > 0 ? `
                    <div class="result-item">
                        <strong>Reasons:</strong>
                        <ul style="margin-top: 8px; margin-left: 20px;">
                            ${result.reasons.map(r => `<li>${r}</li>`).join('')}
                        </ul>
                    </div>
                ` : ''}
            </div>

            ${itemizedHtml}

            ${result.letter ? `
                <div class="letter-section">
                    <div class="letter-title">Decision Letter</div>
                    <div class="letter-content">${result.letter}</div>
                </div>
            ` : ''}
        `;

        this.resultSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    private generateItemizedDecisionsHtml(itemizedDecisions?: ItemizedDecision[]): string {
        if (!itemizedDecisions || itemizedDecisions.length === 0) {
            return '';
        }

        return `
            <div class="itemized-decisions">
                <h3>Itemized Decisions</h3>
                ${itemizedDecisions.map(item => `
                    <div class="decision-item ${item.covered ? 'covered' : 'not-covered'}">
                        <strong>${item.service}</strong> - ₹${item.amount?.toFixed(2) || '0.00'}
                        <br>
                        <span style="font-size: 14px; color: #666;">
                            ${item.covered ? 
                                `✓ Covered ${item.coPayment ? `(Co-pay: ₹${item.coPayment.toFixed(2)})` : ''}` : 
                                `✗ ${item.reason || 'Not covered'}`
                            }
                        </span>
                    </div>
                `).join('')}
            </div>
        `;
    }

    private resetPolicyForm(): void {
        const policyForm = document.getElementById('policyForm') as HTMLFormElement;
        if (policyForm) {
            policyForm.reset();
        }
        
        // Clear file input
        const policyFileInput = document.getElementById('policyFileUpload') as HTMLInputElement;
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

    private showLoader(): void {
        this.loader.style.display = 'flex';
    }

    private hideLoader(): void {
        this.loader.style.display = 'none';
    }

    private showSuccessMessage(message: string): void {
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

    private showErrorMessage(message: string): void {
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

    // Added utility function for dynamic content loading
    private async loadDynamicContent(url: string, targetElementId: string): Promise<void> {
        try {
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const content = await response.text();
            const targetElement = document.getElementById(targetElementId);
            if (targetElement) {
                targetElement.innerHTML = content;
            } else {
                throw new Error(`Target element with ID '${targetElementId}' not found.`);
            }
        } catch (error) {
            this.showErrorMessage(`Failed to load content: ${error.message}`);
        }
    }
}

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new ClaimUnderwriterApp();
});
