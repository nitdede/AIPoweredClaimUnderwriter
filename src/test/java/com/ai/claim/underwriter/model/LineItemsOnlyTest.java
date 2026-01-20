package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LineItemsOnlyTest {

    @Test
    void testConstructorAndGetter() {
        // Arrange
        ExtractedInvoice.LineItem lineItem = new ExtractedInvoice.LineItem("Consultation", 500.0, 0.95);
        List<ExtractedInvoice.LineItem> lineItems = Collections.singletonList(lineItem);

        // Act
        LineItemsOnly lineItemsOnly = new LineItemsOnly(lineItems);

        // Assert
        assertNotNull(lineItemsOnly);
        assertEquals(lineItems, lineItemsOnly.lineItems());
        assertEquals(1, lineItemsOnly.lineItems().size());
    }

    @Test
    void testConstructorWithNullValue() {
        // Act
        LineItemsOnly lineItemsOnly = new LineItemsOnly(null);

        // Assert
        assertNotNull(lineItemsOnly);
        assertNull(lineItemsOnly.lineItems());
    }

    @Test
    void testConstructorWithEmptyList() {
        // Arrange
        List<ExtractedInvoice.LineItem> emptyList = Collections.emptyList();

        // Act
        LineItemsOnly lineItemsOnly = new LineItemsOnly(emptyList);

        // Assert
        assertNotNull(lineItemsOnly);
        assertTrue(lineItemsOnly.lineItems().isEmpty());
    }

    @Test
    void testWithMultipleLineItems() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Arrays.asList(
                new ExtractedInvoice.LineItem("Consultation", 500.0, 0.95),
                new ExtractedInvoice.LineItem("Lab Test", 750.0, 0.92),
                new ExtractedInvoice.LineItem("X-Ray", 1000.0, 0.90)
        );

        // Act
        LineItemsOnly lineItemsOnly = new LineItemsOnly(lineItems);

        // Assert
        assertEquals(3, lineItemsOnly.lineItems().size());
        assertEquals("Consultation", lineItemsOnly.lineItems().get(0).desc());
        assertEquals(750.0, lineItemsOnly.lineItems().get(1).amount());
        assertEquals(0.90, lineItemsOnly.lineItems().get(2).confidence());
    }

    @Test
    void testEquals() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Collections.singletonList(
                new ExtractedInvoice.LineItem("Service", 100.0, 0.9)
        );
        LineItemsOnly items1 = new LineItemsOnly(lineItems);
        LineItemsOnly items2 = new LineItemsOnly(lineItems);
        LineItemsOnly items3 = new LineItemsOnly(Collections.emptyList());

        // Assert
        assertEquals(items1, items2);
        assertNotEquals(items1, items3);
    }

    @Test
    void testHashCode() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Collections.singletonList(
                new ExtractedInvoice.LineItem("Service", 100.0, 0.9)
        );
        LineItemsOnly items1 = new LineItemsOnly(lineItems);
        LineItemsOnly items2 = new LineItemsOnly(lineItems);

        // Assert
        assertEquals(items1.hashCode(), items2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Collections.singletonList(
                new ExtractedInvoice.LineItem("MRI Scan", 2500.0, 0.96)
        );
        LineItemsOnly lineItemsOnly = new LineItemsOnly(lineItems);

        // Act
        String result = lineItemsOnly.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("lineItems"));
    }

    @Test
    void testLineItemValues() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Arrays.asList(
                new ExtractedInvoice.LineItem("Emergency Room", 1200.0, 0.97),
                new ExtractedInvoice.LineItem("Medication", 300.0, 0.88)
        );

        // Act
        LineItemsOnly lineItemsOnly = new LineItemsOnly(lineItems);

        // Assert
        assertEquals(2, lineItemsOnly.lineItems().size());
        
        ExtractedInvoice.LineItem firstItem = lineItemsOnly.lineItems().get(0);
        assertEquals("Emergency Room", firstItem.desc());
        assertEquals(1200.0, firstItem.amount());
        assertEquals(0.97, firstItem.confidence());
        
        ExtractedInvoice.LineItem secondItem = lineItemsOnly.lineItems().get(1);
        assertEquals("Medication", secondItem.desc());
        assertEquals(300.0, secondItem.amount());
        assertEquals(0.88, secondItem.confidence());
    }

    @Test
    void testCompleteLineItemsOnly() {
        // Arrange
        List<ExtractedInvoice.LineItem> lineItems = Arrays.asList(
                new ExtractedInvoice.LineItem("Physical Examination", 200.0, 0.99),
                new ExtractedInvoice.LineItem("Blood Work", 350.0, 0.94),
                new ExtractedInvoice.LineItem("Ultrasound", 800.0, 0.91),
                new ExtractedInvoice.LineItem("Prescription Drugs", 150.0, 0.87)
        );

        // Act
        LineItemsOnly lineItemsOnly = new LineItemsOnly(lineItems);

        // Assert
        assertEquals(4, lineItemsOnly.lineItems().size());
        
        // Verify sum of amounts
        double totalAmount = lineItemsOnly.lineItems().stream()
                .mapToDouble(ExtractedInvoice.LineItem::amount)
                .sum();
        assertEquals(1500.0, totalAmount, 0.01);
        
        // Verify all items have confidence scores
        boolean allHaveConfidence = lineItemsOnly.lineItems().stream()
                .allMatch(item -> item.confidence() != null && item.confidence() > 0);
        assertTrue(allHaveConfidence);
    }
}
