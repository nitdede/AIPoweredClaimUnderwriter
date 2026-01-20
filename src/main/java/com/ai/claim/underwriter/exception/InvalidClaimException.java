package com.ai.claim.underwriter.exception;

/**
 * Exception thrown when claim data is invalid
 */
public class InvalidClaimException extends ClaimUnderwriterException {

    public InvalidClaimException(String message) {
        super(message, "INVALID_CLAIM");
    }

    public InvalidClaimException(String message, Throwable cause) {
        super(message, "INVALID_CLAIM", cause);
    }

    public InvalidClaimException(String message, Object errorDetails) {
        super(message, "INVALID_CLAIM", errorDetails);
    }
}
