package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.model.ClaimAdjudicationRequest;
import com.ai.claim.underwriter.model.ClaimEvidence;
import com.ai.claim.underwriter.model.ClaimProcessingResult;
import com.ai.claim.underwriter.model.ExtractRequest;
import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.model.ParsedAction;
import com.ai.claim.underwriter.model.ToolResult;
import com.ai.claim.underwriter.tools.DataBaseTools;
import com.ai.claim.underwriter.tools.InvoiceContext;
import com.ai.claim.underwriter.utils.utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReActAgentService {
    private static final Logger log = LoggerFactory.getLogger(ReActAgentService.class);
    
    private final ChatClient chatClient;
    private final InvoiceExtractorService extractorService;
    private final InvoiceContext invoiceContext;
    private final ObjectMapper objectMapper;
    private final ClaimAdjudicationService claimAdjudicationService;
    private final DataBaseTools dataBaseTools;
    private ClaimEvidence lastClaimEvidence;
    private static final int MAX_ITERATIONS = 20;
    
    public ReActAgentService(ChatClient.Builder chatClientBuilder,
                             InvoiceExtractorService extractorService,
                             InvoiceContext invoiceContext,
                             ObjectMapper objectMapper, ClaimAdjudicationService claimAdjudicationService, DataBaseTools dataBaseTools) {
        this.chatClient = chatClientBuilder.defaultSystem(utils.REACT_SYSTEM_PROMPT).defaultAdvisors(new SafeGuardAdvisor(utils.sensitiveWords)).build();
        this.extractorService = extractorService;
        this.invoiceContext = invoiceContext;
        this.objectMapper = objectMapper;
        this.claimAdjudicationService = claimAdjudicationService;
        this.dataBaseTools = dataBaseTools;
    }
    
    /**
     * Process an invoice using the ReAct (Reason + Act) pattern.
     * The agent explicitly reasons about each step before taking action.
     * Returns the final claim processing result with decision, payableAmount, and letter.
     */
    public ClaimProcessingResult processWithReAct(ExtractRequest request, String policyNumber) {
        log.info("=== Starting ReAct Agent ===");
        
        // Reset state for new request
        this.lastClaimEvidence = null;
        
        // Step 1: Initialize conversation history
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("Process this invoice and save it to database:\n\n" + request.invoiceText()));
        
        // Step 2: Enter the ReAct loop
        for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {
            log.info("\n--- Iteration {} ---", iteration);
            
            // Step 3: Get LLM response (Thought + Action OR Final Answer)
            String response = chatClient.prompt(new Prompt(messages))
                    .options(ChatOptions.builder().temperature(0.0).build())
                    .call()
                    .content();
            
            log.info("Agent Response:\n{}", response);
            
            // Step 4: Check if agent is done
            if (containsFinalAnswer(response)) {
                log.info("=== Agent completed with FINAL ANSWER ===");
                
                // Return the claim decision result (the actual output of the pipeline)
                if (lastClaimEvidence != null) {
                    ClaimDecision decision = lastClaimEvidence.claimDecision();
                    List<String> reasons = parseReasons(decision.getReasons());
                    return ClaimProcessingResult.success(
                            decision.getClaimId(),
                            policyNumber,
                            decision.getDecision(),
                            decision.getPayableAmount() != null ? decision.getPayableAmount().doubleValue() : null,
                            reasons,
                            decision.getLetter()
                    );
                }
                
                // Fallback: return error if no claim evidence available
                return ClaimProcessingResult.error("Processing completed but no claim decision was generated");
            }
            
            // Step 5: Parse the action from response
            ParsedAction action = parseAction(response);
            
            if (action == null) {
                log.warn("Could not parse action from response, asking agent to clarify");
                messages.add(new AssistantMessage(response));
                messages.add(new UserMessage("OBSERVATION: I couldn't understand your action. Please use the format: ACTION: tool_name(parameters)"));
                continue;
            }
            
            log.info("Parsed Action: {} with params: {}", action.toolName(), action.parameters());

            // Step 6: Execute the tool and get observation
            ToolResult result = executeTool(action);
            
            log.info("Tool Result: {}", result.observation());

            // Step 7: Add to conversation history
            messages.add(new AssistantMessage(response));
            messages.add(new UserMessage("OBSERVATION: " + result.observation()));

            messages = messageTrimming(messages);
        }
        
        // If we exit the loop without finishing
        log.error("Agent did not complete within {} iterations", MAX_ITERATIONS);
        
        // Return whatever claim decision we have
        if (lastClaimEvidence != null) {
            ClaimDecision decision = lastClaimEvidence.claimDecision();
            List<String> reasons = parseReasons(decision.getReasons());
            return ClaimProcessingResult.success(
                    decision.getClaimId(),
                    policyNumber,
                    decision.getDecision(),
                    decision.getPayableAmount() != null ? decision.getPayableAmount().doubleValue() : null,
                    reasons,
                    decision.getLetter()
            );
        }
        
        return ClaimProcessingResult.error("ReAct agent did not complete within " + MAX_ITERATIONS + " iterations");
    }
    
    /**
     * Parse reasons from JSON string to List<String>
     */
    private List<String> parseReasons(String reasonsJson) {
        if (reasonsJson == null || reasonsJson.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(reasonsJson, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            // If it's not valid JSON, return as single reason
            return List.of(reasonsJson);
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Check if the response contains a final answer
     */
    private boolean containsFinalAnswer(String response) {
        return response != null && response.toUpperCase().contains("FINAL ANSWER:");
    }
    
    /**
     * Parse the action from the LLM response.
     * Expected format: ACTION: tool_name(parameters)
     */
    private ParsedAction parseAction(String response) {
        if (response == null) return null;
        
        // Pattern to match: ACTION: tool_name(anything)
        Pattern pattern = Pattern.compile(
            "ACTION:\\s*(\\w+)\\s*\\((.*)\\)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(response);
        
        if (matcher.find()) {
            String toolName = matcher.group(1).toLowerCase().trim();
            String parameters = matcher.group(2).trim();
            return new ParsedAction(toolName, parameters);
        }
        
        return null;
    }
    
    /**
     * Execute a tool and return the observation
     */
    private ToolResult executeTool(ParsedAction action) {
        try {
            return switch (action.toolName()) {
                case "extract" -> executeExtract(action.parameters());
                case "validate" -> executeValidate(action.parameters());
                case "save" -> executeSave(action.parameters());
                case "adjudicate" -> adjudicateClaim(action.parameters());
                case "saveclaimdecision" -> saveClaimDecisionAndEvidence(action.parameters());
                case "getclaimdecisiondata" -> getClaimDecisionData(action.parameters());
                default -> new ToolResult(
                        false,
                        "Unknown tool: " + action.toolName() + ". Available tools: extract, validate, save, adjudicate, saveclaimdecision, saveclaimevidence, getclaimdecisiondata",
                        null
                );
            };
        } catch (Exception e) {
            log.error("Tool execution failed: {}", e.getMessage());
            return new ToolResult(false, "Error executing " + action.toolName() + ": " + e.getMessage(), null);
        }
    }

    private ToolResult getClaimDecisionData(String parameters) {

        log.info("Executing GET CLAIM DATA tool");

        try {
            // Use stored claim evidence from adjudicate
            if (lastClaimEvidence == null) {
                return new ToolResult(false, "{\"success\": false, \"error\": \"No claim evidence available. Call adjudicate first.\"}", null);
            }

            ClaimDecision claimDecision = lastClaimEvidence.claimDecision();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("claimId", claimDecision.getClaimId());
            result.put("decision", claimDecision.getDecision());
            result.put("payableAmount", claimDecision.getPayableAmount());
            result.put("letter", claimDecision.getLetter());
            result.put("message", "Claim data fetched successfully");

            return new ToolResult(true, objectMapper.writeValueAsString(result), claimDecision);

        } catch (Exception e) {
            log.error("Saving claim decision Evidence failed: {}", e.getMessage());
            return new ToolResult(false, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}", null);
        }
    }

    private ToolResult saveClaimDecisionAndEvidence(String parameters) {
    log.info("Executing SAVE CLAIM DECISION tool");
    
    try {
        if (lastClaimEvidence == null) {
            return new ToolResult(false, "{\"success\": false, \"error\": \"No claim evidence available. Call adjudicate first.\"}", null);
        }
            // Save claim decision and evidence in parallel
            claimAdjudicationService.saveIntoClaimDecisionDB(lastClaimEvidence);

            claimAdjudicationService.saveIntoClaimEvidenceDB(lastClaimEvidence);

        log.info("Saved Claim Evidence and Claim Decision for claimId: {}", lastClaimEvidence.claimDecision().getClaimId());

        // Single result object creation
        Map<String, Object> result = Map.of(
            "success", true,
            "claimId", lastClaimEvidence.claimDecision().getClaimId(),
            "decision", lastClaimEvidence.claimDecision().getDecision(),
            "evidenceCount", lastClaimEvidence.evidenceChunks().size(),
            "message", "Claim decision and evidence saved successfully"
        );

        return new ToolResult(true, objectMapper.writeValueAsString(result), lastClaimEvidence.claimDecision());

    } catch (Exception e) {
        log.error("Saving claim decision failed: {}", e.getMessage());
        return new ToolResult(false, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}", null);
    }
}

    private ToolResult adjudicateClaim(String parameters) {
        log.info("Executing ADJUDICATE tool");
        
        try {
            // ALWAYS use the stored invoice from context (more reliable than LLM parameters)
            ExtractedInvoice invoice = invoiceContext.getLastExtractedInvoice();
            if (invoice == null) {
                return new ToolResult(false, "{\"error\": \"No invoice data available for adjudication. Call save first!\"}", null);
            }
            
            // Build a complete invoice summary with all line items for accurate calculation
            StringBuilder summaryBuilder = new StringBuilder();
            summaryBuilder.append(String.format("Patient: %s\n", invoice.patientName()));
            summaryBuilder.append(String.format("Invoice Number: %s\n", invoice.invoiceNumber()));
            summaryBuilder.append(String.format("Date of Service: %s\n", invoice.dateOfService()));
            summaryBuilder.append(String.format("Hospital: %s\n", invoice.hospitalName()));
            summaryBuilder.append("\nLine Items:\n");
            
            if (invoice.lineItems() != null) {
                for (var item : invoice.lineItems()) {
                    summaryBuilder.append(String.format("- %s: %s %s\n", 
                            item.desc(), 
                            invoice.currency() != null ? invoice.currency() : "INR",
                            item.amount()));
                }
            }
            
            summaryBuilder.append(String.format("\nTotal Amount: %s %s", 
                    invoice.currency() != null ? invoice.currency() : "INR",
                    invoice.totalAmount()));
            
            String summary = summaryBuilder.toString();
            log.info("Built invoice summary for adjudication:\n{}", summary);
            
            // Use invoice number hash as claim ID (consistent and unique)
            long claimId = Math.abs(invoice.invoiceNumber().hashCode());
            
            ClaimAdjudicationRequest adjudicationRequest = new ClaimAdjudicationRequest(claimId, summary, 5);
            
            // Call the adjudication service
            var claimEvidence = claimAdjudicationService.adjudicate(adjudicationRequest);
            
            // Store for use by saveClaimDecision, saveClaimEvidence, getClaimDecisionData
            this.lastClaimEvidence = claimEvidence;
            
            return new ToolResult(true, objectMapper.writeValueAsString(claimEvidence), claimEvidence);
            
        } catch (Exception e) {
            log.error("Adjudication failed: {}", e.getMessage());
            return new ToolResult(false, "{\"error\": \"Adjudication failed: " + e.getMessage() + "\"}", null);
        }
    }

    /**
     * Execute the extract tool
     */
    private ToolResult executeExtract(String invoiceText) {
        log.info("Executing EXTRACT tool");
        
        try {
            // Use the existing extractor service
            Map<String, Object> extractedOutput = extractorService.extract(invoiceText);

            // Start with any issues reported by extractor
            List<String> issues = extractedOutput.get("issues") != null ?
                    (List<String>) extractedOutput.get("issues") : new ArrayList<>();

            ExtractedInvoice invoice = extractedOutput.get("invoice") != null ?
                    (ExtractedInvoice) extractedOutput.get("invoice") : null;

            if (invoice == null) {
                return new ToolResult(false, "Extraction failed: no invoice object returned", null);
            }

            // Programmatic confidence enforcement (deterministic)
            double threshold = 0.7d;

            // Check top-level confidence map
            if (invoice.confidence() != null) {
                for (Map.Entry<String, Double> e : invoice.confidence().entrySet()) {
                    Double v = e.getValue();
                    if (v == null || v <= threshold) {
                        issues.add(String.format("Low confidence for field '%s': %s", e.getKey(), v));
                    }
                }
            }

            // Check each line item's confidence
            if (invoice.lineItems() != null) {
                for (var item : invoice.lineItems()) {
                    Double c = item.confidence();
                    if (c == null || c <= threshold) {
                        issues.add(String.format("Low confidence for line item '%s': %s", item.desc(), c));
                    }
                }
            }

            // Build deterministic result that includes validation info so LLM doesn't re-decide
            Map<String, Object> result = new HashMap<>();
            result.put("invoice", invoice);
            result.put("issues", issues);
            result.put("valid", issues.isEmpty());

            String json = objectMapper.writeValueAsString(result);
            return new ToolResult(issues.isEmpty(), json, invoice);
        } catch (Exception e) {
            return new ToolResult(false, "Extraction failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Execute the validate tool
     */
    private ToolResult executeValidate(String invoiceDataJson) {
        log.info("Executing VALIDATE tool");
        
        try {
            // Parse the invoice data
            ExtractedInvoice invoice;
            
            // Try to get from context first (more reliable)
            invoice = invoiceContext.getLastExtractedInvoice();
            
            if (invoice == null) {
                // Try to parse from the parameter
                invoice = objectMapper.readValue(invoiceDataJson, ExtractedInvoice.class);
            }
            
            // Perform validation
            List<String> issues = new ArrayList<>();
            
            if (invoice.patientName() == null || invoice.patientName().isBlank()) {
                issues.add("Missing patient name");
            }
            if (invoice.invoiceNumber() == null || invoice.invoiceNumber().isBlank()) {
                issues.add("Missing invoice number");
            }
            if (invoice.totalAmount() == null || invoice.totalAmount() <= 0) {
                issues.add("Invalid or missing total amount");
            }
            if (invoice.dateOfService() == null || invoice.dateOfService().isBlank()) {
                issues.add("Missing date of service");
            }
            
            // Build validation result
            Map<String, Object> result = new HashMap<>();
            result.put("valid", issues.isEmpty());
            result.put("issues", issues);
            
            if (!issues.isEmpty()) {
                result.put("message", "Validation failed: " + String.join(", ", issues));
            } else {
                result.put("message", "All required fields are present and valid");
            }
            
            return new ToolResult(issues.isEmpty(), objectMapper.writeValueAsString(result), invoice);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("valid", false);
            errorResult.put("error", e.getMessage());
            
            try {
                return new ToolResult(false, objectMapper.writeValueAsString(errorResult), null);
            } catch (Exception ex) {
                return new ToolResult(false, "{\"valid\": false, \"error\": \"" + e.getMessage() + "\"}", null);
            }
        }
    }
    
    /**
     * Execute the save tool
     */
    private ToolResult executeSave(String invoiceDataJson) {
        log.info("Executing SAVE tool");
        
        try {
            // Get the invoice from context (set by extract tool)
            ExtractedInvoice invoice = invoiceContext.getLastExtractedInvoice();
            
            if (invoice == null) {
                // Try to parse from parameter
                invoice = objectMapper.readValue(invoiceDataJson, ExtractedInvoice.class);
            }
            
            if (invoice == null) {
                return new ToolResult(false, "{\"success\": false, \"error\": \"No invoice data to save\"}", null);
            }

            dataBaseTools.saveInvoiceData(invoice);
            
            log.info("Saving invoice: {}", invoice.invoiceNumber());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("invoiceNumber", invoice.invoiceNumber());
            result.put("message", "Invoice saved successfully");
            
            return new ToolResult(true, objectMapper.writeValueAsString(result), invoice);
            
        } catch (Exception e) {
            return new ToolResult(false, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}", null);
        }
    }
    
    /**
     * Parse the final answer from the response
     */
    private ExtractedInvoice parseFinalAnswer(String response) {
        try {
            // Find JSON in the final answer
            int startIdx = response.indexOf("{");
            int endIdx = response.lastIndexOf("}");
            
            if (startIdx >= 0 && endIdx > startIdx) {
                String json = response.substring(startIdx, endIdx + 1);
                return objectMapper.readValue(json, ExtractedInvoice.class);
            }
        } catch (Exception e) {
            log.warn("Could not parse final answer: {}", e.getMessage());
        }
        
        // Fallback to context
        ExtractedInvoice fromContext = invoiceContext.getLastExtractedInvoice();
        if (fromContext != null) {
            return fromContext;
        }
        
        throw new RuntimeException("Could not parse final answer from: " + response);
    }

    public List<Message> messageTrimming(List<Message> messages) {
        List<Message> result = new ArrayList<>();
        if (messages.size() > 5) {
            result.addAll(messages.subList(messages.size() - 4, messages.size()));
            return result;
        }

        return messages;
    }
}