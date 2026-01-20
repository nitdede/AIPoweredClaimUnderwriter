package com.ai.claim.underwriter.exception;

/**
 * Exception thrown when authentication fails
 */
public class AuthenticationException extends ClaimUnderwriterException {

    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_FAILED", cause);
    }
}
