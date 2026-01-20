# Exception Handling System

This document describes the comprehensive exception handling system implemented in the AI-Powered Claim Underwriter application.

## Overview

The exception handling system consists of:
1. **Custom Exception Classes** - Domain-specific exceptions
2. **Global Exception Handler** - Centralized exception handling
3. **Error Response Builder** - Utility for building detailed error responses
4. **Interceptors** - Request/response interceptors for logging, authentication, and rate limiting

## Exception Hierarchy

```
RuntimeException
└── ClaimUnderwriterException (base exception)
    ├── InvalidClaimException
    ├── PolicyNotFoundException
    ├── ClaimProcessingException
    ├── FileProcessingException
    ├── AuthenticationException
    └── RateLimitExceededException
```

## Custom Exceptions

### ClaimUnderwriterException
Base exception class for all application-specific exceptions.

**Properties:**
- `errorCode` - String identifier for the error type
- `errorDetails` - Additional context or details about the error

**Constructors:**
```java
ClaimUnderwriterException(String message)
ClaimUnderwriterException(String message, Throwable cause)
ClaimUnderwriterException(String message, String errorCode)
ClaimUnderwriterException(String message, String errorCode, Throwable cause)
ClaimUnderwriterException(String message, String errorCode, Object errorDetails)
ClaimUnderwriterException(String message, String errorCode, Object errorDetails, Throwable cause)
```

### InvalidClaimException
Thrown when claim data is invalid or doesn't meet validation requirements.

**Example:**
```java
throw new InvalidClaimException("Missing required field: patientName");
throw new InvalidClaimException("Invalid claim amount", validationErrors);
```

### PolicyNotFoundException
Thrown when a policy cannot be found in the system.

**Example:**
```java
throw new PolicyNotFoundException("POL-12345");
throw new PolicyNotFoundException("Policy has expired", "POL-12345");
```

### ClaimProcessingException
Thrown when claim processing fails due to system or business logic errors.

**Example:**
```java
throw new ClaimProcessingException("Failed to process claim");
throw new ClaimProcessingException("AI service unavailable", cause);
```

### FileProcessingException
Thrown when file upload or processing fails.

**Example:**
```java
throw new FileProcessingException("Invalid file format");
throw new FileProcessingException("invoice.pdf", "PDF", cause);
```

### AuthenticationException
Thrown when authentication fails.

**Example:**
```java
throw new AuthenticationException("Invalid API key");
throw new AuthenticationException("Token expired", cause);
```

### RateLimitExceededException
Thrown when rate limits are exceeded.

**Example:**
```java
throw new RateLimitExceededException("Too many requests");
throw new RateLimitExceededException("client-123", 100);
```

## Global Exception Handler

The `GlobalExceptionHandler` class catches all exceptions and returns standardized error responses.

### Error Response Format

All errors return a JSON response with the following structure:

```json
{
  "timestamp": "2026-01-27T10:30:00",
  "status": 400,
  "errorCode": "INVALID_CLAIM",
  "message": "Missing required field: patientName",
  "details": {...},
  "path": "/claims/process-claim"
}
```

### HTTP Status Codes

| Exception | HTTP Status | Error Code |
|-----------|-------------|------------|
| InvalidClaimException | 400 BAD_REQUEST | INVALID_CLAIM |
| PolicyNotFoundException | 404 NOT_FOUND | POLICY_NOT_FOUND |
| ClaimProcessingException | 500 INTERNAL_SERVER_ERROR | CLAIM_PROCESSING_ERROR |
| FileProcessingException | 400 BAD_REQUEST | FILE_PROCESSING_ERROR |
| AuthenticationException | 401 UNAUTHORIZED | AUTHENTICATION_FAILED |
| RateLimitExceededException | 429 TOO_MANY_REQUESTS | RATE_LIMIT_EXCEEDED |
| IllegalArgumentException | 400 BAD_REQUEST | INVALID_ARGUMENT |
| MaxUploadSizeExceededException | 413 PAYLOAD_TOO_LARGE | FILE_SIZE_EXCEEDED |

## Error Response Builder

Utility class for building detailed error responses with validation errors.

**Example:**
```java
ErrorResponse response = ErrorResponseBuilder.builder()
    .status(400)
    .errorCode("VALIDATION_ERROR")
    .message("Invalid claim data")
    .path("/claims/process")
    .addValidationError("patientName", "must not be null")
    .addValidationError("claimAmount", "must be greater than 0", -100)
    .build();
```

## Usage Examples

### In Controllers

```java
@PostMapping("/process-claim")
public ClaimProcessingResult processClaimFile(
    @RequestPart("file") MultipartFile file,
    @RequestParam String policyNumber,
    @RequestParam String patientName) {
    
    // Validate input
    if (patientName == null || patientName.trim().isEmpty()) {
        throw new InvalidClaimException("Patient name is required");
    }
    
    // Check file type
    if (!allowedTypes.contains(file.getContentType())) {
        throw new FileProcessingException(
            "Unsupported file type: " + file.getContentType());
    }
    
    // Process claim
    try {
        return claimService.processClaim(file, policyNumber, patientName);
    } catch (Exception e) {
        throw new ClaimProcessingException(
            "Failed to process claim", e);
    }
}
```

### In Services

```java
public Policy findPolicy(String policyNumber) {
    return policyRepository.findByPolicyNumber(policyNumber)
        .orElseThrow(() -> new PolicyNotFoundException(policyNumber));
}

public ClaimProcessingResult processClaim(ExtractRequest request, String policyNumber) {
    Policy policy = findPolicy(policyNumber);
    
    if (!policy.isActive()) {
        throw new InvalidClaimException(
            "Policy is not active",
            Map.of("policyNumber", policyNumber, "status", policy.getStatus()));
    }
    
    try {
        return performClaimProcessing(request, policy);
    } catch (AIServiceException e) {
        throw new ClaimProcessingException(
            "AI service failed during claim processing",
            Map.of("policyNumber", policyNumber),
            e);
    }
}
```

## Logging

All exceptions are automatically logged by the GlobalExceptionHandler:
- **ERROR level**: Unexpected errors, processing failures
- **WARN level**: Rate limiting, authentication failures
- **DEBUG level**: Validation errors, bad requests

## Configuration

Configure security settings in `application.yml`:

```yaml
app:
  security:
    enabled: false  # Set to true to enable authentication
    api-key: your-secret-api-key-here

logging:
  level:
    com.ai.claim.underwriter.interceptor: debug
    com.ai.claim.underwriter.exception: debug
```

## Best Practices

1. **Use specific exceptions** - Choose the most appropriate exception type
2. **Include context** - Add error details to help with debugging
3. **Don't expose sensitive data** - Be careful what you include in error messages
4. **Log appropriately** - Important errors should be logged at ERROR level
5. **Provide actionable messages** - Help users understand what went wrong and how to fix it

## Testing

Example test for exception handling:

```java
@Test
void shouldReturnBadRequestForInvalidClaim() {
    // Arrange
    ExtractRequest invalidRequest = new ExtractRequest();
    
    // Act & Assert
    assertThrows(InvalidClaimException.class, () -> {
        claimService.processClaim(invalidRequest, "POL-123", null);
    });
}

@Test
void shouldReturnNotFoundForMissingPolicy() {
    // Act & Assert
    assertThrows(PolicyNotFoundException.class, () -> {
        policyService.findPolicy("INVALID-POLICY");
    });
}
```
