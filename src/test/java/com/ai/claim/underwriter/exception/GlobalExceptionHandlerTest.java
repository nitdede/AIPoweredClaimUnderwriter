package com.ai.claim.underwriter.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * Comprehensive unit tests for GlobalExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        lenient().when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("Should handle InvalidClaimException")
    void testHandleInvalidClaimException() {
        // Arrange
        InvalidClaimException exception = new InvalidClaimException("Invalid claim data");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleInvalidClaimException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid claim data");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("Should handle PolicyNotFoundException")
    void testHandlePolicyNotFoundException() {
        // Arrange
        PolicyNotFoundException exception = new PolicyNotFoundException("Policy not found");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handlePolicyNotFoundException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("Policy not found");
    }

    @Test
    @DisplayName("Should handle ClaimProcessingException")
    void testHandleClaimProcessingException() {
        // Arrange
        ClaimProcessingException exception = new ClaimProcessingException("Processing failed");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleClaimProcessingException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Processing failed");
    }

    @Test
    @DisplayName("Should handle FileProcessingException")
    void testHandleFileProcessingException() {
        // Arrange
        FileProcessingException exception = new FileProcessingException("File upload failed");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleFileProcessingException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("File upload failed");
    }

    @Test
    @DisplayName("Should handle MaxUploadSizeExceededException")
    void testHandleMaxUploadSizeExceededException() {
        // Arrange
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(10000000);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleMaxUploadSizeExceededException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(413);
        assertThat(response.getBody().getErrorCode()).isEqualTo("FILE_SIZE_EXCEEDED");
        assertThat(response.getBody().getMessage()).contains("exceeds the maximum allowed limit");
    }

    @Test
    @DisplayName("Should handle AuthenticationException")
    void testHandleAuthenticationException() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Authentication failed");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).isEqualTo("Authentication failed");
    }

    @Test
    @DisplayName("Should handle RateLimitExceededException with Retry-After header")
    void testHandleRateLimitExceededException() {
        // Arrange
        RateLimitExceededException exception = new RateLimitExceededException("Rate limit exceeded");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleRateLimitExceededException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(429);
        assertThat(response.getHeaders().get("Retry-After")).contains("60");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleIllegalArgumentException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_ARGUMENT");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid argument");
    }

    @Test
    @DisplayName("Should handle generic ClaimUnderwriterException")
    void testHandleClaimUnderwriterException() {
        // Arrange
        ClaimUnderwriterException exception = new ClaimUnderwriterException("Generic error");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleClaimUnderwriterException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("Generic error");
    }

    @Test
    @DisplayName("Should handle RuntimeException")
    void testHandleRuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Runtime error");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleRuntimeException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getErrorCode()).isEqualTo("RUNTIME_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Runtime error");
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void testHandleGlobalException() {
        // Arrange
        Exception exception = new Exception("Unexpected error");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    @DisplayName("Should include error details when present in exception")
    void testHandleExceptionWithDetails() {
        // Arrange
        Map<String, Object> details = Map.of("field", "email", "constraint", "required");
        InvalidClaimException exception = new InvalidClaimException("Validation failed", details);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleInvalidClaimException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("Should use error code from exception when available")
    void testHandleExceptionWithErrorCode() {
        // Arrange
        ClaimUnderwriterException exception = new ClaimUnderwriterException(
                "Custom error message", "CUSTOM_ERROR", null
        );

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleClaimUnderwriterException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CUSTOM_ERROR");
    }

    @Test
    @DisplayName("Should use default error code when exception has no error code")
    void testHandleExceptionWithoutErrorCode() {
        // Arrange
        ClaimUnderwriterException exception = new ClaimUnderwriterException("Error without code");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleClaimUnderwriterException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    @DisplayName("ErrorResponse should convert to map correctly")
    void testErrorResponseToMap() {
        // Arrange
        GlobalExceptionHandler.ErrorResponse errorResponse = 
                new GlobalExceptionHandler.ErrorResponse(
                        400, 
                        "BAD_REQUEST", 
                        "Invalid input", 
                        "Additional details", 
                        "/api/test"
                );

        // Act
        Map<String, Object> map = errorResponse.toMap();

        // Assert
        assertThat(map).containsKeys("timestamp", "status", "errorCode", "message", "details", "path");
        assertThat(map.get("status")).isEqualTo(400);
        assertThat(map.get("errorCode")).isEqualTo("BAD_REQUEST");
        assertThat(map.get("message")).isEqualTo("Invalid input");
        assertThat(map.get("details")).isEqualTo("Additional details");
        assertThat(map.get("path")).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("ErrorResponse should exclude null details from map")
    void testErrorResponseToMapWithNullDetails() {
        // Arrange
        GlobalExceptionHandler.ErrorResponse errorResponse = 
                new GlobalExceptionHandler.ErrorResponse(
                        400, 
                        "BAD_REQUEST", 
                        "Invalid input", 
                        null, 
                        "/api/test"
                );

        // Act
        Map<String, Object> map = errorResponse.toMap();

        // Assert
        assertThat(map).doesNotContainKey("details");
    }

    @Test
    @DisplayName("Should extract path from WebRequest correctly")
    void testPathExtraction() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/claims/12345");
        InvalidClaimException exception = new InvalidClaimException("Test");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleInvalidClaimException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/claims/12345");
    }

    @Test
    @DisplayName("Should handle exceptions with null messages")
    void testHandleExceptionWithNullMessage() {
        // Arrange
        RuntimeException exception = new RuntimeException();

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
                globalExceptionHandler.handleRuntimeException(exception, webRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("ErrorResponse getters should work correctly")
    void testErrorResponseGetters() {
        // Arrange
        GlobalExceptionHandler.ErrorResponse errorResponse = 
                new GlobalExceptionHandler.ErrorResponse(
                        404, 
                        "NOT_FOUND", 
                        "Resource not found", 
                        "Policy 123 not found", 
                        "/api/policy/123"
                );

        // Assert
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getErrorCode()).isEqualTo("NOT_FOUND");
        assertThat(errorResponse.getMessage()).isEqualTo("Resource not found");
        assertThat(errorResponse.getDetails()).isEqualTo("Policy 123 not found");
        assertThat(errorResponse.getPath()).isEqualTo("/api/policy/123");
    }
}
