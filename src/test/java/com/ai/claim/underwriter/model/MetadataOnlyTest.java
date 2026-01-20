package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetadataOnlyTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String patientName = "John Doe";
        String invoiceNumber = "INV-2024-001";
        String dateOfService = "2024-01-15";
        Double totalAmount = 1500.50;
        String currency = "USD";
        String hospitalName = "City Hospital";

        // Act
        MetadataOnly metadata = new MetadataOnly(
                patientName, invoiceNumber, dateOfService, totalAmount, currency, hospitalName
        );

        // Assert
        assertNotNull(metadata);
        assertEquals(patientName, metadata.patientName());
        assertEquals(invoiceNumber, metadata.invoiceNumber());
        assertEquals(dateOfService, metadata.dateOfService());
        assertEquals(totalAmount, metadata.totalAmount());
        assertEquals(currency, metadata.currency());
        assertEquals(hospitalName, metadata.hospitalName());
    }

    @Test
    void testConstructorWithNullValues() {
        // Act
        MetadataOnly metadata = new MetadataOnly(null, null, null, null, null, null);

        // Assert
        assertNotNull(metadata);
        assertNull(metadata.patientName());
        assertNull(metadata.invoiceNumber());
        assertNull(metadata.dateOfService());
        assertNull(metadata.totalAmount());
        assertNull(metadata.currency());
        assertNull(metadata.hospitalName());
    }

    @Test
    void testEquals() {
        // Arrange
        MetadataOnly metadata1 = new MetadataOnly(
                "Patient A", "INV-001", "2024-01-01", 100.0, "USD", "Hospital A"
        );
        MetadataOnly metadata2 = new MetadataOnly(
                "Patient A", "INV-001", "2024-01-01", 100.0, "USD", "Hospital A"
        );
        MetadataOnly metadata3 = new MetadataOnly(
                "Patient B", "INV-002", "2024-01-02", 200.0, "INR", "Hospital B"
        );

        // Assert
        assertEquals(metadata1, metadata2);
        assertNotEquals(metadata1, metadata3);
    }

    @Test
    void testHashCode() {
        // Arrange
        MetadataOnly metadata1 = new MetadataOnly(
                "Patient A", "INV-001", "2024-01-01", 100.0, "USD", "Hospital A"
        );
        MetadataOnly metadata2 = new MetadataOnly(
                "Patient A", "INV-001", "2024-01-01", 100.0, "USD", "Hospital A"
        );

        // Assert
        assertEquals(metadata1.hashCode(), metadata2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        MetadataOnly metadata = new MetadataOnly(
                "Jane Smith", "INV-2024-100", "2024-01-20", 2500.75, "INR", "General Hospital"
        );

        // Act
        String result = metadata.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Jane Smith"));
        assertTrue(result.contains("INV-2024-100"));
    }

    @Test
    void testWithDifferentCurrencies() {
        // Arrange
        MetadataOnly usdMetadata = new MetadataOnly(
                "Patient 1", "INV-1", "2024-01-01", 1000.0, "USD", "Hospital 1"
        );
        MetadataOnly inrMetadata = new MetadataOnly(
                "Patient 2", "INV-2", "2024-01-02", 75000.0, "INR", "Hospital 2"
        );
        MetadataOnly eurMetadata = new MetadataOnly(
                "Patient 3", "INV-3", "2024-01-03", 900.0, "EUR", "Hospital 3"
        );

        // Assert
        assertEquals("USD", usdMetadata.currency());
        assertEquals("INR", inrMetadata.currency());
        assertEquals("EUR", eurMetadata.currency());
    }

    @Test
    void testWithDateFormats() {
        // Arrange
        MetadataOnly metadata = new MetadataOnly(
                "Test Patient", "INV-999", "2024-12-31", 500.0, "USD", "Test Hospital"
        );

        // Assert
        assertEquals("2024-12-31", metadata.dateOfService());
    }

    @Test
    void testWithZeroAmount() {
        // Arrange
        MetadataOnly metadata = new MetadataOnly(
                "Free Service Patient", "INV-FREE", "2024-01-01", 0.0, "USD", "Charity Hospital"
        );

        // Assert
        assertEquals(0.0, metadata.totalAmount());
    }

    @Test
    void testCompleteMetadata() {
        // Arrange & Act
        MetadataOnly metadata = new MetadataOnly(
                "Alice Johnson",
                "INV-2024-GOLD-456",
                "2024-01-28",
                12500.99,
                "INR",
                "Metropolitan Medical Center"
        );

        // Assert
        assertEquals("Alice Johnson", metadata.patientName());
        assertEquals("INV-2024-GOLD-456", metadata.invoiceNumber());
        assertEquals("2024-01-28", metadata.dateOfService());
        assertEquals(12500.99, metadata.totalAmount());
        assertEquals("INR", metadata.currency());
        assertEquals("Metropolitan Medical Center", metadata.hospitalName());
    }
}
