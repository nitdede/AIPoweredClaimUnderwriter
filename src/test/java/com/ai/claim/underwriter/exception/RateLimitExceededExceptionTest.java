package com.ai.claim.underwriter.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RateLimitExceededException
 */
class RateLimitExceededExceptionTest {

    @Test
    void constructor_withMessage_setsMessageAndErrorCode() {
        // Arrange
        String message = "Rate limit exceeded";
        
        // Act
        RateLimitExceededException exception = new RateLimitExceededException(message);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
    }

    @Test
    void constructor_withClientIdAndMaxRequests_createsDetailedMessage() {
        // Arrange
        String clientId = "client123";
        int maxRequests = 100;
        
        // Act
        RateLimitExceededException exception = new RateLimitExceededException(clientId, maxRequests);
        
        // Assert
        assertTrue(exception.getMessage().contains(clientId));
        assertTrue(exception.getMessage().contains("100"));
        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
        assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
    }

    @Test
    void constructor_withClientIdAndMaxRequests_setsErrorDetails() {
        // Arrange
        String clientId = "client123";
        int maxRequests = 100;
        
        // Act
        RateLimitExceededException exception = new RateLimitExceededException(clientId, maxRequests);
        
        // Assert
        assertNotNull(exception.getErrorDetails());
        assertTrue(exception.getErrorDetails() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) exception.getErrorDetails();
        assertEquals(clientId, details.get("clientId"));
        assertEquals(maxRequests, details.get("maxRequests"));
    }

    @Test
    void extendsClaimUnderwriterException() {
        // Arrange & Act
        RateLimitExceededException exception = new RateLimitExceededException("test");
        
        // Assert
        assertTrue(exception instanceof ClaimUnderwriterException);
    }
}
