package com.ai.claim.underwriter.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FileProcessingException
 */
class FileProcessingExceptionTest {

    @Test
    void constructor_withMessage_setsMessageAndErrorCode() {
        // Arrange
        String message = "File processing failed";
        
        // Act
        FileProcessingException exception = new FileProcessingException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("FILE_PROCESSING_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_withMessageAndCause_setsMessageErrorCodeAndCause() {
        // Arrange
        String message = "File processing failed";
        Throwable cause = new java.io.IOException("IO error");
        
        // Act
        FileProcessingException exception = new FileProcessingException(message, cause);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("FILE_PROCESSING_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void constructor_withFileNameAndType_createsMessageWithFileInfo() {
        // Arrange
        String fileName = "invoice.pdf";
        String fileType = "PDF";
        Throwable cause = new RuntimeException("Parse error");
        
        // Act
        FileProcessingException exception = new FileProcessingException(fileName, fileType, cause);
        
        // Assert
        assertTrue(exception.getMessage().contains(fileName));
        assertTrue(exception.getMessage().contains("Failed to process file"));
        assertEquals("FILE_PROCESSING_ERROR", exception.getErrorCode());
        assertEquals(fileType, exception.getErrorDetails());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void extendsClaimUnderwriterException() {
        // Arrange & Act
        FileProcessingException exception = new FileProcessingException("test");
        
        // Assert
        assertTrue(exception instanceof ClaimUnderwriterException);
    }
}
