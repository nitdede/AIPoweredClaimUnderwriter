package com.ai.claim.underwriter.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for building detailed error responses
 */
public class ErrorResponseBuilder {

    private int status;
    private String errorCode;
    private String message;
    private Object details;
    private String path;
    private List<ValidationError> validationErrors;

    public ErrorResponseBuilder() {
        this.validationErrors = new ArrayList<>();
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public ErrorResponseBuilder status(int status) {
        this.status = status;
        return this;
    }

    public ErrorResponseBuilder errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public ErrorResponseBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ErrorResponseBuilder details(Object details) {
        this.details = details;
        return this;
    }

    public ErrorResponseBuilder path(String path) {
        this.path = path;
        return this;
    }

    public ErrorResponseBuilder addValidationError(String field, String errorMessage) {
        this.validationErrors.add(new ValidationError(field, errorMessage));
        return this;
    }

    public ErrorResponseBuilder addValidationError(String field, String errorMessage, Object rejectedValue) {
        this.validationErrors.add(new ValidationError(field, errorMessage, rejectedValue));
        return this;
    }

    public GlobalExceptionHandler.ErrorResponse build() {
        Object finalDetails = details;
        if (!validationErrors.isEmpty()) {
            if (details == null) {
                finalDetails = validationErrors;
            } else {
                Map<String, Object> combinedDetails = new HashMap<>();
                combinedDetails.put("details", details);
                combinedDetails.put("validationErrors", validationErrors);
                finalDetails = combinedDetails;
            }
        }
        return new GlobalExceptionHandler.ErrorResponse(status, errorCode, message, finalDetails, path);
    }

    /**
     * Validation error model
     */
    public static class ValidationError {
        private final String field;
        private final String message;
        private final Object rejectedValue;

        public ValidationError(String field, String message) {
            this(field, message, null);
        }

        public ValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }
    }
}
