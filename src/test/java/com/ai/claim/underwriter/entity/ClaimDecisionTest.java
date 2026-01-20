package com.ai.claim.underwriter.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClaimDecisionTest {

    private ClaimDecision claimDecision;

    @BeforeEach
    void setUp() {
        claimDecision = new ClaimDecision();
    }

    @Test
    void testDefaultConstructor() {
        // Assert
        assertNotNull(claimDecision);
        assertNull(claimDecision.getId());
        assertNull(claimDecision.getClaimId());
        assertNull(claimDecision.getDecision());
        assertNull(claimDecision.getPayableAmount());
        assertNull(claimDecision.getReasons());
        assertNull(claimDecision.getLetter());
        assertNull(claimDecision.getCreatedAt());
        assertNotNull(claimDecision.getEvidences());
        assertTrue(claimDecision.getEvidences().isEmpty());
    }

    @Test
    void testIdGetterAndSetter() {
        // Arrange
        Long id = 12345L;

        // Act
        claimDecision.setId(id);

        // Assert
        assertEquals(id, claimDecision.getId());
    }

    @Test
    void testClaimIdGetterAndSetter() {
        // Arrange
        Long claimId = 67890L;

        // Act
        claimDecision.setClaimId(claimId);

        // Assert
        assertEquals(claimId, claimDecision.getClaimId());
    }

    @Test
    void testDecisionGetterAndSetter() {
        // Arrange
        String decision = "APPROVED";

        // Act
        claimDecision.setDecision(decision);

        // Assert
        assertEquals(decision, claimDecision.getDecision());
    }

    @Test
    void testPayableAmountGetterAndSetter() {
        // Arrange
        BigDecimal payableAmount = new BigDecimal("1500.50");

        // Act
        claimDecision.setPayableAmount(payableAmount);

        // Assert
        assertEquals(payableAmount, claimDecision.getPayableAmount());
    }

    @Test
    void testReasonsGetterAndSetter() {
        // Arrange
        String reasons = "[\"Policy covers all services\", \"No deductible remaining\"]";

        // Act
        claimDecision.setReasons(reasons);

        // Assert
        assertEquals(reasons, claimDecision.getReasons());
    }

    @Test
    void testLetterGetterAndSetter() {
        // Arrange
        String letter = "Dear customer, your claim has been approved.";

        // Act
        claimDecision.setLetter(letter);

        // Assert
        assertEquals(letter, claimDecision.getLetter());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);

        // Act
        claimDecision.setCreatedAt(createdAt);

        // Assert
        assertEquals(createdAt, claimDecision.getCreatedAt());
    }

    @Test
    void testEvidencesGetterAndSetter() {
        // Arrange
        List<ClaimDecisionEvidence> evidences = new ArrayList<>();
        ClaimDecisionEvidence evidence1 = new ClaimDecisionEvidence();
        evidence1.setChunkText("Evidence chunk 1");
        evidences.add(evidence1);

        // Act
        claimDecision.setEvidences(evidences);

        // Assert
        assertEquals(evidences, claimDecision.getEvidences());
        assertEquals(1, claimDecision.getEvidences().size());
    }

    @Test
    void testApprovedDecisionScenario() {
        // Arrange
        claimDecision.setId(1L);
        claimDecision.setClaimId(100L);
        claimDecision.setDecision("APPROVED");
        claimDecision.setPayableAmount(new BigDecimal("2500.00"));
        claimDecision.setReasons("[\"All services covered\"]");
        claimDecision.setLetter("Your claim is fully approved.");
        claimDecision.setCreatedAt(LocalDateTime.now());

        // Assert
        assertEquals("APPROVED", claimDecision.getDecision());
        assertEquals(new BigDecimal("2500.00"), claimDecision.getPayableAmount());
        assertTrue(claimDecision.getReasons().contains("All services covered"));
    }

    @Test
    void testDeniedDecisionScenario() {
        // Arrange
        claimDecision.setId(2L);
        claimDecision.setClaimId(200L);
        claimDecision.setDecision("DENIED");
        claimDecision.setPayableAmount(BigDecimal.ZERO);
        claimDecision.setReasons("[\"Service not covered\"]");
        claimDecision.setLetter("Unfortunately, your claim has been denied.");
        claimDecision.setCreatedAt(LocalDateTime.now());

        // Assert
        assertEquals("DENIED", claimDecision.getDecision());
        assertEquals(BigDecimal.ZERO, claimDecision.getPayableAmount());
    }

    @Test
    void testPartialApprovalScenario() {
        // Arrange
        claimDecision.setId(3L);
        claimDecision.setClaimId(300L);
        claimDecision.setDecision("PARTIAL");
        claimDecision.setPayableAmount(new BigDecimal("1200.50"));
        claimDecision.setReasons("[\"Some services covered at 80%\"]");
        claimDecision.setLetter("Your claim is partially approved.");
        claimDecision.setCreatedAt(LocalDateTime.now());

        // Assert
        assertEquals("PARTIAL", claimDecision.getDecision());
        assertTrue(claimDecision.getPayableAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testWithMultipleEvidences() {
        // Arrange
        ClaimDecisionEvidence evidence1 = new ClaimDecisionEvidence();
        evidence1.setChunkText("Policy section 1");
        evidence1.setScore(new BigDecimal("0.95"));

        ClaimDecisionEvidence evidence2 = new ClaimDecisionEvidence();
        evidence2.setChunkText("Policy section 2");
        evidence2.setScore(new BigDecimal("0.92"));

        List<ClaimDecisionEvidence> evidences = new ArrayList<>();
        evidences.add(evidence1);
        evidences.add(evidence2);

        // Act
        claimDecision.setEvidences(evidences);

        // Assert
        assertEquals(2, claimDecision.getEvidences().size());
        assertEquals("Policy section 1", claimDecision.getEvidences().get(0).getChunkText());
        assertEquals(new BigDecimal("0.92"), claimDecision.getEvidences().get(1).getScore());
    }

    @Test
    void testCompleteClaimDecision() {
        // Arrange
        claimDecision.setId(999L);
        claimDecision.setClaimId(54321L);
        claimDecision.setDecision("APPROVED");
        claimDecision.setPayableAmount(new BigDecimal("5000.75"));
        claimDecision.setReasons("[\"Emergency services covered\", \"Within annual limit\"]");
        claimDecision.setLetter("Dear valued customer, your emergency claim has been approved for $5000.75");
        claimDecision.setCreatedAt(LocalDateTime.of(2024, 1, 28, 14, 30));

        ClaimDecisionEvidence evidence = new ClaimDecisionEvidence();
        evidence.setChunkText("Emergency room visits are covered at 100%");
        evidence.setScore(new BigDecimal("0.98"));
        claimDecision.setEvidences(List.of(evidence));

        // Assert
        assertEquals(999L, claimDecision.getId());
        assertEquals(54321L, claimDecision.getClaimId());
        assertEquals("APPROVED", claimDecision.getDecision());
        assertEquals(0, new BigDecimal("5000.75").compareTo(claimDecision.getPayableAmount()));
        assertTrue(claimDecision.getReasons().contains("Emergency services covered"));
        assertTrue(claimDecision.getLetter().contains("5000.75"));
        assertNotNull(claimDecision.getCreatedAt());
        assertEquals(1, claimDecision.getEvidences().size());
    }

    @Test
    void testNullPayableAmount() {
        // Act
        claimDecision.setPayableAmount(null);

        // Assert
        assertNull(claimDecision.getPayableAmount());
    }

    @Test
    void testEmptyReasonsString() {
        // Act
        claimDecision.setReasons("");

        // Assert
        assertEquals("", claimDecision.getReasons());
    }

    @Test
    void testLongLetterContent() {
        // Arrange
        String longLetter = "Dear valued customer, ".repeat(100);

        // Act
        claimDecision.setLetter(longLetter);

        // Assert
        assertEquals(longLetter, claimDecision.getLetter());
    }
}
