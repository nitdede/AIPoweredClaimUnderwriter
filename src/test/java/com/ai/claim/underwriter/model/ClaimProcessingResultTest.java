package com.ai.claim.underwriter.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClaimProcessingResultTest {

    private ObjectMapper objectMapper;
    private JsonNode sampleJsonNode;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        sampleJsonNode = objectMapper.readTree("{\"service\":\"Consultation\",\"covered\":true}");
    }

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String status = "success";
        Long claimId = 12345L;
        String policyNumber = "POL-123";
        String decision = "APPROVED";
        Double payableAmount = 1500.0;
        List<String> reasons = Arrays.asList("Reason 1", "Reason 2");
        String letter = "Approval letter";

        // Act
        ClaimProcessingResult result = new ClaimProcessingResult(
                status, claimId, policyNumber, decision, payableAmount,
                reasons, sampleJsonNode, letter, null
        );

        // Assert
        assertNotNull(result);
        assertEquals(status, result.status());
        assertEquals(claimId, result.claimId());
        assertEquals(policyNumber, result.policyNumber());
        assertEquals(decision, result.decision());
        assertEquals(payableAmount, result.payableAmount());
        assertEquals(reasons, result.reasons());
        assertEquals(sampleJsonNode, result.itemizedDecisions());
        assertEquals(letter, result.letter());
        assertNull(result.errorMessage());
    }

    @Test
    void testSuccessFactoryMethod() {
        // Arrange
        Long claimId = 999L;
        String policyNumber = "POL-GOLD-456";
        String decision = "APPROVED";
        Double payableAmount = 2500.75;
        List<String> reasons = Arrays.asList("Policy covers all services", "No deductible remaining");
        String letter = "Your claim has been approved";

        // Act
        ClaimProcessingResult result = ClaimProcessingResult.success(
                claimId, policyNumber, decision, payableAmount, reasons, sampleJsonNode, letter
        );

        // Assert
        assertNotNull(result);
        assertEquals("success", result.status());
        assertEquals(999L, result.claimId());
        assertEquals("POL-GOLD-456", result.policyNumber());
        assertEquals("APPROVED", result.decision());
        assertEquals(2500.75, result.payableAmount());
        assertEquals(2, result.reasons().size());
        assertEquals("Your claim has been approved", result.letter());
        assertNull(result.errorMessage());
    }

    @Test
    void testErrorFactoryMethod() {
        // Arrange
        String errorMessage = "Invalid policy number";

        // Act
        ClaimProcessingResult result = ClaimProcessingResult.error(errorMessage);

        // Assert
        assertNotNull(result);
        assertEquals("error", result.status());
        assertNull(result.claimId());
        assertNull(result.policyNumber());
        assertNull(result.decision());
        assertNull(result.payableAmount());
        assertNull(result.reasons());
        assertNull(result.itemizedDecisions());
        assertNull(result.letter());
        assertEquals(errorMessage, result.errorMessage());
    }

    @Test
    void testErrorFactoryMethodWithNullMessage() {
        // Act
        ClaimProcessingResult result = ClaimProcessingResult.error(null);

        // Assert
        assertNotNull(result);
        assertEquals("error", result.status());
        assertNull(result.errorMessage());
    }

    @Test
    void testSuccessWithNullValues() {
        // Act
        ClaimProcessingResult result = ClaimProcessingResult.success(
                null, null, null, null, null, null, null
        );

        // Assert
        assertEquals("success", result.status());
        assertNull(result.claimId());
        assertNull(result.policyNumber());
        assertNull(result.decision());
    }

    @Test
    void testApprovedClaimScenario() {
        // Arrange
        List<String> reasons = Arrays.asList(
                "All services are covered under policy",
                "Deductible already met for this year"
        );

        // Act
        ClaimProcessingResult result = ClaimProcessingResult.success(
                100L, "POL-PREMIUM-789", "APPROVED", 5000.0, reasons, sampleJsonNode,
                "Your claim is fully approved. Payment will be processed within 5 business days."
        );

        // Assert
        assertEquals("success", result.status());
        assertEquals("APPROVED", result.decision());
        assertEquals(5000.0, result.payableAmount());
        assertTrue(result.reasons().contains("All services are covered under policy"));
    }

    @Test
    void testPartialApprovalScenario() {
        // Arrange
        List<String> reasons = Arrays.asList(
                "Some services not covered",
                "80% coverage applied as per policy"
        );

        // Act
        ClaimProcessingResult result = ClaimProcessingResult.success(
                200L, "POL-STANDARD-456", "PARTIAL", 1600.0, reasons, sampleJsonNode,
                "Your claim is partially approved. $1600 will be paid."
        );

        // Assert
        assertEquals("success", result.status());
        assertEquals("PARTIAL", result.decision());
        assertEquals(1600.0, result.payableAmount());
        assertEquals(2, result.reasons().size());
    }

    @Test
    void testDeniedClaimScenario() {
        // Arrange
        List<String> reasons = Collections.singletonList("Service not covered under current policy");

        // Act
        ClaimProcessingResult result = ClaimProcessingResult.success(
                300L, "POL-BASIC-123", "DENIED", 0.0, reasons, sampleJsonNode,
                "Your claim has been denied as the service is not covered."
        );

        // Assert
        assertEquals("success", result.status());
        assertEquals("DENIED", result.decision());
        assertEquals(0.0, result.payableAmount());
    }

    @Test
    void testErrorScenarios() {
        // Test various error scenarios
        ClaimProcessingResult error1 = ClaimProcessingResult.error("Policy not found");
        ClaimProcessingResult error2 = ClaimProcessingResult.error("Database connection failed");
        ClaimProcessingResult error3 = ClaimProcessingResult.error("Invalid claim data");

        // Assert
        assertEquals("error", error1.status());
        assertEquals("Policy not found", error1.errorMessage());
        assertEquals("error", error2.status());
        assertEquals("Database connection failed", error2.errorMessage());
        assertEquals("error", error3.status());
        assertEquals("Invalid claim data", error3.errorMessage());
    }

    @Test
    void testEquals() {
        // Arrange
        List<String> reasons = Arrays.asList("Reason 1");
        ClaimProcessingResult result1 = ClaimProcessingResult.success(
                1L, "POL-1", "APPROVED", 100.0, reasons, sampleJsonNode, "Letter 1"
        );
        ClaimProcessingResult result2 = ClaimProcessingResult.success(
                1L, "POL-1", "APPROVED", 100.0, reasons, sampleJsonNode, "Letter 1"
        );
        ClaimProcessingResult result3 = ClaimProcessingResult.success(
                2L, "POL-2", "DENIED", 0.0, reasons, sampleJsonNode, "Letter 2"
        );

        // Assert
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
    }

    @Test
    void testHashCode() {
        // Arrange
        List<String> reasons = Arrays.asList("Reason 1");
        ClaimProcessingResult result1 = ClaimProcessingResult.success(
                1L, "POL-1", "APPROVED", 100.0, reasons, sampleJsonNode, "Letter 1"
        );
        ClaimProcessingResult result2 = ClaimProcessingResult.success(
                1L, "POL-1", "APPROVED", 100.0, reasons, sampleJsonNode, "Letter 1"
        );

        // Assert
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        ClaimProcessingResult result = ClaimProcessingResult.success(
                123L, "POL-999", "APPROVED", 1000.0,
                Collections.singletonList("Test reason"), sampleJsonNode, "Test letter"
        );

        // Act
        String resultString = result.toString();

        // Assert
        assertNotNull(resultString);
        assertTrue(resultString.contains("success"));
        assertTrue(resultString.contains("123"));
    }

    @Test
    void testCompleteSuccessResult() throws Exception {
        // Arrange
        JsonNode itemizedDecisions = objectMapper.readTree(
                "{\"consultation\":{\"covered\":true,\"amount\":500},\"lab\":{\"covered\":true,\"amount\":750}}"
        );
        List<String> reasons = Arrays.asList(
                "All services covered",
                "Within annual limit",
                "No pre-authorization required"
        );

        // Act
        ClaimProcessingResult result = ClaimProcessingResult.success(
                98765L,
                "POL-PLATINUM-2024",
                "APPROVED",
                1250.0,
                reasons,
                itemizedDecisions,
                "Dear valued customer, your claim has been fully approved for payment of $1250."
        );

        // Assert
        assertEquals("success", result.status());
        assertEquals(98765L, result.claimId());
        assertEquals("POL-PLATINUM-2024", result.policyNumber());
        assertEquals("APPROVED", result.decision());
        assertEquals(1250.0, result.payableAmount());
        assertEquals(3, result.reasons().size());
        assertNotNull(result.itemizedDecisions());
        assertTrue(result.letter().contains("fully approved"));
        assertNull(result.errorMessage());
    }
}
