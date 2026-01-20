package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtractResponseTest {

    private ClaimExtractionResult sampleResult;

    @BeforeEach
    void setUp() {
        sampleResult = new ClaimExtractionResult();
        sampleResult.patientName = "John Doe";
        sampleResult.policyNumber = "POL123";
        sampleResult.totalAmount = 1000.0;
    }

    @Test
    void testDefaultConstructor() {
        // Act
        ExtractResponse response = new ExtractResponse();

        // Assert
        assertNotNull(response);
        assertNull(response.status);
        assertNull(response.result);
        assertNull(response.message);
    }

    @Test
    void testOkFactoryMethod() {
        // Act
        ExtractResponse response = ExtractResponse.ok(sampleResult);

        // Assert
        assertNotNull(response);
        assertEquals("OK", response.status);
        assertEquals(sampleResult, response.result);
        assertEquals("Extraction completed.", response.message);
    }

    @Test
    void testOkFactoryMethodWithNullResult() {
        // Act
        ExtractResponse response = ExtractResponse.ok(null);

        // Assert
        assertNotNull(response);
        assertEquals("OK", response.status);
        assertNull(response.result);
        assertEquals("Extraction completed.", response.message);
    }

    @Test
    void testNeedsInfoFactoryMethod() {
        // Arrange
        String customMessage = "Additional patient information required";

        // Act
        ExtractResponse response = ExtractResponse.needsInfo(sampleResult, customMessage);

        // Assert
        assertNotNull(response);
        assertEquals("NEEDS_INFO", response.status);
        assertEquals(sampleResult, response.result);
        assertEquals(customMessage, response.message);
    }

    @Test
    void testNeedsInfoFactoryMethodWithNullResult() {
        // Arrange
        String customMessage = "Missing invoice number";

        // Act
        ExtractResponse response = ExtractResponse.needsInfo(null, customMessage);

        // Assert
        assertNotNull(response);
        assertEquals("NEEDS_INFO", response.status);
        assertNull(response.result);
        assertEquals(customMessage, response.message);
    }

    @Test
    void testNeedsInfoFactoryMethodWithNullMessage() {
        // Act
        ExtractResponse response = ExtractResponse.needsInfo(sampleResult, null);

        // Assert
        assertNotNull(response);
        assertEquals("NEEDS_INFO", response.status);
        assertEquals(sampleResult, response.result);
        assertNull(response.message);
    }

    @Test
    void testStatusGetterAndSetter() {
        // Arrange
        ExtractResponse response = new ExtractResponse();
        String status = "PROCESSING";

        // Act
        response.status = status;

        // Assert
        assertEquals(status, response.status);
    }

    @Test
    void testResultGetterAndSetter() {
        // Arrange
        ExtractResponse response = new ExtractResponse();

        // Act
        response.result = sampleResult;

        // Assert
        assertEquals(sampleResult, response.result);
    }

    @Test
    void testMessageGetterAndSetter() {
        // Arrange
        ExtractResponse response = new ExtractResponse();
        String message = "Custom message";

        // Act
        response.message = message;

        // Assert
        assertEquals(message, response.message);
    }

    @Test
    void testCompleteOkResponse() {
        // Arrange
        ClaimExtractionResult detailedResult = new ClaimExtractionResult();
        detailedResult.patientName = "Jane Smith";
        detailedResult.policyNumber = "POL456";
        detailedResult.hospitalName = "City Hospital";
        detailedResult.totalAmount = 2500.0;
        detailedResult.currency = "USD";

        // Act
        ExtractResponse response = ExtractResponse.ok(detailedResult);

        // Assert
        assertEquals("OK", response.status);
        assertEquals("Jane Smith", response.result.patientName);
        assertEquals("POL456", response.result.policyNumber);
        assertEquals(2500.0, response.result.totalAmount);
    }

    @Test
    void testCompleteNeedsInfoResponse() {
        // Arrange
        ClaimExtractionResult partialResult = new ClaimExtractionResult();
        partialResult.patientName = "Alice Johnson";
        String infoMessage = "Policy number is missing or unclear";

        // Act
        ExtractResponse response = ExtractResponse.needsInfo(partialResult, infoMessage);

        // Assert
        assertEquals("NEEDS_INFO", response.status);
        assertEquals("Alice Johnson", response.result.patientName);
        assertEquals(infoMessage, response.message);
    }
}
