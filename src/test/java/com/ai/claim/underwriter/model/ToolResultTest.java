package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolResultTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        boolean success = true;
        String observation = "Operation completed successfully";
        Object data = "Sample data";

        // Act
        ToolResult result = new ToolResult(success, observation, data);

        // Assert
        assertNotNull(result);
        assertTrue(result.success());
        assertEquals(observation, result.observation());
        assertEquals(data, result.data());
    }

    @Test
    void testConstructorWithNullValues() {
        // Act
        ToolResult result = new ToolResult(false, null, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.success());
        assertNull(result.observation());
        assertNull(result.data());
    }

    @Test
    void testSuccessTrue() {
        // Act
        ToolResult result = new ToolResult(true, "Success message", "data");

        // Assert
        assertTrue(result.success());
    }

    @Test
    void testSuccessFalse() {
        // Act
        ToolResult result = new ToolResult(false, "Error occurred", null);

        // Assert
        assertFalse(result.success());
    }

    @Test
    void testWithStringData() {
        // Arrange
        String stringData = "String data value";

        // Act
        ToolResult result = new ToolResult(true, "String operation", stringData);

        // Assert
        assertEquals(stringData, result.data());
        assertTrue(result.data() instanceof String);
    }

    @Test
    void testWithIntegerData() {
        // Arrange
        Integer intData = 12345;

        // Act
        ToolResult result = new ToolResult(true, "Integer operation", intData);

        // Assert
        assertEquals(intData, result.data());
        assertTrue(result.data() instanceof Integer);
    }

    @Test
    void testWithComplexObjectData() {
        // Arrange
        ClaimExtractionResult complexData = new ClaimExtractionResult();
        complexData.patientName = "John Doe";
        complexData.totalAmount = 1000.0;

        // Act
        ToolResult result = new ToolResult(true, "Extracted invoice data", complexData);

        // Assert
        assertNotNull(result.data());
        assertTrue(result.data() instanceof ClaimExtractionResult);
        ClaimExtractionResult extractedData = (ClaimExtractionResult) result.data();
        assertEquals("John Doe", extractedData.patientName);
    }

    @Test
    void testEquals() {
        // Arrange
        ToolResult result1 = new ToolResult(true, "Observation 1", "Data 1");
        ToolResult result2 = new ToolResult(true, "Observation 1", "Data 1");
        ToolResult result3 = new ToolResult(false, "Observation 2", "Data 2");

        // Assert
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
    }

    @Test
    void testHashCode() {
        // Arrange
        ToolResult result1 = new ToolResult(true, "Observation 1", "Data 1");
        ToolResult result2 = new ToolResult(true, "Observation 1", "Data 1");

        // Assert
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        ToolResult result = new ToolResult(true, "Test observation", "Test data");

        // Act
        String resultString = result.toString();

        // Assert
        assertNotNull(resultString);
        assertTrue(resultString.contains("true"));
        assertTrue(resultString.contains("Test observation"));
    }

    @Test
    void testFailureScenario() {
        // Arrange
        String errorObservation = "Failed to process claim: invalid policy number";

        // Act
        ToolResult result = new ToolResult(false, errorObservation, null);

        // Assert
        assertFalse(result.success());
        assertEquals(errorObservation, result.observation());
        assertNull(result.data());
    }

    @Test
    void testSuccessScenarioWithData() {
        // Arrange
        String successObservation = "Policy validated successfully";
        String policyData = "POL-12345";

        // Act
        ToolResult result = new ToolResult(true, successObservation, policyData);

        // Assert
        assertTrue(result.success());
        assertEquals(successObservation, result.observation());
        assertEquals(policyData, result.data());
    }

    @Test
    void testWithEmptyObservation() {
        // Act
        ToolResult result = new ToolResult(true, "", "data");

        // Assert
        assertTrue(result.success());
        assertEquals("", result.observation());
    }

    @Test
    void testWithComplexDataTypes() {
        // Arrange
        java.util.List<String> listData = java.util.Arrays.asList("Item 1", "Item 2", "Item 3");

        // Act
        ToolResult result = new ToolResult(true, "List processing complete", listData);

        // Assert
        assertTrue(result.success());
        assertNotNull(result.data());
        assertTrue(result.data() instanceof java.util.List);
        assertEquals(3, ((java.util.List<?>) result.data()).size());
    }
}
