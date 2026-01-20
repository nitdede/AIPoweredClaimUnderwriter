package com.ai.claim.underwriter.repository;

import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.entity.ClaimDecisionEvidence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for ClaimDecisionEvidenceDB repository
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimDecisionEvidenceDB Repository Tests")
class ClaimDecisionEvidenceDBTest {

    @Mock
    private ClaimDecisionEvidenceDB evidenceDB;

    private ClaimDecision testClaimDecision;
    private ClaimDecisionEvidence testEvidence;

    @BeforeEach
    void setUp() {
        // Arrange - Create test claim decision
        testClaimDecision = new ClaimDecision();
        testClaimDecision.setId(1L);
        testClaimDecision.setClaimId(1001L);
        testClaimDecision.setDecision("APPROVED");
        testClaimDecision.setPayableAmount(new BigDecimal("5000.00"));
        testClaimDecision.setCreatedAt(LocalDateTime.now());

        // Create test evidence
        testEvidence = new ClaimDecisionEvidence();
        testEvidence.setId(1L);
        testEvidence.setClaimDecision(testClaimDecision);
        testEvidence.setChunkText("This is evidence text from policy document");
        testEvidence.setScore(new BigDecimal("0.95"));
        
        // Stub save() to return the argument (lenient to avoid unnecessary stubbing errors)
        lenient().when(evidenceDB.save(any(ClaimDecisionEvidence.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should save and retrieve evidence")
    void testSaveAndFindById() {
        // Arrange
        when(evidenceDB.findById(1L)).thenReturn(Optional.of(testEvidence));
        
        // Act
        ClaimDecisionEvidence saved = evidenceDB.save(testEvidence);
        Optional<ClaimDecisionEvidence> found = evidenceDB.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getChunkText()).isEqualTo("This is evidence text from policy document");
        assertThat(found.get().getScore()).isEqualByComparingTo(new BigDecimal("0.95"));
    }

    @Test
    @DisplayName("Should find all evidences by decision ID")
    void testFindByDecisionId() {
        // Arrange
        ClaimDecisionEvidence evidence1 = new ClaimDecisionEvidence();
        evidence1.setClaimDecision(testClaimDecision);
        evidence1.setChunkText("Evidence 1");
        evidence1.setScore(new BigDecimal("0.95"));

        ClaimDecisionEvidence evidence2 = new ClaimDecisionEvidence();
        evidence2.setClaimDecision(testClaimDecision);
        evidence2.setChunkText("Evidence 2");
        evidence2.setScore(new BigDecimal("0.88"));

        ClaimDecisionEvidence evidence3 = new ClaimDecisionEvidence();
        evidence3.setClaimDecision(testClaimDecision);
        evidence3.setChunkText("Evidence 3");
        evidence3.setScore(new BigDecimal("0.92"));

        when(evidenceDB.findByDecisionId(testClaimDecision.getId())).thenReturn(List.of(evidence1, evidence2, evidence3));

        // Act
        List<ClaimDecisionEvidence> evidences = evidenceDB.findByDecisionId(testClaimDecision.getId());

        // Assert
        assertThat(evidences).hasSize(3);
        assertThat(evidences)
                .extracting(ClaimDecisionEvidence::getChunkText)
                .containsExactlyInAnyOrder("Evidence 1", "Evidence 2", "Evidence 3");
    }

    @Test
    @DisplayName("Should return empty list when no evidences found for decision ID")
    void testFindByDecisionIdNotFound() {
        // Arrange
        when(evidenceDB.findByDecisionId(9999L)).thenReturn(List.of());
        
        // Act
        List<ClaimDecisionEvidence> evidences = evidenceDB.findByDecisionId(9999L);

        // Assert
        assertThat(evidences).isEmpty();
    }

    @Test
    @DisplayName("Should save evidence with high score")
    void testSaveEvidenceWithHighScore() {
        // Arrange
        ClaimDecisionEvidence highScoreEvidence = new ClaimDecisionEvidence();
        highScoreEvidence.setId(1L);
        highScoreEvidence.setClaimDecision(testClaimDecision);
        highScoreEvidence.setChunkText("High confidence evidence");
        highScoreEvidence.setScore(new BigDecimal("0.99"));
        
        when(evidenceDB.findById(1L)).thenReturn(Optional.of(highScoreEvidence));

        // Act
        ClaimDecisionEvidence saved = evidenceDB.save(highScoreEvidence);

        // Assert
        Optional<ClaimDecisionEvidence> found = evidenceDB.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualByComparingTo(new BigDecimal("0.99"));
    }

    @Test
    @DisplayName("Should save evidence with low score")
    void testSaveEvidenceWithLowScore() {
        // Arrange
        ClaimDecisionEvidence lowScoreEvidence = new ClaimDecisionEvidence();
        lowScoreEvidence.setId(1L);
        lowScoreEvidence.setClaimDecision(testClaimDecision);
        lowScoreEvidence.setChunkText("Low confidence evidence");
        lowScoreEvidence.setScore(new BigDecimal("0.25"));
        
        when(evidenceDB.findById(1L)).thenReturn(Optional.of(lowScoreEvidence));

        // Act
        ClaimDecisionEvidence saved = evidenceDB.save(lowScoreEvidence);

        // Assert
        Optional<ClaimDecisionEvidence> found = evidenceDB.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualByComparingTo(new BigDecimal("0.25"));
    }

    @Test
    @DisplayName("Should delete evidence")
    void testDeleteEvidence() {
        // Arrange
        Long id = 1L;
        when(evidenceDB.findById(id)).thenReturn(Optional.empty());

        // Act
        evidenceDB.deleteById(id);

        // Assert
        Optional<ClaimDecisionEvidence> found = evidenceDB.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should update evidence")
    void testUpdateEvidence() {
        // Arrange
        testEvidence.setChunkText("Updated evidence text");
        testEvidence.setScore(new BigDecimal("0.87"));
        when(evidenceDB.save(any())).thenReturn(testEvidence);

        // Act
        ClaimDecisionEvidence updated = evidenceDB.save(testEvidence);

        // Assert
        assertThat(updated.getChunkText()).isEqualTo("Updated evidence text");
        assertThat(updated.getScore()).isEqualByComparingTo(new BigDecimal("0.87"));
    }

    @Test
    @DisplayName("Should find all evidences")
    void testFindAll() {
        // Arrange
        ClaimDecisionEvidence evidence2 = new ClaimDecisionEvidence();
        evidence2.setClaimDecision(testClaimDecision);
        evidence2.setChunkText("Another evidence");
        evidence2.setScore(new BigDecimal("0.80"));
        
        when(evidenceDB.findAll()).thenReturn(List.of(testEvidence, evidence2));

        // Act
        List<ClaimDecisionEvidence> evidences = evidenceDB.findAll();

        // Assert
        assertThat(evidences).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should maintain relationship with claim decision")
    void testRelationshipWithClaimDecision() {
        // Arrange
        when(evidenceDB.save(any())).thenReturn(testEvidence);
        when(evidenceDB.findById(1L)).thenReturn(Optional.of(testEvidence));

        // Act
        ClaimDecisionEvidence saved = evidenceDB.save(testEvidence);
        Optional<ClaimDecisionEvidence> found = evidenceDB.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getClaimDecision()).isNotNull();
        assertThat(found.get().getClaimDecision().getId()).isEqualTo(testClaimDecision.getId());
    }

    @Test
    @DisplayName("Should handle multiple evidences for different decisions")
    void testMultipleEvidencesForDifferentDecisions() {
        // Arrange
        testClaimDecision.setId(1L);
        ClaimDecision decision2 = new ClaimDecision();
        decision2.setId(2L);
        decision2.setClaimId(2002L);
        decision2.setDecision("REJECTED");
        decision2.setCreatedAt(LocalDateTime.now());

        ClaimDecisionEvidence evidence1 = new ClaimDecisionEvidence();
        evidence1.setClaimDecision(testClaimDecision);
        evidence1.setChunkText("Evidence for decision 1");
        evidence1.setScore(new BigDecimal("0.90"));

        ClaimDecisionEvidence evidence2 = new ClaimDecisionEvidence();
        evidence2.setClaimDecision(decision2);
        evidence2.setChunkText("Evidence for decision 2");
        evidence2.setScore(new BigDecimal("0.85"));

        when(evidenceDB.findByDecisionId(testClaimDecision.getId())).thenReturn(List.of(evidence1));
        when(evidenceDB.findByDecisionId(decision2.getId())).thenReturn(List.of(evidence2));

        // Act
        List<ClaimDecisionEvidence> evidencesForDecision1 = evidenceDB.findByDecisionId(testClaimDecision.getId());
        List<ClaimDecisionEvidence> evidencesForDecision2 = evidenceDB.findByDecisionId(decision2.getId());

        // Assert
        assertThat(evidencesForDecision1).hasSize(1);
        assertThat(evidencesForDecision2).hasSize(1);
        assertThat(evidencesForDecision1.get(0).getChunkText()).isEqualTo("Evidence for decision 1");
        assertThat(evidencesForDecision2.get(0).getChunkText()).isEqualTo("Evidence for decision 2");
    }

    @Test
    @DisplayName("Should save evidence with long text")
    void testSaveEvidenceWithLongText() {
        // Arrange
        String longText = "A".repeat(1000);
        ClaimDecisionEvidence longTextEvidence = new ClaimDecisionEvidence();
        longTextEvidence.setId(1L);
        longTextEvidence.setClaimDecision(testClaimDecision);
        longTextEvidence.setChunkText(longText);
        longTextEvidence.setScore(new BigDecimal("0.75"));
        
        when(evidenceDB.findById(1L)).thenReturn(Optional.of(longTextEvidence));

        // Act
        ClaimDecisionEvidence saved = evidenceDB.save(longTextEvidence);

        // Assert
        Optional<ClaimDecisionEvidence> found = evidenceDB.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getChunkText()).hasSize(1000);
    }
}
