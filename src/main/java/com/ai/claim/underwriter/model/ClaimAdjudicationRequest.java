package com.ai.claim.underwriter.model;

public record ClaimAdjudicationRequest(
        String patientName,
        long claimId,
        String policyNumber,
        String invoiceSummaryText,   // invoice text OR extracted summary (easy)
        int topK
) {}