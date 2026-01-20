package com.ai.claim.underwriter.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClaimAIResultTest {

    private ClaimAIResult claimAIResult;

    @BeforeEach
    void setUp() {
        claimAIResult = new ClaimAIResult();
    }

    @Test
    void testDefaultConstructor() {
        // Assert
        assertNotNull(claimAIResult);
        assertNull(claimAIResult.getId());
        assertNull(claimAIResult.getPatientName());
        assertNull(claimAIResult.getPolicyNumber());
        assertNull(claimAIResult.getHospitalName());
        assertNull(claimAIResult.getInvoiceNumber());
        assertNull(claimAIResult.getTotalAmount());
        assertNull(claimAIResult.getCurrency());
        assertNull(claimAIResult.getConfidenceScore());
        assertNull(claimAIResult.getAiStatus());
        assertNull(claimAIResult.getAiOutput());
        assertNull(claimAIResult.getCreatedAt());
    }

    @Test
    void testIdGetterAndSetter() {
        // Arrange
        Integer id = 12345;

        // Act
        claimAIResult.setId(id);

        // Assert
        assertEquals(id, claimAIResult.getId());
    }

    @Test
    void testPatientNameGetterAndSetter() {
        // Arrange
        String patientName = "John Doe";

        // Act
        claimAIResult.setPatientName(patientName);

        // Assert
        assertEquals(patientName, claimAIResult.getPatientName());
    }

    @Test
    void testPolicyNumberGetterAndSetter() {
        // Arrange
        String policyNumber = "POL-12345";

        // Act
        claimAIResult.setPolicyNumber(policyNumber);

        // Assert
        assertEquals(policyNumber, claimAIResult.getPolicyNumber());
    }

    @Test
    void testHospitalNameGetterAndSetter() {
        // Arrange
        String hospitalName = "City Hospital";

        // Act
        claimAIResult.setHospitalName(hospitalName);

        // Assert
        assertEquals(hospitalName, claimAIResult.getHospitalName());
    }

    @Test
    void testInvoiceNumberGetterAndSetter() {
        // Arrange
        String invoiceNumber = "INV-2024-001";

        // Act
        claimAIResult.setInvoiceNumber(invoiceNumber);

        // Assert
        assertEquals(invoiceNumber, claimAIResult.getInvoiceNumber());
    }

    @Test
    void testTotalAmountGetterAndSetter() {
        // Arrange
        BigDecimal totalAmount = new BigDecimal("1500.50");

        // Act
        claimAIResult.setTotalAmount(totalAmount);

        // Assert
        assertEquals(totalAmount, claimAIResult.getTotalAmount());
    }

    @Test
    void testCurrencyGetterAndSetter() {
        // Arrange
        String currency = "USD";

        // Act
        claimAIResult.setCurrency(currency);

        // Assert
        assertEquals(currency, claimAIResult.getCurrency());
    }

    @Test
    void testConfidenceScoreGetterAndSetter() {
        // Arrange
        BigDecimal confidenceScore = new BigDecimal("0.95");

        // Act
        claimAIResult.setConfidenceScore(confidenceScore);

        // Assert
        assertEquals(confidenceScore, claimAIResult.getConfidenceScore());
    }

    @Test
    void testAiStatusGetterAndSetter() {
        // Arrange
        String aiStatus = "PROCESSED";

        // Act
        claimAIResult.setAiStatus(aiStatus);

        // Assert
        assertEquals(aiStatus, claimAIResult.getAiStatus());
    }

    @Test
    void testAiOutputGetterAndSetter() {
        // Arrange
        String aiOutput = "{\"result\":\"approved\",\"confidence\":0.95}";

        // Act
        claimAIResult.setAiOutput(aiOutput);

        // Assert
        assertEquals(aiOutput, claimAIResult.getAiOutput());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        // Arrange
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);

        // Act
        claimAIResult.setCreatedAt(createdAt);

        // Assert
        assertEquals(createdAt, claimAIResult.getCreatedAt());
    }

    @Test
    void testCompleteClaimAIResult() {
        // Arrange
        claimAIResult.setId(999);
        claimAIResult.setPatientName("Jane Smith");
        claimAIResult.setPolicyNumber("POL-GOLD-456");
        claimAIResult.setHospitalName("General Hospital");
        claimAIResult.setInvoiceNumber("INV-2024-100");
        claimAIResult.setTotalAmount(new BigDecimal("2500.75"));
        claimAIResult.setCurrency("INR");
        claimAIResult.setConfidenceScore(new BigDecimal("0.92"));
        claimAIResult.setAiStatus("SUCCESS");
        claimAIResult.setAiOutput("{\"extracted\":true,\"fields\":5}");
        claimAIResult.setCreatedAt(LocalDateTime.of(2024, 1, 28, 14, 30));

        // Assert
        assertEquals(999, claimAIResult.getId());
        assertEquals("Jane Smith", claimAIResult.getPatientName());
        assertEquals("POL-GOLD-456", claimAIResult.getPolicyNumber());
        assertEquals("General Hospital", claimAIResult.getHospitalName());
        assertEquals("INV-2024-100", claimAIResult.getInvoiceNumber());
        assertEquals(0, new BigDecimal("2500.75").compareTo(claimAIResult.getTotalAmount()));
        assertEquals("INR", claimAIResult.getCurrency());
        assertEquals(0, new BigDecimal("0.92").compareTo(claimAIResult.getConfidenceScore()));
        assertEquals("SUCCESS", claimAIResult.getAiStatus());
        assertTrue(claimAIResult.getAiOutput().contains("extracted"));
        assertNotNull(claimAIResult.getCreatedAt());
    }

    @Test
    void testHighConfidenceResult() {
        // Arrange
        claimAIResult.setPatientName("High Confidence Patient");
        claimAIResult.setConfidenceScore(new BigDecimal("0.99"));
        claimAIResult.setAiStatus("VERIFIED");

        // Assert
        assertTrue(claimAIResult.getConfidenceScore().compareTo(new BigDecimal("0.95")) > 0);
        assertEquals("VERIFIED", claimAIResult.getAiStatus());
    }

    @Test
    void testLowConfidenceResult() {
        // Arrange
        claimAIResult.setPatientName("Low Confidence Patient");
        claimAIResult.setConfidenceScore(new BigDecimal("0.65"));
        claimAIResult.setAiStatus("NEEDS_REVIEW");

        // Assert
        assertTrue(claimAIResult.getConfidenceScore().compareTo(new BigDecimal("0.70")) < 0);
        assertEquals("NEEDS_REVIEW", claimAIResult.getAiStatus());
    }

    @Test
    void testDifferentCurrencies() {
        // Test USD
        claimAIResult.setCurrency("USD");
        claimAIResult.setTotalAmount(new BigDecimal("1000.00"));
        assertEquals("USD", claimAIResult.getCurrency());

        // Test INR
        claimAIResult.setCurrency("INR");
        claimAIResult.setTotalAmount(new BigDecimal("75000.00"));
        assertEquals("INR", claimAIResult.getCurrency());

        // Test EUR
        claimAIResult.setCurrency("EUR");
        claimAIResult.setTotalAmount(new BigDecimal("900.00"));
        assertEquals("EUR", claimAIResult.getCurrency());
    }

    @Test
    void testDifferentAiStatuses() {
        // Test PROCESSING
        claimAIResult.setAiStatus("PROCESSING");
        assertEquals("PROCESSING", claimAIResult.getAiStatus());

        // Test SUCCESS
        claimAIResult.setAiStatus("SUCCESS");
        assertEquals("SUCCESS", claimAIResult.getAiStatus());

        // Test FAILED
        claimAIResult.setAiStatus("FAILED");
        assertEquals("FAILED", claimAIResult.getAiStatus());

        // Test NEEDS_REVIEW
        claimAIResult.setAiStatus("NEEDS_REVIEW");
        assertEquals("NEEDS_REVIEW", claimAIResult.getAiStatus());
    }

    @Test
    void testComplexAiOutputJson() {
        // Arrange
        String complexJson = """
                {
                    "extractedFields": {
                        "patientName": {"value": "John Doe", "confidence": 0.98},
                        "invoiceNumber": {"value": "INV-001", "confidence": 0.95}
                    },
                    "validationStatus": "PASSED",
                    "warnings": []
                }
                """;

        // Act
        claimAIResult.setAiOutput(complexJson);

        // Assert
        assertNotNull(claimAIResult.getAiOutput());
        assertTrue(claimAIResult.getAiOutput().contains("extractedFields"));
        assertTrue(claimAIResult.getAiOutput().contains("PASSED"));
    }

    @Test
    void testNullValues() {
        // Act
        claimAIResult.setPatientName(null);
        claimAIResult.setPolicyNumber(null);
        claimAIResult.setTotalAmount(null);
        claimAIResult.setConfidenceScore(null);
        claimAIResult.setAiStatus(null);

        // Assert
        assertNull(claimAIResult.getPatientName());
        assertNull(claimAIResult.getPolicyNumber());
        assertNull(claimAIResult.getTotalAmount());
        assertNull(claimAIResult.getConfidenceScore());
        assertNull(claimAIResult.getAiStatus());
    }

    @Test
    void testZeroAmount() {
        // Arrange
        claimAIResult.setTotalAmount(BigDecimal.ZERO);

        // Assert
        assertEquals(BigDecimal.ZERO, claimAIResult.getTotalAmount());
    }

    @Test
    void testLargeAmount() {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("99999999.99");
        claimAIResult.setTotalAmount(largeAmount);

        // Assert
        assertEquals(largeAmount, claimAIResult.getTotalAmount());
    }

    @Test
    void testTimestampValues() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime future = LocalDateTime.of(2030, 12, 31, 23, 59);

        // Test current time
        claimAIResult.setCreatedAt(now);
        assertEquals(now, claimAIResult.getCreatedAt());

        // Test past time
        claimAIResult.setCreatedAt(past);
        assertEquals(past, claimAIResult.getCreatedAt());

        // Test future time
        claimAIResult.setCreatedAt(future);
        assertEquals(future, claimAIResult.getCreatedAt());
    }
}
