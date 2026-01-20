package com.ai.claim.underwriter.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ClaimUnderwriterException
 */
class ClaimUnderwriterExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        // Arrange
        String message = "Error occurred";
        
        // Act
        ClaimUnderwriterException exception = new ClaimUnderwriterException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertNull(exception.getErrorCode());
        assertNull(exception.getErrorDetails());
    }

    @Test
    void constructor_withMessageAndCause_setsBoth() {
        // Arrange
        String message = "Error occurred";
        Throwable cause = new RuntimeException("root cause");
        
        // Act
        ClaimUnderwriterException exception = new ClaimUnderwriterException(message, cause);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_withMessageAndErrorCode_setsBoth() {
        // Arrange
        String message = "Error occurred";
        String errorCode = "ERR_001";
        
        // Act
        ClaimUnderwriterException exception = new ClaimUnderwriterException(message, errorCode);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }

    @Test
    void constructor_withMessageErrorCodeAndCause_setsAll() {
        // Arrange
        String message = "Error occurred";
        String errorCode = "ERR_001";
        Throwable cause = new IllegalStateException();
        
        // Act
        ClaimUnderwriterException exception = new ClaimUnderwriterException(message, errorCode, cause);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_withMessageErrorCodeAndDetails_setsAll() {
        // Arrange
        String message = "Error occurred";
        String errorCode = "ERR_001";
        String errorDetails = "Additional details";
        
        // Act
        ClaimUnderwriterException exception = new ClaimUnderwriterException(message, errorCode, errorDetails);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorDetails, exception.getErrorDetails());
    }

    @Test
    void constructor_withAllParameters_setsAllProperties() {
        // Arrange
        String message = "Error occurred";
        String errorCode = "ERR_001";
        String errorDetails = "Additional details";
        Throwable cause = new IllegalArgumentException();
        
        // Act
        ClaimUnderwriterException exception = new ClaimUnderwriterException(message, errorCode, errorDetails, cause);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorDetails, exception.getErrorDetails());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void setErrorCode_updatesErrorCode() {
        // Arrange
        ClaimUnderwriterException exception = new ClaimUnderwriterException("test");
        
        // Act
        exception.setErrorCode("NEW_CODE");
        
        // Assert
        assertEquals("NEW_CODE", exception.getErrorCode());
    }

    @Test
    void setErrorDetails_updatesErrorDetails() {
        // Arrange
        ClaimUnderwriterException exception = new ClaimUnderwriterException("test");
        Object details = new Object();
        
        // Act
        exception.setErrorDetails(details);
        
        // Assert
        assertEquals(details, exception.getErrorDetails());
    }

    @Test
    void toString_withMinimalData_includesMessage() {
        // Arrange
        ClaimUnderwriterException exception = new ClaimUnderwriterException("Test error");
        
        // Act
        String result = exception.toString();
        
        // Assert
        assertTrue(result.contains("Test error"));
        assertTrue(result.contains("ClaimUnderwriterException"));
    }

    @Test
    void toString_withAllData_includesAllFields() {
        // Arrange
        ClaimUnderwriterException exception = new ClaimUnderwriterException("Test error", "ERR_001", "details");
        
        // Act
        String result = exception.toString();
        
        // Assert
        assertTrue(result.contains("Test error"));
        assertTrue(result.contains("ERR_001"));
        assertTrue(result.contains("details"));
    }

    @Test
    void extendsRuntimeException() {
        // Arrange & Act
        ClaimUnderwriterException exception = new ClaimUnderwriterException("test");
        
        // Assert
        assertTrue(exception instanceof RuntimeException);
    }
}
