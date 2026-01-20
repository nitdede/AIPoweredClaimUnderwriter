package com.ai.claim.underwriter.exception;

/**
 * Exception thrown when file processing fails
 */
public class FileProcessingException extends ClaimUnderwriterException {

    public FileProcessingException(String message) {
        super(message, "FILE_PROCESSING_ERROR");
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, "FILE_PROCESSING_ERROR", cause);
    }

    public FileProcessingException(String fileName, String fileType, Throwable cause) {
        super("Failed to process file: " + fileName, "FILE_PROCESSING_ERROR", fileType, cause);
    }
}
