package com.ai.claim.underwriter.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PolicyNotFoundException
 */
class PolicyNotFoundExceptionTest {

    @Test
    void constructor_withPolicyNumber_createsMessageWithPolicyNumber() {
        // Arrange
        String policyNumber = "POL123456";
        
        // Act
        PolicyNotFoundException exception = new PolicyNotFoundException(policyNumber);
        
        // Assert
        assertTrue(exception.getMessage().contains(policyNumber));
        assertTrue(exception.getMessage().contains("Policy not found"));
        assertEquals("POLICY_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void constructor_withCustomMessageAndPolicyNumber_setsMessageAndDetails() {
        // Arrange
        String message = "Custom error message";
        String policyNumber = "POL123456";
        
        // Act
        PolicyNotFoundException exception = new PolicyNotFoundException(message, policyNumber);
        
        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals("POLICY_NOT_FOUND", exception.getErrorCode());
        assertEquals(policyNumber, exception.getErrorDetails());
    }

    @Test
    void extendsClaimUnderwriterException() {
        // Arrange & Act
        PolicyNotFoundException exception = new PolicyNotFoundException("POL123");
        
        // Assert
        assertTrue(exception instanceof ClaimUnderwriterException);
    }
}
