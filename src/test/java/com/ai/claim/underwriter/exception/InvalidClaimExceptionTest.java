package com.ai.claim.underwriter.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for InvalidClaimException
 */
class InvalidClaimExceptionTest {

    @Test
    void constructor_withMessage_setsMessageAndErrorCode() {
        // Arrange
        String message = "Claim is invalid";
        
        // Act
        InvalidClaimException exception = new InvalidClaimException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_CLAIM", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_withMessageAndCause_setsMessageErrorCodeAndCause() {
        // Arrange
        String message = "Claim is invalid";
        Throwable cause = new IllegalArgumentException("Invalid data");
        
        // Act
        InvalidClaimException exception = new InvalidClaimException(message, cause);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_CLAIM", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_withMessageAndErrorDetails_setsMessageErrorCodeAndDetails() {
        // Arrange
        String message = "Claim is invalid";
        String errorDetails = "Missing required fields";
        
        // Act
        InvalidClaimException exception = new InvalidClaimException(message, errorDetails);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("INVALID_CLAIM", exception.getErrorCode());
        assertEquals(errorDetails, exception.getErrorDetails());
    }

    @Test
    void extendsClaimUnderwriterException() {
        // Arrange & Act
        InvalidClaimException exception = new InvalidClaimException("test");
        
        // Assert
        assertTrue(exception instanceof ClaimUnderwriterException);
    }
}
