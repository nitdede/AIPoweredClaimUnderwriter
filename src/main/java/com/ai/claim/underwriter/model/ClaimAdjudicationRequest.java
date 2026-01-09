package com.ai.claim.underwriter.model;

public record ClaimAdjudicationRequest(
        long claimId,
        String invoiceSummaryText,   // invoice text OR extracted summary (easy)
        int topK
) {}