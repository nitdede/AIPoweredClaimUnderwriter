package com.ai.claim.underwriter.model;

public record HelpDeskRequest(String claimId, String issueDescription, String customerName, String policyNumber) {
}
