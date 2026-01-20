package com.ai.claim.underwriter.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for ErrorResponseBuilder
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ErrorResponseBuilder Tests")
class ErrorResponseBuilderTest {

    private ErrorResponseBuilder builder;

    @BeforeEach
    void setUp() {
        builder = ErrorResponseBuilder.builder();
    }

    @Test
    @DisplayName("Should create builder instance")
    void testBuilderCreation() {
        // Act
        ErrorResponseBuilder newBuilder = ErrorResponseBuilder.builder();

        // Assert
        assertThat(newBuilder).isNotNull();
    }

    @Test
    @DisplayName("Should build error response with basic fields")
    void testBuildWithBasicFields() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(400)
                .errorCode("BAD_REQUEST")
                .message("Invalid request")
                .path("/api/claims")
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getErrorCode()).isEqualTo("BAD_REQUEST");
        assertThat(response.getMessage()).isEqualTo("Invalid request");
        assertThat(response.getPath()).isEqualTo("/api/claims");
    }

    @Test
    @DisplayName("Should build error response with details")
    void testBuildWithDetails() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(404)
                .errorCode("NOT_FOUND")
                .message("Resource not found")
                .details("Policy ID: 12345 not found")
                .path("/api/policy/12345")
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDetails()).isEqualTo("Policy ID: 12345 not found");
    }

    @Test
    @DisplayName("Should add single validation error")
    void testAddSingleValidationError() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed")
                .addValidationError("email", "Email is required")
                .path("/api/claims")
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDetails()).isInstanceOf(List.class);
        
        @SuppressWarnings("unchecked")
        List<ErrorResponseBuilder.ValidationError> errors = (List<ErrorResponseBuilder.ValidationError>) response.getDetails();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getField()).isEqualTo("email");
        assertThat(errors.get(0).getMessage()).isEqualTo("Email is required");
    }

    @Test
    @DisplayName("Should add multiple validation errors")
    void testAddMultipleValidationErrors() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message("Multiple validation errors")
                .addValidationError("email", "Email is required")
                .addValidationError("name", "Name cannot be empty")
                .addValidationError("age", "Age must be positive")
                .path("/api/claims")
                .build();

        // Assert
        assertThat(response).isNotNull();
        
        @SuppressWarnings("unchecked")
        List<ErrorResponseBuilder.ValidationError> errors = (List<ErrorResponseBuilder.ValidationError>) response.getDetails();
        assertThat(errors).hasSize(3);
    }

    @Test
    @DisplayName("Should add validation error with rejected value")
    void testAddValidationErrorWithRejectedValue() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message("Invalid age")
                .addValidationError("age", "Age must be positive", -5)
                .path("/api/claims")
                .build();

        // Assert
        assertThat(response).isNotNull();
        
        @SuppressWarnings("unchecked")
        List<ErrorResponseBuilder.ValidationError> errors = (List<ErrorResponseBuilder.ValidationError>) response.getDetails();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getRejectedValue()).isEqualTo(-5);
    }

    @Test
    @DisplayName("Should combine details and validation errors")
    void testCombineDetailsAndValidationErrors() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .details("Additional context information")
                .addValidationError("field1", "Error 1")
                .addValidationError("field2", "Error 2")
                .path("/api/claims")
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDetails()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> combinedDetails = (Map<String, Object>) response.getDetails();
        assertThat(combinedDetails).containsKey("details");
        assertThat(combinedDetails).containsKey("validationErrors");
        assertThat(combinedDetails.get("details")).isEqualTo("Additional context information");
    }

    @Test
    @DisplayName("Should build response without details")
    void testBuildWithoutDetails() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(500)
                .errorCode("INTERNAL_ERROR")
                .message("Internal server error")
                .path("/api/claims")
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDetails()).isNull();
    }

    @Test
    @DisplayName("Should set timestamp automatically")
    void testTimestampAutoSet() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(400)
                .errorCode("ERROR")
                .message("Error message")
                .path("/api/test")
                .build();

        // Assert
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should support method chaining")
    void testMethodChaining() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = ErrorResponseBuilder.builder()
                .status(400)
                .errorCode("ERROR")
                .message("Message")
                .details("Details")
                .path("/path")
                .addValidationError("field", "error")
                .build();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should create ValidationError with field and message")
    void testValidationErrorCreation() {
        // Arrange & Act
        ErrorResponseBuilder.ValidationError error = new ErrorResponseBuilder.ValidationError("email", "Required");

        // Assert
        assertThat(error.getField()).isEqualTo("email");
        assertThat(error.getMessage()).isEqualTo("Required");
        assertThat(error.getRejectedValue()).isNull();
    }

    @Test
    @DisplayName("Should create ValidationError with field, message and rejected value")
    void testValidationErrorCreationWithRejectedValue() {
        // Arrange & Act
        ErrorResponseBuilder.ValidationError error = new ErrorResponseBuilder.ValidationError(
                "age", "Must be positive", -10
        );

        // Assert
        assertThat(error.getField()).isEqualTo("age");
        assertThat(error.getMessage()).isEqualTo("Must be positive");
        assertThat(error.getRejectedValue()).isEqualTo(-10);
    }

    @Test
    @DisplayName("Should build different error codes")
    void testBuildWithDifferentErrorCodes() {
        // Test various error codes
        String[] errorCodes = {"NOT_FOUND", "UNAUTHORIZED", "FORBIDDEN", "INTERNAL_ERROR"};

        for (String errorCode : errorCodes) {
            // Arrange & Act
            GlobalExceptionHandler.ErrorResponse response = ErrorResponseBuilder.builder()
                    .status(400)
                    .errorCode(errorCode)
                    .message("Test message")
                    .path("/test")
                    .build();

            // Assert
            assertThat(response.getErrorCode()).isEqualTo(errorCode);
        }
    }

    @Test
    @DisplayName("Should build with various status codes")
    void testBuildWithVariousStatusCodes() {
        // Test various HTTP status codes
        int[] statusCodes = {400, 401, 403, 404, 500, 503};

        for (int statusCode : statusCodes) {
            // Arrange & Act
            GlobalExceptionHandler.ErrorResponse response = ErrorResponseBuilder.builder()
                    .status(statusCode)
                    .errorCode("ERROR")
                    .message("Test")
                    .path("/test")
                    .build();

            // Assert
            assertThat(response.getStatus()).isEqualTo(statusCode);
        }
    }

    @Test
    @DisplayName("Should handle complex object as details")
    void testBuildWithComplexObjectDetails() {
        // Arrange
        Map<String, Object> complexDetails = Map.of(
                "errorType", "validation",
                "errorCount", 3,
                "severity", "high"
        );

        // Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message("Complex validation error")
                .details(complexDetails)
                .path("/api/claims")
                .build();

        // Assert
        assertThat(response.getDetails()).isEqualTo(complexDetails);
    }

    @Test
    @DisplayName("Should build empty validation errors list when no errors added")
    void testBuildWithNoValidationErrors() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse response = builder
                .status(400)
                .errorCode("ERROR")
                .message("Error")
                .path("/test")
                .build();

        // Assert
        assertThat(response.getDetails()).isNull();
    }
}
