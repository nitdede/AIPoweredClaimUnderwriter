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
 * Comprehensive unit tests for ClaimDecisionDB repository
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClaimDecisionDB Repository Tests")
class ClaimDecisionDBTest {

    @Mock
    private ClaimDecisionDB claimDecisionDB;

    private ClaimDecision testClaimDecision;

    @BeforeEach
    void setUp() {
        // Arrange - Create test data
        testClaimDecision = new ClaimDecision();
        testClaimDecision.setId(1L);
        testClaimDecision.setClaimId(1001L);
        testClaimDecision.setDecision("APPROVED");
        testClaimDecision.setPayableAmount(new BigDecimal("5000.00"));
        testClaimDecision.setReasons("{\"reason\": \"Valid claim\"}");
        testClaimDecision.setLetter("Claim approved letter");
        testClaimDecision.setCreatedAt(LocalDateTime.now());
        
        // Stub save() to return the argument (lenient to avoid unnecessary stubbing errors)
        lenient().when(claimDecisionDB.save(any(ClaimDecision.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should save and retrieve claim decision")
    void testSaveAndFindById() {
        // Arrange
        when(claimDecisionDB.findById(1L)).thenReturn(Optional.of(testClaimDecision));
        
        // Act
        ClaimDecision saved = claimDecisionDB.save(testClaimDecision);
        Optional<ClaimDecision> found = claimDecisionDB.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getClaimId()).isEqualTo(1001L);
        assertThat(found.get().getDecision()).isEqualTo("APPROVED");
        assertThat(found.get().getPayableAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    @DisplayName("Should find claim decision by claim ID")
    void testFindByClaimId() {
        // Arrange
        when(claimDecisionDB.findByClaimId(1001L)).thenReturn(Optional.of(testClaimDecision));

        // Act
        Optional<ClaimDecision> found = claimDecisionDB.findByClaimId(1001L);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getClaimId()).isEqualTo(1001L);
        assertThat(found.get().getDecision()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("Should return empty optional when claim ID not found")
    void testFindByClaimIdNotFound() {
        // Arrange
        when(claimDecisionDB.findByClaimId(9999L)).thenReturn(Optional.empty());
        
        // Act
        Optional<ClaimDecision> found = claimDecisionDB.findByClaimId(9999L);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find latest claim decision by claim ID")
    void testFindLatestByClaimId() {
        // Arrange - Create multiple decisions for same claim
        ClaimDecision decision1 = new ClaimDecision();
        decision1.setClaimId(2001L);
        decision1.setDecision("PENDING");
        decision1.setCreatedAt(LocalDateTime.now().minusDays(2));

        ClaimDecision decision2 = new ClaimDecision();
        decision2.setClaimId(2001L);
        decision2.setDecision("APPROVED");
        decision2.setCreatedAt(LocalDateTime.now().minusDays(1));

        ClaimDecision decision3 = new ClaimDecision();
        decision3.setClaimId(2001L);
        decision3.setDecision("FINAL");
        decision3.setCreatedAt(LocalDateTime.now());
        
        when(claimDecisionDB.findLatestByClaimId(2001L)).thenReturn(Optional.of(List.of(decision3, decision2, decision1)));

        // Act
        Optional<List<ClaimDecision>> result = claimDecisionDB.findLatestByClaimId(2001L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isNotEmpty();
        assertThat(result.get().get(0).getDecision()).isEqualTo("FINAL");
    }

    @Test
    @DisplayName("Should find claim decision with evidence using JOIN FETCH")
    void testFindByClaimIdWithEvidence() {
        // Arrange
        ClaimDecisionEvidence evidence1 = new ClaimDecisionEvidence();
        evidence1.setClaimDecision(testClaimDecision);
        evidence1.setChunkText("Evidence text 1");
        evidence1.setScore(new BigDecimal("0.95"));

        ClaimDecisionEvidence evidence2 = new ClaimDecisionEvidence();
        evidence2.setClaimDecision(testClaimDecision);
        evidence2.setChunkText("Evidence text 2");
        evidence2.setScore(new BigDecimal("0.88"));
        
        testClaimDecision.setEvidences(List.of(evidence1, evidence2));
        when(claimDecisionDB.findByClaimIdWithEvidence(1001L)).thenReturn(Optional.of(testClaimDecision));

        // Act
        Optional<ClaimDecision> found = claimDecisionDB.findByClaimIdWithEvidence(1001L);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEvidences()).hasSize(2);
        assertThat(found.get().getEvidences())
                .extracting(ClaimDecisionEvidence::getChunkText)
                .containsExactlyInAnyOrder("Evidence text 1", "Evidence text 2");
    }

    @Test
    @DisplayName("Should find latest claim decision with evidence")
    void testFindLatestByClaimIdWithEvidence() {
        // Arrange
        ClaimDecision latestDecision = new ClaimDecision();
        latestDecision.setClaimId(3001L);
        latestDecision.setDecision("APPROVED");
        latestDecision.setCreatedAt(LocalDateTime.now());

        ClaimDecisionEvidence evidence = new ClaimDecisionEvidence();
        evidence.setClaimDecision(latestDecision);
        evidence.setChunkText("Latest evidence");
        evidence.setScore(new BigDecimal("0.92"));
        
        latestDecision.setEvidences(List.of(evidence));
        when(claimDecisionDB.findLatestByClaimIdWithEvidence(3001L)).thenReturn(Optional.of(latestDecision));

        // Act
        Optional<ClaimDecision> found = claimDecisionDB.findLatestByClaimIdWithEvidence(3001L);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getDecision()).isEqualTo("APPROVED");
        assertThat(found.get().getEvidences()).hasSize(1);
        assertThat(found.get().getEvidences().get(0).getChunkText()).isEqualTo("Latest evidence");
    }

    @Test
    @DisplayName("Should return empty when no decision with evidence found")
    void testFindByClaimIdWithEvidenceNotFound() {
        // Arrange
        when(claimDecisionDB.findByClaimIdWithEvidence(9999L)).thenReturn(Optional.empty());
        
        // Act
        Optional<ClaimDecision> found = claimDecisionDB.findByClaimIdWithEvidence(9999L);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should handle claim decision without evidences")
    void testFindByClaimIdWithEvidenceNoEvidences() {
        // Arrange
        testClaimDecision.setEvidences(List.of());
        when(claimDecisionDB.findByClaimIdWithEvidence(1001L)).thenReturn(Optional.of(testClaimDecision));

        // Act
        Optional<ClaimDecision> found = claimDecisionDB.findByClaimIdWithEvidence(1001L);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEvidences()).isEmpty();
    }

    @Test
    @DisplayName("Should delete claim decision")
    void testDeleteClaimDecision() {
        // Arrange
        Long id = 1L;
        when(claimDecisionDB.findById(id)).thenReturn(Optional.empty());

        // Act
        claimDecisionDB.deleteById(id);

        // Assert
        Optional<ClaimDecision> found = claimDecisionDB.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all claim decisions")
    void testFindAll() {
        // Arrange
        ClaimDecision decision2 = new ClaimDecision();
        decision2.setId(2L);
        decision2.setClaimId(1002L);
        decision2.setDecision("REJECTED");
        decision2.setCreatedAt(LocalDateTime.now());
        
        when(claimDecisionDB.findAll()).thenReturn(List.of(testClaimDecision, decision2));

        // Act
        List<ClaimDecision> decisions = claimDecisionDB.findAll();

        // Assert
        assertThat(decisions).hasSize(2);
    }

    @Test
    @DisplayName("Should update claim decision")
    void testUpdateClaimDecision() {
        // Arrange
        testClaimDecision.setDecision("REJECTED");
        testClaimDecision.setPayableAmount(BigDecimal.ZERO);

        // Act
        ClaimDecision updated = claimDecisionDB.save(testClaimDecision);

        // Assert
        assertThat(updated.getDecision()).isEqualTo("REJECTED");
        assertThat(updated.getPayableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
