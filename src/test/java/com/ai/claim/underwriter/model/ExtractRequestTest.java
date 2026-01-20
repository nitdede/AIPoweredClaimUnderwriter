package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtractRequestTest {

    @Test
    void testConstructorAndGetter() {
        // Arrange
        String invoiceText = "Sample invoice text";

        // Act
        ExtractRequest request = new ExtractRequest(invoiceText);

        // Assert
        assertNotNull(request);
        assertEquals(invoiceText, request.invoiceText());
    }

    @Test
    void testConstructorWithNullValue() {
        // Arrange & Act
        ExtractRequest request = new ExtractRequest(null);

        // Assert
        assertNotNull(request);
        assertNull(request.invoiceText());
    }

    @Test
    void testEquals() {
        // Arrange
        ExtractRequest request1 = new ExtractRequest("Invoice 1");
        ExtractRequest request2 = new ExtractRequest("Invoice 1");
        ExtractRequest request3 = new ExtractRequest("Invoice 2");

        // Assert
        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
    }

    @Test
    void testHashCode() {
        // Arrange
        ExtractRequest request1 = new ExtractRequest("Invoice 1");
        ExtractRequest request2 = new ExtractRequest("Invoice 1");

        // Assert
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        ExtractRequest request = new ExtractRequest("Invoice text");

        // Act
        String result = request.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Invoice text"));
    }
}
