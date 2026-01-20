package com.ai.claim.underwriter.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AuthenticationException
 */
class AuthenticationExceptionTest {

    @Test
    void constructor_withMessage_setsMessageAndErrorCode() {
        // Arrange
        String message = "Authentication failed";
        
        // Act
        AuthenticationException exception = new AuthenticationException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("AUTHENTICATION_FAILED", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_withMessageAndCause_setsMessageErrorCodeAndCause() {
        // Arrange
        String message = "Authentication failed";
        Throwable cause = new RuntimeException("Root cause");
        
        // Act
        AuthenticationException exception = new AuthenticationException(message, cause);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("AUTHENTICATION_FAILED", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void extendsClaimUnderwriterException() {
        // Arrange & Act
        AuthenticationException exception = new AuthenticationException("test");
        
        // Assert
        assertTrue(exception instanceof ClaimUnderwriterException);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void toString_includesMessageAndErrorCode() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Invalid credentials");
        
        // Act
        String result = exception.toString();
        
        // Assert
        assertTrue(result.contains("Invalid credentials"));
        assertTrue(result.contains("AUTHENTICATION_FAILED"));
    }
}
