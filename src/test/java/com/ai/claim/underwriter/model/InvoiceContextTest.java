package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceContextTest {

    private InvoiceContext invoiceContext;
    private ExtractedInvoice sampleInvoice;

    @BeforeEach
    void setUp() {
        invoiceContext = new InvoiceContext();
        sampleInvoice = new ExtractedInvoice(
                "John Doe", "INV-001", "2024-01-15", 1000.0,
                "USD", "City Hospital", Collections.emptyList(), Collections.emptyMap()
        );
    }

    @AfterEach
    void tearDown() {
        // Clean up ThreadLocal to prevent memory leaks in tests
        invoiceContext.clear();
    }

    @Test
    void testSetAndGetLastExtractedInvoice() {
        // Act
        invoiceContext.setLastExtractedInvoice(sampleInvoice);
        ExtractedInvoice retrieved = invoiceContext.getLastExtractedInvoice();

        // Assert
        assertNotNull(retrieved);
        assertEquals(sampleInvoice, retrieved);
        assertEquals("John Doe", retrieved.patientName());
        assertEquals("INV-001", retrieved.invoiceNumber());
    }

    @Test
    void testGetLastExtractedInvoiceWhenNull() {
        // Act
        ExtractedInvoice retrieved = invoiceContext.getLastExtractedInvoice();

        // Assert
        assertNull(retrieved);
    }

    @Test
    void testClear() {
        // Arrange
        invoiceContext.setLastExtractedInvoice(sampleInvoice);
        assertNotNull(invoiceContext.getLastExtractedInvoice());

        // Act
        invoiceContext.clear();
        ExtractedInvoice retrieved = invoiceContext.getLastExtractedInvoice();

        // Assert
        assertNull(retrieved);
    }

    @Test
    void testSetNullInvoice() {
        // Act
        invoiceContext.setLastExtractedInvoice(null);
        ExtractedInvoice retrieved = invoiceContext.getLastExtractedInvoice();

        // Assert
        assertNull(retrieved);
    }

    @Test
    void testOverwriteExistingInvoice() {
        // Arrange
        ExtractedInvoice firstInvoice = new ExtractedInvoice(
                "Patient 1", "INV-001", "2024-01-01", 500.0,
                "USD", "Hospital 1", Collections.emptyList(), Collections.emptyMap()
        );
        ExtractedInvoice secondInvoice = new ExtractedInvoice(
                "Patient 2", "INV-002", "2024-01-02", 1000.0,
                "INR", "Hospital 2", Collections.emptyList(), Collections.emptyMap()
        );

        // Act
        invoiceContext.setLastExtractedInvoice(firstInvoice);
        assertEquals("Patient 1", invoiceContext.getLastExtractedInvoice().patientName());

        invoiceContext.setLastExtractedInvoice(secondInvoice);
        ExtractedInvoice retrieved = invoiceContext.getLastExtractedInvoice();

        // Assert
        assertEquals("Patient 2", retrieved.patientName());
        assertEquals("INV-002", retrieved.invoiceNumber());
    }

    @Test
    void testThreadLocalIsolation() throws InterruptedException {
        // Arrange
        ExtractedInvoice mainThreadInvoice = new ExtractedInvoice(
                "Main Thread Patient", "INV-MAIN", "2024-01-01", 1000.0,
                "USD", "Main Hospital", Collections.emptyList(), Collections.emptyMap()
        );

        final ExtractedInvoice[] otherThreadInvoice = new ExtractedInvoice[1];
        final ExtractedInvoice[] otherThreadRetrieved = new ExtractedInvoice[1];

        // Act
        invoiceContext.setLastExtractedInvoice(mainThreadInvoice);

        Thread otherThread = new Thread(() -> {
            otherThreadInvoice[0] = new ExtractedInvoice(
                    "Other Thread Patient", "INV-OTHER", "2024-01-02", 2000.0,
                    "INR", "Other Hospital", Collections.emptyList(), Collections.emptyMap()
            );
            invoiceContext.setLastExtractedInvoice(otherThreadInvoice[0]);
            otherThreadRetrieved[0] = invoiceContext.getLastExtractedInvoice();
        });

        otherThread.start();
        otherThread.join();

        ExtractedInvoice mainThreadRetrieved = invoiceContext.getLastExtractedInvoice();

        // Assert
        assertEquals("Main Thread Patient", mainThreadRetrieved.patientName());
        assertEquals("Other Thread Patient", otherThreadRetrieved[0].patientName());
        assertNotEquals(mainThreadRetrieved, otherThreadRetrieved[0]);
    }

    @Test
    void testClearDoesNotAffectOtherThreads() throws InterruptedException {
        // Arrange
        ExtractedInvoice mainThreadInvoice = new ExtractedInvoice(
                "Main Patient", "INV-M", "2024-01-01", 1000.0,
                "USD", "Main Hospital", Collections.emptyList(), Collections.emptyMap()
        );

        final ExtractedInvoice[] otherThreadRetrieved = new ExtractedInvoice[1];

        // Act
        invoiceContext.setLastExtractedInvoice(mainThreadInvoice);

        Thread otherThread = new Thread(() -> {
            ExtractedInvoice otherInvoice = new ExtractedInvoice(
                    "Other Patient", "INV-O", "2024-01-02", 2000.0,
                    "INR", "Other Hospital", Collections.emptyList(), Collections.emptyMap()
            );
            invoiceContext.setLastExtractedInvoice(otherInvoice);
            
            // Main thread clears its context (simulated by test)
            try {
                Thread.sleep(100); // Give time for main thread to clear
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            otherThreadRetrieved[0] = invoiceContext.getLastExtractedInvoice();
        });

        otherThread.start();
        Thread.sleep(50);
        invoiceContext.clear(); // Clear main thread context
        otherThread.join();

        // Assert
        assertNull(invoiceContext.getLastExtractedInvoice()); // Main thread cleared
        assertNotNull(otherThreadRetrieved[0]); // Other thread not affected
        assertEquals("Other Patient", otherThreadRetrieved[0].patientName());
    }

    @Test
    void testMultipleSetAndClearCycles() {
        // Arrange
        ExtractedInvoice invoice1 = new ExtractedInvoice(
                "Patient 1", "INV-1", "2024-01-01", 100.0,
                "USD", "Hospital 1", Collections.emptyList(), Collections.emptyMap()
        );
        ExtractedInvoice invoice2 = new ExtractedInvoice(
                "Patient 2", "INV-2", "2024-01-02", 200.0,
                "USD", "Hospital 2", Collections.emptyList(), Collections.emptyMap()
        );

        // Act & Assert - Cycle 1
        invoiceContext.setLastExtractedInvoice(invoice1);
        assertEquals("Patient 1", invoiceContext.getLastExtractedInvoice().patientName());
        invoiceContext.clear();
        assertNull(invoiceContext.getLastExtractedInvoice());

        // Act & Assert - Cycle 2
        invoiceContext.setLastExtractedInvoice(invoice2);
        assertEquals("Patient 2", invoiceContext.getLastExtractedInvoice().patientName());
        invoiceContext.clear();
        assertNull(invoiceContext.getLastExtractedInvoice());
    }
}
