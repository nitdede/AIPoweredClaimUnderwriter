package com.ai.claim.underwriter.model;

public record ToolResult(boolean success,
         String observation,  // What to tell the agent
         Object data) {
}
