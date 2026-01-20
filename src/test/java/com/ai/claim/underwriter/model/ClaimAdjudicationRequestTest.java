package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClaimAdjudicationRequestTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String patientName = "John Doe";
        long claimId = 12345L;
        String policyNumber = "POL-98765";
        String invoiceSummaryText = "Medical consultation and lab tests";
        int topK = 5;

        // Act
        ClaimAdjudicationRequest request = new ClaimAdjudicationRequest(
                patientName, claimId, policyNumber, invoiceSummaryText, topK
        );

        // Assert
        assertNotNull(request);
        assertEquals(patientName, request.patientName());
        assertEquals(claimId, request.claimId());
        assertEquals(policyNumber, request.policyNumber());
        assertEquals(invoiceSummaryText, request.invoiceSummaryText());
        assertEquals(topK, request.topK());
    }

    @Test
    void testConstructorWithNullValues() {
        // Act
        ClaimAdjudicationRequest request = new ClaimAdjudicationRequest(
                null, 0L, null, null, 0
        );

        // Assert
        assertNotNull(request);
        assertNull(request.patientName());
        assertEquals(0L, request.claimId());
        assertNull(request.policyNumber());
        assertNull(request.invoiceSummaryText());
        assertEquals(0, request.topK());
    }

    @Test
    void testEquals() {
        // Arrange
        ClaimAdjudicationRequest request1 = new ClaimAdjudicationRequest(
                "Patient A", 100L, "POL-001", "Summary 1", 5
        );
        ClaimAdjudicationRequest request2 = new ClaimAdjudicationRequest(
                "Patient A", 100L, "POL-001", "Summary 1", 5
        );
        ClaimAdjudicationRequest request3 = new ClaimAdjudicationRequest(
                "Patient B", 200L, "POL-002", "Summary 2", 10
        );

        // Assert
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
    }

    @Test
    void testHashCode() {
        // Arrange
        ClaimAdjudicationRequest request1 = new ClaimAdjudicationRequest(
                "Patient A", 100L, "POL-001", "Summary 1", 5
        );
        ClaimAdjudicationRequest request2 = new ClaimAdjudicationRequest(
                "Patient A", 100L, "POL-001", "Summary 1", 5
        );

        // Assert
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        ClaimAdjudicationRequest request = new ClaimAdjudicationRequest(
                "Jane Smith", 999L, "POL-456", "X-Ray and consultation", 3
        );

        // Act
        String result = request.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Jane Smith"));
        assertTrue(result.contains("999"));
        assertTrue(result.contains("POL-456"));
    }

    @Test
    void testWithDifferentTopKValues() {
        // Arrange & Act
        ClaimAdjudicationRequest request1 = new ClaimAdjudicationRequest(
                "Patient 1", 1L, "POL-1", "Summary", 1
        );
        ClaimAdjudicationRequest request2 = new ClaimAdjudicationRequest(
                "Patient 2", 2L, "POL-2", "Summary", 10
        );
        ClaimAdjudicationRequest request3 = new ClaimAdjudicationRequest(
                "Patient 3", 3L, "POL-3", "Summary", 100
        );

        // Assert
        assertEquals(1, request1.topK());
        assertEquals(10, request2.topK());
        assertEquals(100, request3.topK());
    }

    @Test
    void testWithEmptyStrings() {
        // Act
        ClaimAdjudicationRequest request = new ClaimAdjudicationRequest(
                "", 0L, "", "", 0
        );

        // Assert
        assertNotNull(request);
        assertEquals("", request.patientName());
        assertEquals("", request.policyNumber());
        assertEquals("", request.invoiceSummaryText());
    }

    @Test
    void testCompleteRequest() {
        // Arrange
        String patientName = "Alice Johnson";
        long claimId = 54321L;
        String policyNumber = "POL-GOLD-123";
        String invoiceSummary = "Emergency room visit, CT scan, medication prescribed";
        int topK = 7;

        // Act
        ClaimAdjudicationRequest request = new ClaimAdjudicationRequest(
                patientName, claimId, policyNumber, invoiceSummary, topK
        );

        // Assert
        assertEquals("Alice Johnson", request.patientName());
        assertEquals(54321L, request.claimId());
        assertEquals("POL-GOLD-123", request.policyNumber());
        assertTrue(request.invoiceSummaryText().contains("Emergency room"));
        assertEquals(7, request.topK());
    }
}
