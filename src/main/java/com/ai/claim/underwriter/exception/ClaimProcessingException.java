package com.ai.claim.underwriter.exception;

/**
 * Exception thrown when claim processing fails
 */
public class ClaimProcessingException extends ClaimUnderwriterException {

    public ClaimProcessingException(String message) {
        super(message, "CLAIM_PROCESSING_ERROR");
    }

    public ClaimProcessingException(String message, Throwable cause) {
        super(message, "CLAIM_PROCESSING_ERROR", cause);
    }

    public ClaimProcessingException(String message, Object errorDetails, Throwable cause) {
        super(message, "CLAIM_PROCESSING_ERROR", errorDetails, cause);
    }
}
