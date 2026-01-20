package com.ai.claim.underwriter.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AbstractConstant
 */
class AbstractConstantTest {

    @Test
    void testConstants_haveExpectedValues() {
        // Test Model output constant
        assertEquals("Model output was not valid JSON", AbstractConstant.MODEL_OUTPUT_WAS_NOT_VALID_JSON);
        
        // Test decision-related constants
        assertEquals("NEEDS_INFO", AbstractConstant.NEEDS_INFO);
        assertEquals("decision", AbstractConstant.DECISION);
        assertEquals("payableAmount", AbstractConstant.PAYABLE_AMOUNT);
        assertEquals("reasons", AbstractConstant.REASONS);
        assertEquals("letter", AbstractConstant.LETTER);
        
        // Test validation constants
        assertEquals("Missing patient name", AbstractConstant.MISSING_PATIENT_NAME);
        assertEquals("Missing invoice number", AbstractConstant.MISSING_INVOICE_NUMBER);
        assertEquals("Invalid or missing total amount", AbstractConstant.INVALID_OR_MISSING_TOTAL_AMOUNT);
        assertEquals("Missing date of service", AbstractConstant.MISSING_DATE_OF_SERVICE);
        
        // Test processing constants
        assertEquals("Extracted invoice: ", AbstractConstant.EXTRACTED_INVOICE);
        assertEquals("Process this invoice and save it to database:\n\n", 
                    AbstractConstant.PROCESS_THIS_INVOICE_AND_SAVE_IT_TO_DATABASE);
        assertEquals("Processing completed but no claim decision was generated", 
                    AbstractConstant.PROCESSING_COMPLETED_BUT_NO_CLAIM_DECISION_WAS_GENERATED);
        assertEquals("Could not parse action from response, asking agent to clarify", 
                    AbstractConstant.COULD_NOT_PARSE_ACTION_FROM_RESPONSE_ASKING_AGENT_TO_CLARIFY);
    }

    @Test
    void testConstants_areNotNull() {
        assertNotNull(AbstractConstant.MODEL_OUTPUT_WAS_NOT_VALID_JSON);
        assertNotNull(AbstractConstant.NEEDS_INFO);
        assertNotNull(AbstractConstant.DECISION);
        assertNotNull(AbstractConstant.PAYABLE_AMOUNT);
        assertNotNull(AbstractConstant.REASONS);
        assertNotNull(AbstractConstant.LETTER);
        assertNotNull(AbstractConstant.MISSING_PATIENT_NAME);
        assertNotNull(AbstractConstant.MISSING_INVOICE_NUMBER);
        assertNotNull(AbstractConstant.INVALID_OR_MISSING_TOTAL_AMOUNT);
        assertNotNull(AbstractConstant.MISSING_DATE_OF_SERVICE);
        assertNotNull(AbstractConstant.EXTRACTED_INVOICE);
        assertNotNull(AbstractConstant.PROCESS_THIS_INVOICE_AND_SAVE_IT_TO_DATABASE);
        assertNotNull(AbstractConstant.PROCESSING_COMPLETED_BUT_NO_CLAIM_DECISION_WAS_GENERATED);
        assertNotNull(AbstractConstant.COULD_NOT_PARSE_ACTION_FROM_RESPONSE_ASKING_AGENT_TO_CLARIFY);
    }

    @Test
    void testConstants_areNotEmpty() {
        assertFalse(AbstractConstant.MODEL_OUTPUT_WAS_NOT_VALID_JSON.isEmpty());
        assertFalse(AbstractConstant.NEEDS_INFO.isEmpty());
        assertFalse(AbstractConstant.DECISION.isEmpty());
        assertFalse(AbstractConstant.PAYABLE_AMOUNT.isEmpty());
        assertFalse(AbstractConstant.REASONS.isEmpty());
        assertFalse(AbstractConstant.LETTER.isEmpty());
    }
}
