package com.ai.claim.underwriter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParsedActionTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String toolName = "extractInvoice";
        String parameters = "{\"invoiceText\":\"sample\"}";
        String policyNumber = "POL-12345";
        String patientName = "John Doe";

        // Act
        ParsedAction action = new ParsedAction(toolName, parameters, policyNumber, patientName);

        // Assert
        assertNotNull(action);
        assertEquals(toolName, action.toolName());
        assertEquals(parameters, action.parameters());
        assertEquals(policyNumber, action.policyNumber());
        assertEquals(patientName, action.patientName());
    }

    @Test
    void testConstructorWithNullValues() {
        // Act
        ParsedAction action = new ParsedAction(null, null, null, null);

        // Assert
        assertNotNull(action);
        assertNull(action.toolName());
        assertNull(action.parameters());
        assertNull(action.policyNumber());
        assertNull(action.patientName());
    }

    @Test
    void testEquals() {
        // Arrange
        ParsedAction action1 = new ParsedAction("tool1", "params1", "POL-001", "Patient A");
        ParsedAction action2 = new ParsedAction("tool1", "params1", "POL-001", "Patient A");
        ParsedAction action3 = new ParsedAction("tool2", "params2", "POL-002", "Patient B");

        // Assert
        assertEquals(action1, action2);
        assertNotEquals(action1, action3);
    }

    @Test
    void testHashCode() {
        // Arrange
        ParsedAction action1 = new ParsedAction("tool1", "params1", "POL-001", "Patient A");
        ParsedAction action2 = new ParsedAction("tool1", "params1", "POL-001", "Patient A");

        // Assert
        assertEquals(action1.hashCode(), action2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        ParsedAction action = new ParsedAction(
                "validateClaim", "{\"claimId\":123}", "POL-999", "Jane Smith"
        );

        // Act
        String result = action.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("validateClaim"));
        assertTrue(result.contains("POL-999"));
        assertTrue(result.contains("Jane Smith"));
    }

    @Test
    void testWithJsonParameters() {
        // Arrange
        String jsonParams = "{\"field1\":\"value1\",\"field2\":123,\"field3\":true}";

        // Act
        ParsedAction action = new ParsedAction(
                "processClaim", jsonParams, "POL-ABC", "Test Patient"
        );

        // Assert
        assertEquals("processClaim", action.toolName());
        assertTrue(action.parameters().contains("field1"));
        assertTrue(action.parameters().contains("value1"));
    }

    @Test
    void testWithEmptyStrings() {
        // Act
        ParsedAction action = new ParsedAction("", "", "", "");

        // Assert
        assertNotNull(action);
        assertEquals("", action.toolName());
        assertEquals("", action.parameters());
        assertEquals("", action.policyNumber());
        assertEquals("", action.patientName());
    }

    @Test
    void testDifferentToolNames() {
        // Arrange & Act
        ParsedAction extractAction = new ParsedAction(
                "extractInvoice", "{}", "POL-1", "Patient 1"
        );
        ParsedAction adjudicateAction = new ParsedAction(
                "adjudicateClaim", "{}", "POL-2", "Patient 2"
        );
        ParsedAction validateAction = new ParsedAction(
                "validatePolicy", "{}", "POL-3", "Patient 3"
        );

        // Assert
        assertEquals("extractInvoice", extractAction.toolName());
        assertEquals("adjudicateClaim", adjudicateAction.toolName());
        assertEquals("validatePolicy", validateAction.toolName());
    }

    @Test
    void testCompleteAction() {
        // Arrange
        String toolName = "processClaimWithEvidence";
        String parameters = "{\"claimId\":54321,\"evidenceRequired\":true,\"urgency\":\"high\"}";
        String policyNumber = "POL-PREMIUM-789";
        String patientName = "Alice Johnson";

        // Act
        ParsedAction action = new ParsedAction(toolName, parameters, policyNumber, patientName);

        // Assert
        assertEquals("processClaimWithEvidence", action.toolName());
        assertTrue(action.parameters().contains("claimId"));
        assertTrue(action.parameters().contains("54321"));
        assertEquals("POL-PREMIUM-789", action.policyNumber());
        assertEquals("Alice Johnson", action.patientName());
    }
}
