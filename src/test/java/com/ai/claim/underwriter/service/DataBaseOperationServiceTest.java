package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.entity.ClaimAIResult;
import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.entity.ClaimDecisionEvidence;
import com.ai.claim.underwriter.model.ClaimEvidence;
import com.ai.claim.underwriter.model.ClaimExtractionResult;
import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.repository.ClaimAIResultDB;
import com.ai.claim.underwriter.repository.ClaimDecisionDB;
import com.ai.claim.underwriter.repository.ClaimDecisionEvidenceDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataBaseOperationServiceTest {

    @Mock
    private ClaimAIResultDB claimAIResultDB;

    @Mock
    private ClaimDecisionDB claimDecisionDB;

    @Mock
    private ClaimDecisionEvidenceDB claimDecisionEvidenceDB;

    private ObjectMapper objectMapper;
    private DataBaseOperationService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new DataBaseOperationService(
                claimAIResultDB,
                objectMapper,
                claimDecisionDB,
                claimDecisionEvidenceDB
        );
    }

    @Test
    void saveInvoiceData_withValidInvoice_savesToDatabase() {
        // Arrange
        ExtractedInvoice invoice = new ExtractedInvoice(
                "John Doe",
                "INV-12345",
                "2024-01-15",
                1500.0,
                "USD",
                "City Hospital",
                List.of(new ExtractedInvoice.LineItem("X-Ray", 500.0, 0.95)),
                Map.of("patientName", 0.9, "totalAmount", 0.85)
        );

        when(claimAIResultDB.save(any(ClaimAIResult.class))).thenReturn(new ClaimAIResult());

        // Act
        service.saveInvoiceData(invoice);

        // Assert
        ArgumentCaptor<ClaimAIResult> captor = ArgumentCaptor.forClass(ClaimAIResult.class);
        verify(claimAIResultDB).save(captor.capture());

        ClaimAIResult saved = captor.getValue();
        assertThat(saved.getPatientName()).isEqualTo("John Doe");
        assertThat(saved.getInvoiceNumber()).isEqualTo("INV-12345");
        assertThat(saved.getHospitalName()).isEqualTo("City Hospital");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(saved.getCurrency()).isEqualTo("USD");
    }

    @Test
    void saveInvoiceData_withNullInvoice_throwsException() {
        // Act & Assert
        assertThatThrownBy(() -> service.saveInvoiceData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No extracted invoice found");
    }

    @Test
    void saveResult_withValidResult_savesToDatabase() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.patientName = "Jane Smith";
        result.policyNumber = "POL-123";
        result.hospitalName = "General Hospital";
        result.invoiceNumber = "INV-456";
        result.totalAmount = 2000.0;
        result.currency = "USD";
        result.confidence = Map.of("patientName", 0.9, "policyNumber", 0.9, "totalAmount", 0.8);

        when(claimAIResultDB.save(any(ClaimAIResult.class))).thenReturn(new ClaimAIResult());

        // Act
        service.saveResult(result);

        // Assert
        ArgumentCaptor<ClaimAIResult> captor = ArgumentCaptor.forClass(ClaimAIResult.class);
        verify(claimAIResultDB).save(captor.capture());

        ClaimAIResult saved = captor.getValue();
        assertThat(saved.getPatientName()).isEqualTo("Jane Smith");
        assertThat(saved.getPolicyNumber()).isEqualTo("POL-123");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(saved.getConfidenceScore()).isEqualByComparingTo(new BigDecimal("0.87"));
        assertThat(saved.getAiStatus()).isEqualTo("COMPLETED");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void saveResult_withLowConfidence_setsNeedsInfoStatus() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.patientName = "Jane Smith";
        result.policyNumber = "POL-123";
        result.totalAmount = 2000.0;
        result.confidence = Map.of("policyNumber", 0.6, "totalAmount", 0.5); // Low confidence

        when(claimAIResultDB.save(any(ClaimAIResult.class))).thenReturn(new ClaimAIResult());

        // Act
        service.saveResult(result);

        // Assert
        ArgumentCaptor<ClaimAIResult> captor = ArgumentCaptor.forClass(ClaimAIResult.class);
        verify(claimAIResultDB).save(captor.capture());

        ClaimAIResult saved = captor.getValue();
        assertThat(saved.getAiStatus()).isEqualTo("NEEDS_INFO");
    }

    @Test
    void saveResult_withMissingPolicyNumber_setsNeedsInfoStatus() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.patientName = "Jane Smith";
        result.policyNumber = null; // Missing
        result.totalAmount = 2000.0;
        result.confidence = Map.of("totalAmount", 0.9);

        when(claimAIResultDB.save(any(ClaimAIResult.class))).thenReturn(new ClaimAIResult());

        // Act
        service.saveResult(result);

        // Assert
        ArgumentCaptor<ClaimAIResult> captor = ArgumentCaptor.forClass(ClaimAIResult.class);
        verify(claimAIResultDB).save(captor.capture());

        ClaimAIResult saved = captor.getValue();
        assertThat(saved.getAiStatus()).isEqualTo("NEEDS_INFO");
    }

    @Test
    void saveIntoClaimEvidenceDB_withValidEvidence_savesAllEvidences() {
        // Arrange
        ClaimDecision decision = new ClaimDecision();
        decision.setId(1L);
        decision.setClaimId(100L);

        Document doc1 = new Document("Evidence text 1", Map.of("score", 0.95));
        Document doc2 = new Document("Evidence text 2", Map.of("score", 0.85));

        ClaimEvidence claimEvidence = new ClaimEvidence(
                List.of(doc1, doc2),
                decision,
                List.of("chunk1", "chunk2"),
                "[]"
        );

        when(claimDecisionEvidenceDB.saveAll(anyList())).thenReturn(List.of());

        // Act
        service.saveIntoClaimEvidenceDB(claimEvidence);

        // Assert
        ArgumentCaptor<List<ClaimDecisionEvidence>> captor = ArgumentCaptor.forClass(List.class);
        verify(claimDecisionEvidenceDB).saveAll(captor.capture());

        List<ClaimDecisionEvidence> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getChunkText()).isEqualTo("Evidence text 1");
        assertThat(saved.get(0).getScore()).isEqualByComparingTo(new BigDecimal("0.9500"));
        assertThat(saved.get(0).getClaimDecision()).isEqualTo(decision);
        assertThat(saved.get(1).getChunkText()).isEqualTo("Evidence text 2");
        assertThat(saved.get(1).getScore()).isEqualByComparingTo(new BigDecimal("0.8500"));
    }

    @Test
    void saveIntoClaimDecisionDB_withValidEvidence_savesDecision() {
        // Arrange
        ClaimDecision decision = new ClaimDecision();
        decision.setClaimId(100L);
        decision.setDecision("APPROVED");

        ClaimEvidence claimEvidence = new ClaimEvidence(
                List.of(),
                decision,
                List.of(),
                "[]"
        );

        when(claimDecisionDB.save(any(ClaimDecision.class))).thenReturn(decision);

        // Act
        ClaimDecision result = service.saveIntoClaimDecisionDB(claimEvidence);

        // Assert
        verify(claimDecisionDB).save(decision);
        assertThat(result).isEqualTo(decision);
    }

    @Test
    void needMoreInfo_withMissingPolicyNumber_returnsTrue() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.policyNumber = null;
        result.totalAmount = 1000.0;
        result.confidence = Map.of("totalAmount", 0.9);

        // Act
        boolean needsInfo = service.needMoreInfo(result);

        // Assert
        assertThat(needsInfo).isTrue();
    }

    @Test
    void needMoreInfo_withBlankPolicyNumber_returnsTrue() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.policyNumber = "   ";
        result.totalAmount = 1000.0;
        result.confidence = Map.of("totalAmount", 0.9);

        // Act
        boolean needsInfo = service.needMoreInfo(result);

        // Assert
        assertThat(needsInfo).isTrue();
    }

    @Test
    void needMoreInfo_withMissingTotalAmount_returnsTrue() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.policyNumber = "POL-123";
        result.totalAmount = null;
        result.confidence = Map.of("policyNumber", 0.9);

        // Act
        boolean needsInfo = service.needMoreInfo(result);

        // Assert
        assertThat(needsInfo).isTrue();
    }

    @Test
    void needMoreInfo_withLowPolicyNumberConfidence_returnsTrue() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.policyNumber = "POL-123";
        result.totalAmount = 1000.0;
        result.confidence = Map.of("policyNumber", 0.6, "totalAmount", 0.9);

        // Act
        boolean needsInfo = service.needMoreInfo(result);

        // Assert
        assertThat(needsInfo).isTrue();
    }

    @Test
    void needMoreInfo_withLowTotalAmountConfidence_returnsTrue() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.policyNumber = "POL-123";
        result.totalAmount = 1000.0;
        result.confidence = Map.of("policyNumber", 0.9, "totalAmount", 0.5);

        // Act
        boolean needsInfo = service.needMoreInfo(result);

        // Assert
        assertThat(needsInfo).isTrue();
    }

    @Test
    void needMoreInfo_withAllValidData_returnsFalse() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.policyNumber = "POL-123";
        result.totalAmount = 1000.0;
        result.confidence = Map.of("policyNumber", 0.9, "totalAmount", 0.9);

        // Act
        boolean needsInfo = service.needMoreInfo(result);

        // Assert
        assertThat(needsInfo).isFalse();
    }

    @Test
    void needMoreInfo_withNullConfidenceMap_returnsTrue() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.policyNumber = "POL-123";
        result.totalAmount = 1000.0;
        result.confidence = null;

        // Act
        boolean needsInfo = service.needMoreInfo(result);

        // Assert
        assertThat(needsInfo).isTrue();
    }

    @Test
    void saveResult_withNullTotalAmount_handlesGracefully() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.patientName = "Test Patient";
        result.totalAmount = null;
        result.confidence = Map.of();

        when(claimAIResultDB.save(any(ClaimAIResult.class))).thenReturn(new ClaimAIResult());

        // Act
        service.saveResult(result);

        // Assert
        ArgumentCaptor<ClaimAIResult> captor = ArgumentCaptor.forClass(ClaimAIResult.class);
        verify(claimAIResultDB).save(captor.capture());

        ClaimAIResult saved = captor.getValue();
        assertThat(saved.getTotalAmount()).isNull();
    }

    @Test
    void saveResult_withEmptyConfidence_setsZeroConfidenceScore() {
        // Arrange
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.patientName = "Test Patient";
        result.totalAmount = 1000.0;
        result.confidence = Map.of();

        when(claimAIResultDB.save(any(ClaimAIResult.class))).thenReturn(new ClaimAIResult());

        // Act
        service.saveResult(result);

        // Assert
        ArgumentCaptor<ClaimAIResult> captor = ArgumentCaptor.forClass(ClaimAIResult.class);
        verify(claimAIResultDB).save(captor.capture());

        ClaimAIResult saved = captor.getValue();
        assertThat(saved.getConfidenceScore()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void saveIntoClaimEvidenceDB_withNonNumericScore_handlesGracefully() {
        // Arrange
        ClaimDecision decision = new ClaimDecision();
        decision.setId(1L);

        Document doc = new Document("Evidence text", Map.of("score", "not a number"));

        ClaimEvidence claimEvidence = new ClaimEvidence(
                List.of(doc),
                decision,
                List.of("chunk1"),
                "[]"
        );

        when(claimDecisionEvidenceDB.saveAll(anyList())).thenReturn(List.of());

        // Act
        service.saveIntoClaimEvidenceDB(claimEvidence);

        // Assert
        ArgumentCaptor<List<ClaimDecisionEvidence>> captor = ArgumentCaptor.forClass(List.class);
        verify(claimDecisionEvidenceDB).saveAll(captor.capture());

        List<ClaimDecisionEvidence> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getScore()).isNull();
    }
}
