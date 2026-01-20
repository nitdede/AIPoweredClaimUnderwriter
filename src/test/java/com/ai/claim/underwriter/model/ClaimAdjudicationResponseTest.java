package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClaimAdjudicationResponseTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        long claimId = 12345L;
        String decision = "APPROVED";
        Double payableAmount = 1500.50;
        List<String> evidenceChunks = Arrays.asList("Evidence 1", "Evidence 2");
        String letter = "Approval letter";

        // Act
        ClaimAdjudicationResponse response = new ClaimAdjudicationResponse(
                claimId, decision, payableAmount, evidenceChunks, letter
        );

        // Assert
        assertNotNull(response);
        assertEquals(claimId, response.claimId());
        assertEquals(decision, response.decision());
        assertEquals(payableAmount, response.payableAmount());
        assertEquals(evidenceChunks, response.evidenceChunks());
        assertEquals(letter, response.letter());
    }

    @Test
    void testConstructorWithNullValues() {
        // Act
        ClaimAdjudicationResponse response = new ClaimAdjudicationResponse(
                0L, null, null, null, null
        );

        // Assert
        assertNotNull(response);
        assertEquals(0L, response.claimId());
        assertNull(response.decision());
        assertNull(response.payableAmount());
        assertNull(response.evidenceChunks());
        assertNull(response.letter());
    }

    @Test
    void testConstructorWithEmptyEvidenceList() {
        // Arrange
        List<String> emptyEvidence = Collections.emptyList();

        // Act
        ClaimAdjudicationResponse response = new ClaimAdjudicationResponse(
                100L, "DENIED", 0.0, emptyEvidence, "Denial letter"
        );

        // Assert
        assertNotNull(response);
        assertTrue(response.evidenceChunks().isEmpty());
    }

    @Test
    void testEquals() {
        // Arrange
        List<String> evidence = Arrays.asList("Evidence 1");
        ClaimAdjudicationResponse response1 = new ClaimAdjudicationResponse(
                1L, "APPROVED", 1000.0, evidence, "Letter 1"
        );
        ClaimAdjudicationResponse response2 = new ClaimAdjudicationResponse(
                1L, "APPROVED", 1000.0, evidence, "Letter 1"
        );
        ClaimAdjudicationResponse response3 = new ClaimAdjudicationResponse(
                2L, "DENIED", 0.0, evidence, "Letter 2"
        );

        // Assert
        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
    }

    @Test
    void testHashCode() {
        // Arrange
        List<String> evidence = Arrays.asList("Evidence 1");
        ClaimAdjudicationResponse response1 = new ClaimAdjudicationResponse(
                1L, "APPROVED", 1000.0, evidence, "Letter 1"
        );
        ClaimAdjudicationResponse response2 = new ClaimAdjudicationResponse(
                1L, "APPROVED", 1000.0, evidence, "Letter 1"
        );

        // Assert
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        List<String> evidence = Arrays.asList("Evidence 1", "Evidence 2");
        ClaimAdjudicationResponse response = new ClaimAdjudicationResponse(
                123L, "PARTIAL", 750.0, evidence, "Partial approval letter"
        );

        // Act
        String result = response.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("123"));
        assertTrue(result.contains("PARTIAL"));
    }

    @Test
    void testPartialApprovalScenario() {
        // Arrange
        List<String> evidence = Arrays.asList(
                "Policy covers 80% of medical expenses",
                "Deductible of $500 applied"
        );

        // Act
        ClaimAdjudicationResponse response = new ClaimAdjudicationResponse(
                999L, "PARTIAL", 1200.0, evidence, "Partial approval based on policy terms"
        );

        // Assert
        assertEquals("PARTIAL", response.decision());
        assertEquals(1200.0, response.payableAmount());
        assertEquals(2, response.evidenceChunks().size());
    }

    @Test
    void testDeniedClaimScenario() {
        // Arrange
        List<String> evidence = Arrays.asList("Service not covered under policy");

        // Act
        ClaimAdjudicationResponse response = new ClaimAdjudicationResponse(
                888L, "DENIED", 0.0, evidence, "Claim denied - service not covered"
        );

        // Assert
        assertEquals("DENIED", response.decision());
        assertEquals(0.0, response.payableAmount());
        assertEquals(1, response.evidenceChunks().size());
    }
}
