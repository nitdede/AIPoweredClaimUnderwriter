package com.ai.claim.underwriter.model;

import java.util.List;

public record ClaimAdjudicationResponse(
        long claimId,
        String decision,
        Double payableAmount,
        List<String> evidenceChunks,
        String letter
) {}