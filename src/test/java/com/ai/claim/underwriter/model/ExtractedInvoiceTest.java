package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ExtractedInvoiceTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String patientName = "John Doe";
        String invoiceNumber = "INV-2024-001";
        String dateOfService = "2024-01-15";
        Double totalAmount = 1500.50;
        String currency = "USD";
        String hospitalName = "City Hospital";
        
        ExtractedInvoice.LineItem lineItem = new ExtractedInvoice.LineItem("Consultation", 500.0, 0.95);
        List<ExtractedInvoice.LineItem> lineItems = Collections.singletonList(lineItem);
        
        Map<String, Double> confidence = new HashMap<>();
        confidence.put("patientName", 0.98);

        // Act
        ExtractedInvoice invoice = new ExtractedInvoice(
                patientName, invoiceNumber, dateOfService, totalAmount, 
                currency, hospitalName, lineItems, confidence
        );

        // Assert
        assertNotNull(invoice);
        assertEquals(patientName, invoice.patientName());
        assertEquals(invoiceNumber, invoice.invoiceNumber());
        assertEquals(dateOfService, invoice.dateOfService());
        assertEquals(totalAmount, invoice.totalAmount());
        assertEquals(currency, invoice.currency());
        assertEquals(hospitalName, invoice.hospitalName());
        assertEquals(lineItems, invoice.lineItems());
        assertEquals(confidence, invoice.confidence());
    }

    @Test
    void testConstructorWithNullValues() {
        // Act
        ExtractedInvoice invoice = new ExtractedInvoice(
                null, null, null, null, null, null, null, null
        );

        // Assert
        assertNotNull(invoice);
        assertNull(invoice.patientName());
        assertNull(invoice.invoiceNumber());
        assertNull(invoice.dateOfService());
        assertNull(invoice.totalAmount());
        assertNull(invoice.currency());
        assertNull(invoice.hospitalName());
        assertNull(invoice.lineItems());
        assertNull(invoice.confidence());
    }

    @Test
    void testLineItemConstructorAndGetters() {
        // Arrange
        String desc = "X-Ray";
        Double amount = 750.0;
        Double confidence = 0.92;

        // Act
        ExtractedInvoice.LineItem lineItem = new ExtractedInvoice.LineItem(desc, amount, confidence);

        // Assert
        assertNotNull(lineItem);
        assertEquals(desc, lineItem.desc());
        assertEquals(amount, lineItem.amount());
        assertEquals(confidence, lineItem.confidence());
    }

    @Test
    void testLineItemWithNullValues() {
        // Act
        ExtractedInvoice.LineItem lineItem = new ExtractedInvoice.LineItem(null, null, null);

        // Assert
        assertNotNull(lineItem);
        assertNull(lineItem.desc());
        assertNull(lineItem.amount());
        assertNull(lineItem.confidence());
    }

    @Test
    void testLineItemEquals() {
        // Arrange
        ExtractedInvoice.LineItem item1 = new ExtractedInvoice.LineItem("Lab Test", 200.0, 0.90);
        ExtractedInvoice.LineItem item2 = new ExtractedInvoice.LineItem("Lab Test", 200.0, 0.90);
        ExtractedInvoice.LineItem item3 = new ExtractedInvoice.LineItem("Blood Test", 300.0, 0.85);

        // Assert
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
    }

    @Test
    void testLineItemHashCode() {
        // Arrange
        ExtractedInvoice.LineItem item1 = new ExtractedInvoice.LineItem("MRI", 1000.0, 0.95);
        ExtractedInvoice.LineItem item2 = new ExtractedInvoice.LineItem("MRI", 1000.0, 0.95);

        // Assert
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void testEquals() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Collections.singletonList(
                new ExtractedInvoice.LineItem("Service", 100.0, 0.9)
        );
        Map<String, Double> confidence = new HashMap<>();
        confidence.put("test", 0.95);

        ExtractedInvoice invoice1 = new ExtractedInvoice(
                "Patient A", "INV-001", "2024-01-01", 100.0, "USD", "Hospital A", lineItems, confidence
        );
        ExtractedInvoice invoice2 = new ExtractedInvoice(
                "Patient A", "INV-001", "2024-01-01", 100.0, "USD", "Hospital A", lineItems, confidence
        );
        ExtractedInvoice invoice3 = new ExtractedInvoice(
                "Patient B", "INV-002", "2024-01-02", 200.0, "INR", "Hospital B", lineItems, confidence
        );

        // Assert
        assertEquals(invoice1, invoice2);
        assertNotEquals(invoice1, invoice3);
    }

    @Test
    void testHashCode() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Collections.singletonList(
                new ExtractedInvoice.LineItem("Service", 100.0, 0.9)
        );
        Map<String, Double> confidence = new HashMap<>();

        ExtractedInvoice invoice1 = new ExtractedInvoice(
                "Patient A", "INV-001", "2024-01-01", 100.0, "USD", "Hospital A", lineItems, confidence
        );
        ExtractedInvoice invoice2 = new ExtractedInvoice(
                "Patient A", "INV-001", "2024-01-01", 100.0, "USD", "Hospital A", lineItems, confidence
        );

        // Assert
        assertEquals(invoice1.hashCode(), invoice2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        ExtractedInvoice invoice = new ExtractedInvoice(
                "Jane Smith", "INV-999", "2024-01-20", 2500.0, "INR", "General Hospital",
                Collections.emptyList(), Collections.emptyMap()
        );

        // Act
        String result = invoice.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Jane Smith"));
        assertTrue(result.contains("INV-999"));
    }

    @Test
    void testWithMultipleLineItems() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Arrays.asList(
                new ExtractedInvoice.LineItem("Consultation", 500.0, 0.95),
                new ExtractedInvoice.LineItem("Lab Tests", 750.0, 0.92),
                new ExtractedInvoice.LineItem("Medication", 250.0, 0.88)
        );

        Map<String, Double> confidence = new HashMap<>();
        confidence.put("patientName", 0.99);
        confidence.put("totalAmount", 0.97);

        // Act
        ExtractedInvoice invoice = new ExtractedInvoice(
                "Alice Johnson", "INV-2024-100", "2024-01-25", 1500.0, "USD",
                "Medical Center", lineItems, confidence
        );

        // Assert
        assertEquals(3, invoice.lineItems().size());
        assertEquals("Consultation", invoice.lineItems().get(0).desc());
        assertEquals(750.0, invoice.lineItems().get(1).amount());
        assertEquals(0.88, invoice.lineItems().get(2).confidence());
    }

    @Test
    void testWithEmptyLineItems() {
        // Arrange
        List<ExtractedInvoice.LineItem> emptyLineItems = Collections.emptyList();
        Map<String, Double> confidence = Collections.emptyMap();

        // Act
        ExtractedInvoice invoice = new ExtractedInvoice(
                "Test Patient", "INV-EMPTY", "2024-01-01", 0.0, "USD",
                "Test Hospital", emptyLineItems, confidence
        );

        // Assert
        assertTrue(invoice.lineItems().isEmpty());
        assertTrue(invoice.confidence().isEmpty());
    }

    @Test
    void testCompleteInvoice() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Arrays.asList(
                new ExtractedInvoice.LineItem("Emergency Room", 1200.0, 0.96),
                new ExtractedInvoice.LineItem("CT Scan", 2500.0, 0.94),
                new ExtractedInvoice.LineItem("Medications", 800.0, 0.91)
        );

        Map<String, Double> confidence = new HashMap<>();
        confidence.put("patientName", 0.99);
        confidence.put("invoiceNumber", 0.98);
        confidence.put("totalAmount", 0.97);
        confidence.put("hospitalName", 0.96);

        // Act
        ExtractedInvoice invoice = new ExtractedInvoice(
                "Robert Williams",
                "INV-2024-GOLD-789",
                "2024-01-28",
                4500.0,
                "INR",
                "Premier Healthcare Hospital",
                lineItems,
                confidence
        );

        // Assert
        assertEquals("Robert Williams", invoice.patientName());
        assertEquals("INV-2024-GOLD-789", invoice.invoiceNumber());
        assertEquals(4500.0, invoice.totalAmount());
        assertEquals(3, invoice.lineItems().size());
        assertEquals(4, invoice.confidence().size());
        assertEquals(0.99, invoice.confidence().get("patientName"));
    }
}
