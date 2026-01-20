package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.exception.PolicyNotFoundException;
import com.ai.claim.underwriter.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ai.claim.underwriter.utils.AbstractConstant.*;


@Service
public class ReActAgentService {
    public static final String TOOL_RESULT = "Tool Result: {}";
    public static final String PARSED_ACTION_WITH_PARAMS = "Parsed Action: {} with params: {}";
    private static final Logger logger = LoggerFactory.getLogger(ReActAgentService.class);
    private static final int MAX_ITERATIONS = 15;
    private final ChatClient chatClient;
    private final InvoiceExtractorService extractorService;
    private final InvoiceContext invoiceContext;
    private final ObjectMapper objectMapper;
    private final ClaimAdjudicationService claimAdjudicationService;
    private final DataBaseOperationService dataBaseOperationService;
    private final ThreadLocal<String> rawInvoiceText = new ThreadLocal<>();

    @Value("classpath:/templates/agentSystemPromptTemplate.st")
    Resource agentSystemPromptTemplate;

    private boolean extractedOnce = false;

    @Autowired
    @Qualifier("blockingTaskExecutor")
    private Executor blockingTaskExecutor;

    private ConcurrentHashMap<String, ClaimEvidence> lastClaimEvidence;


    public ReActAgentService(ChatClient.Builder chatClientBuilder,
                             InvoiceExtractorService extractorService,
                             InvoiceContext invoiceContext,
                             ObjectMapper objectMapper, ClaimAdjudicationService claimAdjudicationService, DataBaseOperationService dataBaseTools) {

        this.chatClient = chatClientBuilder.build();
        this.extractorService = extractorService;
        this.invoiceContext = invoiceContext;
        this.objectMapper = objectMapper;
        this.claimAdjudicationService = claimAdjudicationService;
        this.dataBaseOperationService = dataBaseTools;
        lastClaimEvidence = new ConcurrentHashMap<>();
    }

    /**
     * Process an invoice using the ReAct (Reason + Act) pattern.
     * The agent explicitly reasons about each step before taking action.
     * Returns the final claim processing result with decision, payableAmount, and letter.
     */
    public ClaimProcessingResult processWithReAct(ExtractRequest request, String policyNumber, String patientName) {
        logger.info("=== Claim Processing Started ===");

        List<Message> messages = new ArrayList<>();

        // Reset state for new request
        this.lastClaimEvidence = null;
        rawInvoiceText.set(request.invoiceText());

        // Step 1: Initialize conversation history
        messages.add(new UserMessage(PROCESS_THIS_INVOICE_AND_SAVE_IT_TO_DATABASE + request.invoiceText()));

        try {
            // Step 2: Enter the ReAct loop
            for (int iteration = 1; iteration <= MAX_ITERATIONS; iteration++) {
                logger.info("\n--- Step {} ---", iteration);

                // Step 3: Get LLM response (Thought + Action OR Final Answer)
                String response = chatClient.prompt(new Prompt(messages))
                        .system(agentSystemPromptTemplate)
                        .options(ChatOptions.builder().temperature(0.0).build())
                        .call()
                        .content();

                logger.info("Step {} Response:\n{}", iteration, truncateForLogging(response, 500));

                // Step 4: Check if agent is done
                if (containsFinalAnswer(response)) {
                    logger.info("=== Claim Adjudicated successfully ===");

                    // Return the claim decision result (the actual output of the pipeline)
                    if (lastClaimEvidence != null) {
                        ClaimDecision decision = lastClaimEvidence.get(patientName).claimDecision();
                        List<String> reasons = parseReasons(decision.getReasons());
                        JsonNode itemizedDecisions = parseItemizedDecisions(lastClaimEvidence.get(patientName).itemizedDecisions());

                        return ClaimProcessingResult.success(
                                decision.getClaimId(),
                                policyNumber,
                                decision.getDecision(),
                                decision.getPayableAmount() != null ? decision.getPayableAmount().doubleValue() : null,
                                reasons,
                                itemizedDecisions,
                                decision.getLetter()
                        );
                    }

                    // Fallback: return error if no claim evidence available
                    return ClaimProcessingResult.error(PROCESSING_COMPLETED_BUT_NO_CLAIM_DECISION_WAS_GENERATED);
                }

                // Step 5: Parse the action from response
                ParsedAction action = parseAction(response, policyNumber, patientName);

                if (action == null) {
                    logger.warn(COULD_NOT_PARSE_ACTION_FROM_RESPONSE_ASKING_AGENT_TO_CLARIFY);
                    messages.add(new AssistantMessage(response != null ? response : ""));
                    messages.add(new UserMessage("OBSERVATION: I couldn't understand your action. Please use the format: ACTION: tool_name(parameters)"));
                    continue;
                }

                logger.info(PARSED_ACTION_WITH_PARAMS, action.toolName(), truncateForLogging(action.parameters(), 150));

                // Step 6: Execute the tool and get observation
                ToolResult result = executeTool(action);

                if (action.toolName().equalsIgnoreCase("extract") && result != null && result.observation().contains(MISSING_PATIENT_NAME)) {
                    logger.error(MISSING_PATIENT_NAME + "{}", result.observation());
                    return ClaimProcessingResult.error(MISSING_PATIENT_NAME);
                }

                if (action.toolName().equalsIgnoreCase("adjudicate") && result != null && result.observation().contains("Policy not found for Policy Number") || result.observation().contains("Error while fetching the Policy Data")) {
                    logger.error("Policy not found for Policy Number {}", result.observation());
                    throw new PolicyNotFoundException(result.observation());
                }

                assert result != null;
                logger.info(TOOL_RESULT, result.observation());

                // Step 7: Add to conversation history
                messages.add(new AssistantMessage(response));
                // String observation = summarizeObservation(result != null ? result.observation() : null);
                messages.add(new UserMessage("OBSERVATION: " + result.observation()));

                handleObservation(result.observation(), action.toolName());

                messages = messageTrimming(messages);
            }
        } finally {
            rawInvoiceText.remove();
        }

        // If we exit the loop without finishing
        logger.error("Agent did not complete within {} iterations", MAX_ITERATIONS);

        // Return whatever claim decision we have
        if (lastClaimEvidence != null) {
            ClaimDecision decision = lastClaimEvidence.get(patientName).claimDecision();
            List<String> reasons = parseReasons(decision.getReasons());
            JsonNode itemizedDecisions = parseItemizedDecisions(lastClaimEvidence.get(patientName).itemizedDecisions());

            return ClaimProcessingResult.success(
                    decision.getClaimId(),
                    policyNumber,
                    decision.getDecision(),
                    decision.getPayableAmount() != null ? decision.getPayableAmount().doubleValue() : null,
                    reasons,
                    itemizedDecisions,
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

    /**
     * Parse itemizedDecisions from JSON string to JsonNode
     */
    private JsonNode parseItemizedDecisions(String itemizedDecisionsJson) {
        if (itemizedDecisionsJson == null || itemizedDecisionsJson.isEmpty() || itemizedDecisionsJson.equals("[]")) {
            return objectMapper.createArrayNode();
        }
        try {
            return objectMapper.readTree(itemizedDecisionsJson);
        } catch (Exception e) {
            // Return empty array if parsing fails
            return objectMapper.createArrayNode();
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Truncate long text for logging to avoid cluttering logs
     */
    private String truncateForLogging(String text, int maxLength) {
        if (text == null) return "null";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "\n... [truncated " + (text.length() - maxLength) + " more characters]";
    }

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
    private ParsedAction parseAction(String response, String policyNumber, String patientName) {
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
            return new ParsedAction(toolName, parameters, policyNumber, patientName);
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
                case "adjudicate" -> {
                    // capture context-bound invoice on the calling thread before moving to executor
                    ExtractedInvoice captured = invoiceContext.getLastExtractedInvoice();
                    if (captured == null) {
                        // keep behavior: return the usual error synchronously
                        yield adjudicateClaim(action.policyNumber());
                    }
                    // run adjudication off the servlet thread but operate on the captured object
                    yield runBlockingWithTimeout(() -> adjudicateClaimWithInvoice(captured, action.policyNumber()), 300, "adjudicate");
                }
                case "saveclaimdecision" -> saveClaimDecisionAndEvidence(action.patientName());
                case "getclaimdecisiondata" -> getClaimDecisionData(action.patientName());
                default -> new ToolResult(
                        false,
                        "Unknown tool: " + action.toolName() + ". Available tools: extract, validate, save, adjudicate, saveclaimdecision, saveclaimevidence, getclaimdecisiondata",
                        null
                );
            };
        } catch (Exception e) {
            logger.error("Tool execution failed: {}", e.getMessage(), e);
            return new ToolResult(false, "Error executing " + action.toolName() + ": " + e.getMessage(), null);
        }
    }

    private ToolResult getClaimDecisionData(String patientName) {

        logger.info("Executing GET CLAIM DATA tool");

        try {
            // Use stored claim evidence from adjudicate
            if (lastClaimEvidence == null) {
                return new ToolResult(false, "{\"success\": false, \"error\": \"No claim evidence available. Call adjudicate first.\"}", null);
            }

            ClaimDecision claimDecision = lastClaimEvidence.get(patientName).claimDecision();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("claimId", claimDecision.getClaimId());
            result.put("decision", claimDecision.getDecision());
            result.put("payableAmount", claimDecision.getPayableAmount());
            result.put("letter", claimDecision.getLetter());
            result.put("message", "Claim data fetched successfully");

            return new ToolResult(true, objectMapper.writeValueAsString(result), claimDecision);

        } catch (Exception e) {
            logger.error("Saving claim decision Evidence failed: {}", e.getMessage(), e);
            return new ToolResult(false, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}", null);
        }
    }

    private ToolResult saveClaimDecisionAndEvidence(String patientName) {
        logger.info("Executing SAVE CLAIM DECISION tool");

        try {
            ExtractedInvoice captured = invoiceContext.getLastExtractedInvoice();
            if (lastClaimEvidence == null || lastClaimEvidence.get(patientName) == null) {
                return new ToolResult(false, "{\"success\": false, \"error\": \"No claim evidence available. Call adjudicate first.\"}", null);
            }

            // Save the invoice first (if not already saved)
            dataBaseOperationService.saveInvoiceData(captured);
            // Save claim decision first to get the generated ID
            ClaimDecision savedDecision = dataBaseOperationService.saveIntoClaimDecisionDB(lastClaimEvidence.get(patientName));

            // Update the lastClaimEvidence with the saved decision that has the ID
            ClaimEvidence claimEvidence = new ClaimEvidence(
                    lastClaimEvidence.get(patientName).matches(),
                    savedDecision,
                    lastClaimEvidence.get(patientName).evidenceChunks(),
                    lastClaimEvidence.get(patientName).itemizedDecisions()
            );

            ConcurrentHashMap<String, ClaimEvidence> newLastEvidence = new ConcurrentHashMap<>();
            newLastEvidence.put(patientName, claimEvidence);
            lastClaimEvidence = newLastEvidence;

            // Now save the evidences with the correct decision_id
            dataBaseOperationService.saveIntoClaimEvidenceDB(lastClaimEvidence.get(patientName));

            logger.info("Saved Claim Evidence and Claim Decision for claimId: {}", lastClaimEvidence.get(patientName).claimDecision().getClaimId());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("claimId", lastClaimEvidence.get(patientName).claimDecision().getClaimId());

            return new ToolResult(true, objectMapper.writeValueAsString(result), lastClaimEvidence.get(patientName).claimDecision());

        } catch (Exception e) {
            logger.error("Saving claim decision failed: {}", e.getMessage(), e);
            return new ToolResult(false, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}", null);
        }
    }

    private ToolResult adjudicateClaim(String policyNumber) {
        logger.info("Executing ADJUDICATE tool");

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

            String currency = invoice.currency() != null ? invoice.currency() : "INR";
            if (invoice.lineItems() != null) {
                for (var item : invoice.lineItems()) {
                    summaryBuilder.append(String.format("- %s: %s %s\n",
                            !item.desc().isBlank() ? item.desc() : "No Description",
                            currency,
                            item.amount() != null ? item.amount() : "0.0"));
                }
            }

            summaryBuilder.append(String.format("\nTotal Amount: %s %s",
                    currency,
                    invoice.totalAmount() != null ? invoice.totalAmount() : "0.0"));

            String summary = summaryBuilder.toString();
            logger.info("Built invoice summary for adjudication:\n{}", summary);

            // Use invoice number hash as claim ID (consistent and unique)
            long claimId = Math.abs(invoice.invoiceNumber().hashCode());

            ClaimAdjudicationRequest adjudicationRequest = new ClaimAdjudicationRequest(invoice.patientName(), claimId, policyNumber, summary, 5);

            // Call the adjudication service
            var claimEvidence = claimAdjudicationService.adjudicate(adjudicationRequest);

            // Store for use by saveClaimDecision, saveClaimEvidence, getClaimDecisionData
            ConcurrentHashMap<String, ClaimEvidence> newLastEvidence = new ConcurrentHashMap<>();
            newLastEvidence.put(invoice.patientName(), claimEvidence);
            this.lastClaimEvidence = newLastEvidence;

            return new ToolResult(true, objectMapper.writeValueAsString(claimEvidence), claimEvidence);

        } catch (Exception e) {
            logger.error("Adjudication failed: {}", e.getMessage(), e);
            return new ToolResult(false, "{\"error\": \"Adjudication failed: " + e.getMessage() + "\"}", null);
        }
    }

    /**
     * Execute the extract tool
     */
    private ToolResult executeExtract(String invoiceText) {
        logger.info("Executing EXTRACT tool");

        try {
            String fullText = rawInvoiceText.get();
            if (fullText != null && !fullText.isBlank()) {
                invoiceText = fullText;
            }
            // Use the existing extractor service
            Map<String, Object> extractedOutput = extractorService.extract(invoiceText);

            // Start with any issues reported by extractor
            List<String> issues = new ArrayList<>();
            Object issuesObj = extractedOutput.get("issues");
            if (issuesObj instanceof List<?>) {
                for (Object o : (List<?>) issuesObj) {
                    if (o instanceof String s) {
                        issues.add(s);
                    }
                }
            }

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
                    if (v == null || v < threshold) {
                        issues.add(String.format("Low confidence for field '%s': %s", e.getKey(), v));
                    }
                }
            }

            // Check each line item's confidence
            if (invoice.lineItems() != null) {
                for (var item : invoice.lineItems()) {
                    Double c = item.confidence();
                    if (c == null || c < threshold) {
                        issues.add(String.format("Low confidence for line item '%s': %s", item.desc(), c));
                    }
                }
            }

            // Build deterministic result that includes validation info so LLM doesn't re-decide
            Map<String, Object> result = new HashMap<>();
            result.put("invoiceNumber", invoice.invoiceNumber());
            result.put("totalAmount", invoice.totalAmount());
            result.put("dateOfService", invoice.dateOfService());
            result.put("patientName", invoice.patientName());
            result.put("valid", issues.isEmpty());

            String json = objectMapper.writeValueAsString(result);

            // store in invoiceContext so subsequent tools can access without relying on LLM params
            try {
                invoiceContext.setLastExtractedInvoice(invoice);
            } catch (Exception ignore) {
                // ignore if context not writable in test contexts
            }

            return new ToolResult(issues.isEmpty(), json, invoice);
        } catch (Exception e) {
            return new ToolResult(false, "Extraction failed: " + e.getMessage(), null);
        }
    }

    /**
     * Execute the validate tool
     */
    private ToolResult executeValidate(String invoiceDataJson) {
        logger.info("Executing VALIDATE tool");

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

    private void handleObservation(String observation, String lastToolName) {
        if ("extract".equals(lastToolName) && observation != null && observation.contains("\"valid\":true")) {
            extractedOnce = true;
        }
    }


    public List<Message> messageTrimming(List<Message> messages) {

        boolean replaced = false;
        // keep last up to 5 messages to limit LLM context size
        if (messages == null) return new ArrayList<>();

        List<Message> trimmed = new ArrayList<>(messages.size());

        for (Message message : messages) {
            if (!replaced && extractedOnce && message instanceof UserMessage userMessage) {
                String content = userMessage.getText();
                if (content != null && content.startsWith("Process this invoice and save it to database:")) {
                    trimmed.add(new UserMessage(
                            "Invoice text already provided earlier. Do not ask for it again."
                    ));
                    replaced = true;
                    continue;
                }
            }
            trimmed.add(message);
        }

        if (trimmed.size() > 5) {
            return new ArrayList<>(trimmed.subList(trimmed.size() - 5, trimmed.size()));
        }
        return trimmed;
    }

    private <T> T runBlockingWithTimeout(Supplier<T> supplier, long timeoutSeconds, String taskName) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, blockingTaskExecutor);
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            future.cancel(true);
            logger.warn("{} timed out after {}s", taskName, timeoutSeconds);
            throw new RuntimeException(taskName + " timeout", te);
        } catch (Exception e) {
            logger.error("{} failed", taskName, e);
            throw new RuntimeException(taskName + " failed", e);
        }
    }

    // add this method in the same class
    private ToolResult adjudicateClaimWithInvoice(ExtractedInvoice invoice, String policyNumber) {
        try {
            if (invoice == null) {
                return new ToolResult(false, "{\"error\": \"No invoice data available for adjudication. Call save first!\"}", null);
            }

            StringBuilder summaryBuilder = new StringBuilder();
            summaryBuilder.append(String.format("Patient: %s\n", invoice.patientName()));
            summaryBuilder.append(String.format("Invoice Number: %s\n", invoice.invoiceNumber()));
            summaryBuilder.append(String.format("Date of Service: %s\n", invoice.dateOfService()));
            summaryBuilder.append(String.format("Hospital: %s\n", invoice.hospitalName()));

            String currency = invoice.currency() != null ? invoice.currency() : "INR";

            if (invoice.lineItems() != null && !invoice.lineItems().isEmpty()) {
                summaryBuilder.append(String.format("\nTotal Line Items: %d\n", invoice.lineItems().size()));
                summaryBuilder.append("\nServices Breakdown:\n");

                // Group items by category for concise summary
                for (var item : invoice.lineItems()) {
                    summaryBuilder.append(String.format("- %s: %s %.2f\n",
                            !item.desc().isBlank() ? item.desc() : "No Description",
                            currency,
                            item.amount() != null ? item.amount() : 0.0));
                }
            }

            summaryBuilder.append(String.format("\nTotal Amount: %s %.2f", currency, invoice.totalAmount()));

            String summary = summaryBuilder.toString();

            long claimId = Math.abs(invoice.invoiceNumber().hashCode());
            ClaimAdjudicationRequest adjudicationRequest = new ClaimAdjudicationRequest(invoice.patientName(), claimId, policyNumber, summary, 5);

            var claimEvidence = claimAdjudicationService.adjudicate(adjudicationRequest);
            ConcurrentHashMap<String, ClaimEvidence> newLastEvidence = new ConcurrentHashMap<>();
            newLastEvidence.put(invoice.patientName(), claimEvidence);
            this.lastClaimEvidence = newLastEvidence;

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("claimId", claimEvidence.claimDecision().getClaimId());

            return new ToolResult(true, objectMapper.writeValueAsString(result), claimEvidence);
        } catch (Exception e) {
            logger.error("Adjudication failed: {}", e.getMessage(), e);
            return new ToolResult(false, "{\"error\": \"Adjudication failed: " + e.getMessage() + "\"}", null);
        }
    }
}
