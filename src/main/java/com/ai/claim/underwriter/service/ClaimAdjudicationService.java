package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.exception.ClaimProcessingException;
import com.ai.claim.underwriter.exception.PolicyNotFoundException;
import com.ai.claim.underwriter.model.ClaimAdjudicationRequest;
import com.ai.claim.underwriter.model.ClaimAdjudicationResponse;
import com.ai.claim.underwriter.model.ClaimEvidence;
import com.ai.claim.underwriter.repository.ClaimDecisionDB;
import com.ai.claim.underwriter.repository.ClaimDecisionEvidenceDB;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.ai.claim.underwriter.utils.AbstractConstant.*;

@Service
public class ClaimAdjudicationService {

    private static final Logger logger = LoggerFactory.getLogger(ClaimAdjudicationService.class);
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final VectorStore vectorStore;
    private final ClaimDecisionDB claimDecisionDB;
    private final ClaimDecisionEvidenceDB claimDecisionEvidenceDB;
    private final Map<String, SearchRequest> ragCache;
    private final Map<String, List<String>> evidenceChunkCache = new HashMap<>();
    private final Map<String, List<Document>> documentCache = new HashMap<>();
    private final Executor vectorTaskExecutor;

    @Value("classpath:/templates/claimAdjudicationSystemPromptTemplate.st")
    Resource claimAdjudicationSystemPromptTemplate;

    public ClaimAdjudicationService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper, VectorStore vectorStore, ClaimDecisionDB claimDecisionDB, ClaimDecisionEvidenceDB claimDecisionEvidenceDB, @Qualifier("vectorTaskExecutor") Executor vectorTaskExecutor) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.vectorStore = vectorStore;
        this.claimDecisionDB = claimDecisionDB;
        this.claimDecisionEvidenceDB = claimDecisionEvidenceDB;
        this.ragCache = new HashMap<>();
        this.vectorTaskExecutor = vectorTaskExecutor;
    }

    public ClaimEvidence adjudicate(ClaimAdjudicationRequest claimAdjudicationRequest) {
        long startTime = System.currentTimeMillis();

        String policyNumber = claimAdjudicationRequest.policyNumber();

        try {
            if (!claimAdjudicationRequest.patientName().isEmpty()) {
                String filerStr = "policyNumber == '" + policyNumber + "' && customerId == '" + claimAdjudicationRequest.patientName().toUpperCase() + "'";

                if (ragCache.isEmpty() || !ragCache.containsKey(policyNumber)) {
                    long similaritySearchStart = System.currentTimeMillis();

                    var request = SearchRequest.builder()
                            .query(claimAdjudicationRequest.invoiceSummaryText())
                            .filterExpression(filerStr)
                            .topK(5) // Increased to retrieve sufficient policy context for accurate adjudication
                            .build();

                    ragCache.put(policyNumber, request);
                    List<Document> matches = runBlockingSimilaritySearch(ragCache.get(policyNumber), 15);

                    if (matches.isEmpty()) {
                        throw new PolicyNotFoundException("Policy not found for Policy Number: " + policyNumber + " and Patient Name: " + claimAdjudicationRequest.patientName());
                    }

                    long similaritySearchEnd = System.currentTimeMillis();
                    logger.info("Time taken for similarity search: {} ms", (similaritySearchEnd - similaritySearchStart));

                    List<String> evidenceChunks = matches.stream()
                            .map(Document::getText)
                            .toList();

                    if (matches.isEmpty()) {
                        throw new ClaimProcessingException("Error while fetching the Policy Data: " + policyNumber + " and Patient Name: " + claimAdjudicationRequest.patientName());
                    }

                    documentCache.put(policyNumber, matches);
                    evidenceChunkCache.put(policyNumber, evidenceChunks);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }

        String user = """
                INVOICE SUMMARY:
                %s
                
                POLICY EVIDENCE CHUNKS (use these as the only source of truth):
                %s
                """.formatted(claimAdjudicationRequest.invoiceSummaryText(), evidenceChunkCache.get(policyNumber).stream().map(c -> "- " + c).collect(Collectors.joining("\n")));

        long chatClientStart = System.currentTimeMillis();
        String response = chatClient.prompt()
                .system(claimAdjudicationSystemPromptTemplate)
                .user(user)
                .options(ChatOptions.builder()
                        .temperature(0.0)
                        .build())
                .call()
                .content();
                
        long chatClientEnd = System.currentTimeMillis();
        logger.info("Time taken for chat client response: {} ms", (chatClientEnd - chatClientStart));

        JsonNode node;
        try {
            // Strip markdown code fences if present
            String cleanedResponse = stripMarkdownCodeFences(response);
            node = objectMapper.readTree(cleanedResponse);
        } catch (Exception e) {
            logger.error("Failed to parse AI response as JSON: {}", e.getMessage());
            logger.debug("Raw response: {}", response);
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put(DECISION, NEEDS_INFO);
            objectNode.putNull(PAYABLE_AMOUNT);
            objectNode.putArray(REASONS).add(MODEL_OUTPUT_WAS_NOT_VALID_JSON);
            objectNode.put(LETTER, response);
            node = objectNode;
        }

        String decision = node.path(DECISION).asText(NEEDS_INFO);
        Double payable = node.path(PAYABLE_AMOUNT).isNumber() ? node.path(PAYABLE_AMOUNT).asDouble() : null;
        String reasonsJson = node.path(REASONS).isArray() ? node.path(REASONS).toString() : "[]";
        String itemizedDecisionsJson = node.path("itemizedDecisions").isArray() ? node.path("itemizedDecisions").toString() : "[]";
        String letter = node.path(LETTER).asText("");

        ClaimDecision claimDecision = new ClaimDecision();
        claimDecision.setClaimId(claimAdjudicationRequest.claimId());
        claimDecision.setDecision(decision);
        if (payable != null) {
            claimDecision.setPayableAmount(BigDecimal.valueOf(payable).setScale(2, RoundingMode.HALF_UP));
        } else {
            claimDecision.setPayableAmount(null);
        }
        claimDecision.setReasons(reasonsJson);
        claimDecision.setLetter(letter);
        claimDecision.setCreatedAt(LocalDateTime.now());

        long endTime = System.currentTimeMillis();
        logger.info("Total time taken for adjudication: {} ms", (endTime - startTime));

        return new ClaimEvidence(documentCache.get(policyNumber), claimDecision, evidenceChunkCache.get(policyNumber), itemizedDecisionsJson);
    }

    /**
     * Run the vector store similarity search on the shared blocking executor with a timeout.
     * If the search times out, returns an empty list (caller may use cached results as fallback).
     */
    private List<Document> runBlockingSimilaritySearch(SearchRequest request, int timeoutSeconds) {
        CompletableFuture<List<Document>> future = CompletableFuture.supplyAsync(() -> vectorStore.similaritySearch(request), vectorTaskExecutor);
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            future.cancel(true);
            logger.info("Vector similaritySearch timed out after {}s", timeoutSeconds);
            return List.of();
        } catch (Exception e) {
            logger.info("Vector similaritySearch failed: {}", e.getMessage());
            return List.of();
        }
    }

    public ClaimAdjudicationResponse getClaimDecisionData(ClaimEvidence claimEvidence) {
        ClaimDecision claimDecision = claimEvidence.claimDecision();
        return new ClaimAdjudicationResponse(
                // ensure primitive long is provided (fallback to 0 if null)
                claimDecision.getClaimId() != null ? claimDecision.getClaimId() : 0L,
                claimDecision.getDecision(),
                // convert BigDecimal to Double (nullable)
                claimDecision.getPayableAmount() != null ? claimDecision.getPayableAmount().doubleValue() : null,
                claimEvidence.evidenceChunks(),
                claimDecision.getLetter()
        );
    }

    /**
     * Strips markdown code fences from AI model responses.
     * Handles formats like:
     * - ```json\n{...}\n```
     * - ```\n{...}\n```
     * - {... }```
     * 
     * @param response The raw response from the AI model
     * @return Cleaned JSON string without markdown code fences
     */
    private String stripMarkdownCodeFences(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        
        String cleaned = response.trim();
        
        // Remove opening code fence with optional language identifier
        // Handles: ```json or ``` at the start
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline != -1) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
        }
        
        // Remove closing code fence
        // Handles: ``` at the end
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        return cleaned.trim();
    }

}
