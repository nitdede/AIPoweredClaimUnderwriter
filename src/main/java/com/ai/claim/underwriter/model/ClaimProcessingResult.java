package com.ai.claim.underwriter.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Final result of the claim processing pipeline.
 * Contains both the status and the claim decision details.
 */
public record ClaimProcessingResult(
        String status,           // "success" or "error"
        Long claimId,           // ID of the claim in the database
        String policyNumber,    // Policy number associated with the claim
        String decision,        // "APPROVED", "DENIED", or "PARTIAL"
        Double payableAmount,   // Amount to be paid based on policy
        List<String> reasons,   // Policy-based reasons for the decision
        JsonNode itemizedDecisions, // Service-level coverage decisions
        String letter,          // Explanation letter referencing policy terms
        String errorMessage     // Error message if status is "error"
) {
    // Factory method for success
    public static ClaimProcessingResult success(
            Long claimId,
            String policyNumber,
            String decision,
            Double payableAmount,
            List<String> reasons,
            JsonNode itemizedDecisions,
            String letter
    ) {
        return new ClaimProcessingResult("success", claimId, policyNumber, decision, payableAmount, reasons, itemizedDecisions, letter, null);
    }
    
    // Factory method for error
    public static ClaimProcessingResult error(String errorMessage) {
        return new ClaimProcessingResult("error", null, null, null, null, null, null, null, errorMessage);
    }
}
