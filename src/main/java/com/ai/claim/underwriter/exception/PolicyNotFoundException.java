package com.ai.claim.underwriter.exception;

/**
 * Exception thrown when a policy is not found
 */
public class PolicyNotFoundException extends ClaimUnderwriterException {

    public PolicyNotFoundException(String policyNumber) {
        super("Policy not found: " + policyNumber, "POLICY_NOT_FOUND");
    }

    public PolicyNotFoundException(String message, String policyNumber) {
        super(message, "POLICY_NOT_FOUND", policyNumber);
    }
}
