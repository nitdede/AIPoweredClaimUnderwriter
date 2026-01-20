export interface PolicyMetaData {
    policyId: string;
    customerId: string;
    policyNumber: string;
}

export interface ExtractRequest {
    invoiceText: string;
}

export interface ItemizedDecision {
    service: string;
    amount?: number;
    covered: boolean;
    reason?: string;
    coPayment?: number;
}

export interface ClaimProcessingResult {
    status: string;
    claimId?: number | string;
    policyNumber?: string;
    decision?: string;
    payableAmount?: number;
    reasons?: string[];
    itemizedDecisions?: ItemizedDecision[];
    letter?: string;
    errorMessage?: string;
}

export interface LastClaimContext {
    claimId: string;
    policyNumber: string;
}

export interface HelpDeskPayload {
    claimId: string;
    issueDescription: string;
    customerName: string;
    policyNumber: string;
}
