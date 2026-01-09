package com.ai.claim.underwriter.model;

/**
 * Represents a parsed action from the LLM response
 */
public record ParsedAction ( String toolName, String parameters) {
}

