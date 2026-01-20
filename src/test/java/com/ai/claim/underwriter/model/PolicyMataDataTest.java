package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolicyMataDataTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String policyId = "12345";
        String customerId = "CUST-67890";
        String policyNumber = "POL-ABC-123";

        // Act
        PolicyMataData policyData = new PolicyMataData(policyId, customerId, policyNumber);

        // Assert
        assertNotNull(policyData);
        assertEquals(policyId, policyData.policyId());
        assertEquals(customerId, policyData.customerId());
        assertEquals(policyNumber, policyData.policyNumber());
    }

    @Test
    void testConstructorWithNullValues() {
        // Act
        PolicyMataData policyData = new PolicyMataData(null, null, null);

        // Assert
        assertNotNull(policyData);
        assertNull(policyData.policyId());
        assertNull(policyData.customerId());
        assertNull(policyData.policyNumber());
    }

    @Test
    void testEquals() {
        // Arrange
        PolicyMataData data1 = new PolicyMataData("ID-1", "CUST-1", "POL-1");
        PolicyMataData data2 = new PolicyMataData("ID-1", "CUST-1", "POL-1");
        PolicyMataData data3 = new PolicyMataData("ID-2", "CUST-2", "POL-2");

        // Assert
        assertEquals(data1, data2);
        assertNotEquals(data1, data3);
    }

    @Test
    void testHashCode() {
        // Arrange
        PolicyMataData data1 = new PolicyMataData("ID-1", "CUST-1", "POL-1");
        PolicyMataData data2 = new PolicyMataData("ID-1", "CUST-1", "POL-1");

        // Assert
        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        PolicyMataData policyData = new PolicyMataData("999", "CUST-999", "POL-GOLD-999");

        // Act
        String result = policyData.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("999"));
        assertTrue(result.contains("CUST-999"));
        assertTrue(result.contains("POL-GOLD-999"));
    }

    @Test
    void testWithEmptyStrings() {
        // Act
        PolicyMataData policyData = new PolicyMataData("", "", "");

        // Assert
        assertNotNull(policyData);
        assertEquals("", policyData.policyId());
        assertEquals("", policyData.customerId());
        assertEquals("", policyData.policyNumber());
    }

    @Test
    void testDifferentPolicyTypes() {
        // Arrange & Act
        PolicyMataData healthPolicy = new PolicyMataData(
                "HP-001", "CUST-H-123", "POL-HEALTH-456"
        );
        PolicyMataData lifePolicy = new PolicyMataData(
                "LP-002", "CUST-L-789", "POL-LIFE-012"
        );
        PolicyMataData autoPolicy = new PolicyMataData(
                "AP-003", "CUST-A-345", "POL-AUTO-678"
        );

        // Assert
        assertTrue(healthPolicy.policyNumber().contains("HEALTH"));
        assertTrue(lifePolicy.policyNumber().contains("LIFE"));
        assertTrue(autoPolicy.policyNumber().contains("AUTO"));
    }

    @Test
    void testCompleteData() {
        // Arrange & Act
        PolicyMataData policyData = new PolicyMataData(
                "POL-ID-987654321",
                "CUST-PREMIUM-123456",
                "POL-PLATINUM-GOLD-789"
        );

        // Assert
        assertEquals("POL-ID-987654321", policyData.policyId());
        assertEquals("CUST-PREMIUM-123456", policyData.customerId());
        assertEquals("POL-PLATINUM-GOLD-789", policyData.policyNumber());
    }

    @Test
    void testImmutability() {
        // Arrange
        String originalPolicyId = "ORIGINAL-ID";
        String originalCustomerId = "ORIGINAL-CUST";
        String originalPolicyNumber = "ORIGINAL-POL";

        PolicyMataData policyData = new PolicyMataData(
                originalPolicyId, originalCustomerId, originalPolicyNumber
        );

        // Act - Attempt to modify the original strings (won't affect the record)
        originalPolicyId = "MODIFIED";

        // Assert - Record should still have original values
        assertEquals("ORIGINAL-ID", policyData.policyId());
        assertNotEquals("MODIFIED", policyData.policyId());
    }
}
