package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ClaimExtractionResultTest {

    private ClaimExtractionResult result;

    @BeforeEach
    void setUp() {
        result = new ClaimExtractionResult();
    }

    @Test
    void testDefaultConstructor() {
        // Assert
        assertNotNull(result);
        assertNull(result.patientName);
        assertNull(result.policyNumber);
        assertNull(result.hospitalName);
        assertNull(result.invoiceNumber);
        assertNull(result.dateOfService);
        assertNull(result.currency);
        assertNull(result.lineItems);
        assertNull(result.totalAmount);
        assertNull(result.confidence);
    }

    @Test
    void testPatientNameGetterAndSetter() {
        // Arrange
        String patientName = "John Doe";

        // Act
        result.patientName = patientName;

        // Assert
        assertEquals(patientName, result.patientName);
    }

    @Test
    void testPolicyNumberGetterAndSetter() {
        // Arrange
        String policyNumber = "POL123456";

        // Act
        result.policyNumber = policyNumber;

        // Assert
        assertEquals(policyNumber, result.policyNumber);
    }

    @Test
    void testHospitalNameGetterAndSetter() {
        // Arrange
        String hospitalName = "City Hospital";

        // Act
        result.hospitalName = hospitalName;

        // Assert
        assertEquals(hospitalName, result.hospitalName);
    }

    @Test
    void testInvoiceNumberGetterAndSetter() {
        // Arrange
        String invoiceNumber = "INV-2024-001";

        // Act
        result.invoiceNumber = invoiceNumber;

        // Assert
        assertEquals(invoiceNumber, result.invoiceNumber);
    }

    @Test
    void testDateOfServiceGetterAndSetter() {
        // Arrange
        String dateOfService = "2024-01-15";

        // Act
        result.dateOfService = dateOfService;

        // Assert
        assertEquals(dateOfService, result.dateOfService);
    }

    @Test
    void testCurrencyGetterAndSetter() {
        // Arrange
        String currency = "USD";

        // Act
        result.currency = currency;

        // Assert
        assertEquals(currency, result.currency);
    }

    @Test
    void testTotalAmountGetterAndSetter() {
        // Arrange
        Double totalAmount = 1500.50;

        // Act
        result.totalAmount = totalAmount;

        // Assert
        assertEquals(totalAmount, result.totalAmount);
    }

    @Test
    void testLineItemsGetterAndSetter() {
        // Arrange
        ClaimExtractionResult.LineItem lineItem = new ClaimExtractionResult.LineItem();
        lineItem.description = "Consultation";
        lineItem.amount = 100.0;
        lineItem.confidence = 0.95;
        
        List<ClaimExtractionResult.LineItem> lineItems = new ArrayList<>();
        lineItems.add(lineItem);

        // Act
        result.lineItems = lineItems;

        // Assert
        assertEquals(lineItems, result.lineItems);
        assertEquals(1, result.lineItems.size());
        assertEquals("Consultation", result.lineItems.get(0).description);
    }

    @Test
    void testConfidenceMapGetterAndSetter() {
        // Arrange
        Map<String, Double> confidenceMap = new HashMap<>();
        confidenceMap.put("patientName", 0.98);
        confidenceMap.put("totalAmount", 0.95);

        // Act
        result.confidence = confidenceMap;

        // Assert
        assertEquals(confidenceMap, result.confidence);
        assertEquals(0.98, result.confidence.get("patientName"));
    }

    @Test
    void testLineItemFields() {
        // Arrange
        ClaimExtractionResult.LineItem lineItem = new ClaimExtractionResult.LineItem();

        // Act
        lineItem.description = "Lab Test";
        lineItem.amount = 250.75;
        lineItem.confidence = 0.92;

        // Assert
        assertEquals("Lab Test", lineItem.description);
        assertEquals(250.75, lineItem.amount);
        assertEquals(0.92, lineItem.confidence);
    }

    @Test
    void testCompleteClaimExtractionResult() {
        // Arrange
        result.patientName = "Jane Smith";
        result.policyNumber = "POL987654";
        result.hospitalName = "General Hospital";
        result.invoiceNumber = "INV-2024-002";
        result.dateOfService = "2024-01-20";
        result.currency = "INR";
        result.totalAmount = 5000.0;

        ClaimExtractionResult.LineItem lineItem1 = new ClaimExtractionResult.LineItem();
        lineItem1.description = "X-Ray";
        lineItem1.amount = 2000.0;
        lineItem1.confidence = 0.96;

        ClaimExtractionResult.LineItem lineItem2 = new ClaimExtractionResult.LineItem();
        lineItem2.description = "Medication";
        lineItem2.amount = 3000.0;
        lineItem2.confidence = 0.94;

        result.lineItems = Arrays.asList(lineItem1, lineItem2);

        Map<String, Double> confidenceMap = new HashMap<>();
        confidenceMap.put("patientName", 0.99);
        confidenceMap.put("totalAmount", 0.97);
        result.confidence = confidenceMap;

        // Assert
        assertEquals("Jane Smith", result.patientName);
        assertEquals("POL987654", result.policyNumber);
        assertEquals(5000.0, result.totalAmount);
        assertEquals(2, result.lineItems.size());
        assertEquals(2, result.confidence.size());
    }

    @Test
    void testLineItemWithNullValues() {
        // Arrange
        ClaimExtractionResult.LineItem lineItem = new ClaimExtractionResult.LineItem();

        // Assert
        assertNull(lineItem.description);
        assertNull(lineItem.amount);
        assertNull(lineItem.confidence);
    }
}
