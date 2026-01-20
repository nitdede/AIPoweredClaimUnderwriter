package com.ai.claim.underwriter.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ClaimProcessingException
 */
class ClaimProcessingExceptionTest {

    @Test
    void constructor_withMessage_setsMessageAndErrorCode() {
        // Arrange
        String message = "Claim processing failed";
        
        // Act
        ClaimProcessingException exception = new ClaimProcessingException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("CLAIM_PROCESSING_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_withMessageAndCause_setsMessageErrorCodeAndCause() {
        // Arrange
        String message = "Claim processing failed";
        Throwable cause = new IllegalStateException("Invalid state");
        
        // Act
        ClaimProcessingException exception = new ClaimProcessingException(message, cause);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("CLAIM_PROCESSING_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_withMessageErrorDetailsAndCause_setsAllProperties() {
        // Arrange
        String message = "Claim processing failed";
        String errorDetails = "Validation errors";
        Throwable cause = new IllegalArgumentException("Invalid argument");
        
        // Act
        ClaimProcessingException exception = new ClaimProcessingException(message, errorDetails, cause);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("CLAIM_PROCESSING_ERROR", exception.getErrorCode());
        assertEquals(errorDetails, exception.getErrorDetails());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void extendsClaimUnderwriterException() {
        // Arrange & Act
        ClaimProcessingException exception = new ClaimProcessingException("test");
        
        // Assert
        assertTrue(exception instanceof ClaimUnderwriterException);
    }

    @Test
    void toString_includesAllDetails() {
        // Arrange
        ClaimProcessingException exception = new ClaimProcessingException("Processing error", "details", null);
        
        // Act
        String result = exception.toString();
        
        // Assert
        assertTrue(result.contains("Processing error"));
        assertTrue(result.contains("CLAIM_PROCESSING_ERROR"));
        assertTrue(result.contains("details"));
    }
}
