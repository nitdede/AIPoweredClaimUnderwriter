package com.ai.claim.underwriter.tools;

import com.ai.claim.underwriter.entity.ClaimAIResult;
import com.ai.claim.underwriter.model.InvoiceContext;
import com.ai.claim.underwriter.model.ClaimExtractionResult;
import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.repository.ClaimAIResultDB;
import com.ai.claim.underwriter.repository.ClaimDecisionDB;
import com.ai.claim.underwriter.repository.ClaimDecisionEvidenceDB;
import com.ai.claim.underwriter.service.DataBaseOperationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataBaseToolsTest {

    @Mock
    private ClaimAIResultDB claimAIResultDB;

    @Mock
    private ClaimDecisionDB claimDecisionDB;

    @Mock
    private ClaimDecisionEvidenceDB claimDecisionEvidenceDB;

    private InvoiceContext invoiceContext;
    private DataBaseOperationService dataBaseTools;

    @BeforeEach
    void setUp() {
        invoiceContext = new InvoiceContext();
        dataBaseTools = new DataBaseOperationService(
                claimAIResultDB,
                new ObjectMapper(),
                claimDecisionDB,
                claimDecisionEvidenceDB
        );
    }

    @Test
    void saveInvoiceData_mapsFieldsAndSaves() {
        ExtractedInvoice invoice = new ExtractedInvoice(
                "Rajesh",
                "INV-1",
                "2024-01-01",
                150.25,
                "INR",
                "Hospital",
                List.of(new ExtractedInvoice.LineItem("Item", 10.0, 0.9)),
                Map.of("policyNumber", 0.8, "totalAmount", 0.9)
        );

        dataBaseTools.saveInvoiceData(invoice);

        ArgumentCaptor<ClaimAIResult> captor = ArgumentCaptor.forClass(ClaimAIResult.class);
        verify(claimAIResultDB).save(captor.capture());
        ClaimAIResult saved = captor.getValue();

        assertThat(saved.getPatientName()).isEqualTo("Rajesh");
        assertThat(saved.getInvoiceNumber()).isEqualTo("INV-1");
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("150.25"));
        assertThat(saved.getCurrency()).isEqualTo("INR");
        assertThat(saved.getAiStatus()).isEqualTo("NEEDS_INFO");
        assertThat(saved.getConfidenceScore()).isEqualByComparingTo(new BigDecimal("0.85"));
        assertThat(saved.getAiOutput()).isNotEmpty();
    }

    @Test
    void saveInvoiceData_throwsWhenInvoiceNull() {
        assertThatThrownBy(() -> dataBaseTools.saveInvoiceData(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No extracted invoice found");
    }

    @Test
    void saveResult_setsFallbackAiOutputOnJsonError() throws Exception {
        ObjectMapper failingMapper = org.mockito.Mockito.mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new RuntimeException("boom"));
        DataBaseOperationService toolsWithFailingMapper = new DataBaseOperationService(
                claimAIResultDB,
                failingMapper,
                claimDecisionDB,
                claimDecisionEvidenceDB
        );

        ClaimExtractionResult result = new ClaimExtractionResult();
        result.patientName = "Rajesh";
        result.totalAmount = 10.0;
        result.confidence = Map.of("policyNumber", 0.9, "totalAmount", 0.9);
        result.policyNumber = "POL-1";

        toolsWithFailingMapper.saveResult(result);

        ArgumentCaptor<ClaimAIResult> captor = ArgumentCaptor.forClass(ClaimAIResult.class);
        verify(claimAIResultDB).save(captor.capture());
        assertThat(captor.getValue().getAiOutput()).isEqualTo("{}");
    }

    @Test
    void needMoreInfo_returnsTrueWhenMissingFieldsOrLowConfidence() {
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.policyNumber = null;
        result.totalAmount = null;
        result.confidence = Map.of("policyNumber", 0.9, "totalAmount", 0.9);
        assertThat(dataBaseTools.needMoreInfo(result)).isTrue();

        result.policyNumber = "POL-1";
        result.totalAmount = 10.0;
        result.confidence = Map.of("policyNumber", 0.6, "totalAmount", 0.9);
        assertThat(dataBaseTools.needMoreInfo(result)).isTrue();
    }

    @Test
    void needMoreInfo_returnsFalseWhenAllFieldsPresentAndConfident() {
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.policyNumber = "POL-1";
        result.totalAmount = 10.0;
        result.confidence = Map.of("policyNumber", 0.9, "totalAmount", 0.9);
        assertThat(dataBaseTools.needMoreInfo(result)).isFalse();
    }
}
