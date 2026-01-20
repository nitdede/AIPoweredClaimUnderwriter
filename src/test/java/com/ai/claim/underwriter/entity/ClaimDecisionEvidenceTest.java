package com.ai.claim.underwriter.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class ClaimDecisionEvidenceTest {

    private ClaimDecisionEvidence evidence;

    @BeforeEach
    void setUp() {
        evidence = new ClaimDecisionEvidence();
    }

    @Test
    void testDefaultConstructor() {
        // Assert
        assertNotNull(evidence);
        assertNull(evidence.getId());
        assertNull(evidence.getDecisionId());
        assertNull(evidence.getChunkText());
        assertNull(evidence.getScore());
        assertNotNull(evidence.getCreatedAt()); // Default constructor sets createdAt to now()
    }

    @Test
    void testCreatedAtInitialization() {
        // Arrange
        OffsetDateTime before = OffsetDateTime.now();
        
        // Act
        ClaimDecisionEvidence newEvidence = new ClaimDecisionEvidence();
        
        // Assert
        OffsetDateTime after = OffsetDateTime.now();
        assertNotNull(newEvidence.getCreatedAt());
        assertTrue(newEvidence.getCreatedAt().isAfter(before.minusSeconds(1)));
        assertTrue(newEvidence.getCreatedAt().isBefore(after.plusSeconds(1)));
    }

    @Test
    void testIdGetterAndSetter() {
        // Arrange
        Long id = 12345L;

        // Act
        evidence.setId(id);

        // Assert
        assertEquals(id, evidence.getId());
    }

    @Test
    void testDecisionIdGetterAndSetter() {
        // Arrange
        Long decisionId = 67890L;

        // Act
        evidence.setDecisionId(decisionId);

        // Assert
        assertEquals(decisionId, evidence.getDecisionId());
    }

    @Test
    void testChunkTextGetterAndSetter() {
        // Arrange
        String chunkText = "Policy section 1: Emergency services are covered at 100%";

        // Act
        evidence.setChunkText(chunkText);

        // Assert
        assertEquals(chunkText, evidence.getChunkText());
    }

    @Test
    void testScoreGetterAndSetter() {
        // Arrange
        BigDecimal score = new BigDecimal("0.950000");

        // Act
        evidence.setScore(score);

        // Assert
        assertEquals(score, evidence.getScore());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        // Arrange
        OffsetDateTime createdAt = OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.UTC);

        // Act
        evidence.setCreatedAt(createdAt);

        // Assert
        assertEquals(createdAt, evidence.getCreatedAt());
    }

    @Test
    void testClaimDecisionGetterAndSetter() {
        // Arrange
        ClaimDecision claimDecision = new ClaimDecision();
        claimDecision.setId(999L);
        claimDecision.setDecision("APPROVED");

        // Act
        evidence.setClaimDecision(claimDecision);

        // Assert
        assertEquals(claimDecision, evidence.getClaimDecision());
        assertEquals(999L, evidence.getDecisionId()); // Should be set automatically
    }

    @Test
    void testSetClaimDecisionUpdatesDecisionId() {
        // Arrange
        ClaimDecision claimDecision = new ClaimDecision();
        claimDecision.setId(555L);

        // Act
        evidence.setClaimDecision(claimDecision);

        // Assert
        assertEquals(555L, evidence.getDecisionId());
        assertEquals(claimDecision, evidence.getClaimDecision());
    }

    @Test
    void testSetClaimDecisionWithNull() {
        // Act
        evidence.setClaimDecision(null);

        // Assert
        assertNull(evidence.getClaimDecision());
        // decisionId is not updated when claimDecision is null
    }

    @Test
    void testCompleteEvidence() {
        // Arrange
        evidence.setId(100L);
        evidence.setDecisionId(200L);
        evidence.setChunkText("Coverage includes hospital stays up to 30 days");
        evidence.setScore(new BigDecimal("0.920000"));
        evidence.setCreatedAt(OffsetDateTime.of(2024, 1, 28, 14, 30, 0, 0, ZoneOffset.UTC));

        ClaimDecision decision = new ClaimDecision();
        decision.setId(200L);
        decision.setDecision("APPROVED");
        evidence.setClaimDecision(decision);

        // Assert
        assertEquals(100L, evidence.getId());
        assertEquals(200L, evidence.getDecisionId());
        assertEquals("Coverage includes hospital stays up to 30 days", evidence.getChunkText());
        assertEquals(0, new BigDecimal("0.920000").compareTo(evidence.getScore()));
        assertNotNull(evidence.getCreatedAt());
        assertEquals("APPROVED", evidence.getClaimDecision().getDecision());
    }

    @Test
    void testHighScoreEvidence() {
        // Arrange
        evidence.setChunkText("Policy explicitly covers this procedure");
        evidence.setScore(new BigDecimal("0.990000"));

        // Assert
        assertTrue(evidence.getScore().compareTo(new BigDecimal("0.95")) > 0);
    }

    @Test
    void testLowScoreEvidence() {
        // Arrange
        evidence.setChunkText("Ambiguous policy text");
        evidence.setScore(new BigDecimal("0.650000"));

        // Assert
        assertTrue(evidence.getScore().compareTo(new BigDecimal("0.70")) < 0);
    }

    @Test
    void testLongChunkText() {
        // Arrange
        String longText = "This is a very long policy text that contains detailed information about coverage. ".repeat(50);

        // Act
        evidence.setChunkText(longText);

        // Assert
        assertEquals(longText, evidence.getChunkText());
        assertTrue(evidence.getChunkText().length() > 1000);
    }

    @Test
    void testNullValues() {
        // Act
        evidence.setId(null);
        evidence.setDecisionId(null);
        evidence.setChunkText(null);
        evidence.setScore(null);
        evidence.setCreatedAt(null);

        // Assert
        assertNull(evidence.getId());
        assertNull(evidence.getDecisionId());
        assertNull(evidence.getChunkText());
        assertNull(evidence.getScore());
        assertNull(evidence.getCreatedAt());
    }

    @Test
    void testScorePrecision() {
        // Arrange - Test 6 decimal places precision
        BigDecimal preciseScore = new BigDecimal("0.123456");

        // Act
        evidence.setScore(preciseScore);

        // Assert
        assertEquals(preciseScore, evidence.getScore());
        assertEquals(0, new BigDecimal("0.123456").compareTo(evidence.getScore()));
    }

    @Test
    void testZeroScore() {
        // Arrange
        evidence.setScore(BigDecimal.ZERO);

        // Assert
        assertEquals(BigDecimal.ZERO, evidence.getScore());
    }

    @Test
    void testMaxScore() {
        // Arrange
        evidence.setScore(BigDecimal.ONE); // 1.0

        // Assert
        assertEquals(BigDecimal.ONE, evidence.getScore());
    }

    @Test
    void testDifferentTimezones() {
        // Arrange
        OffsetDateTime utcTime = OffsetDateTime.of(2024, 1, 28, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime estTime = OffsetDateTime.of(2024, 1, 28, 10, 0, 0, 0, ZoneOffset.ofHours(-5));
        OffsetDateTime istTime = OffsetDateTime.of(2024, 1, 28, 10, 0, 0, 0, ZoneOffset.ofHoursMinutes(5, 30));

        // Test UTC
        evidence.setCreatedAt(utcTime);
        assertEquals(utcTime, evidence.getCreatedAt());

        // Test EST
        evidence.setCreatedAt(estTime);
        assertEquals(estTime, evidence.getCreatedAt());

        // Test IST
        evidence.setCreatedAt(istTime);
        assertEquals(istTime, evidence.getCreatedAt());
    }

    @Test
    void testBidirectionalRelationship() {
        // Arrange
        ClaimDecision decision = new ClaimDecision();
        decision.setId(777L);

        ClaimDecisionEvidence evidence1 = new ClaimDecisionEvidence();
        evidence1.setChunkText("Evidence 1");
        evidence1.setClaimDecision(decision);

        ClaimDecisionEvidence evidence2 = new ClaimDecisionEvidence();
        evidence2.setChunkText("Evidence 2");
        evidence2.setClaimDecision(decision);

        // Assert
        assertEquals(777L, evidence1.getDecisionId());
        assertEquals(777L, evidence2.getDecisionId());
        assertEquals(decision, evidence1.getClaimDecision());
        assertEquals(decision, evidence2.getClaimDecision());
    }
}
