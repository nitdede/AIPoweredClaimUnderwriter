package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.entity.ClaimDecisionEvidence;
import com.ai.claim.underwriter.model.ClaimAdjudicationRequest;
import com.ai.claim.underwriter.model.ClaimAdjudicationResponse;
import com.ai.claim.underwriter.model.ClaimEvidence;
import com.ai.claim.underwriter.repository.ClaimDecisionDB;
import com.ai.claim.underwriter.repository.ClaimDecisionEvidenceDB;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClaimAdjudicationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final VectorStore vectorStore;
    private final ClaimDecisionDB claimDecisionDB;
    private final ClaimDecisionEvidenceDB claimDecisionEvidenceDB;
    private final Map<String, SearchRequest> ragCache;
    private final Map<String, List<String>> evidenceChunkCache = new HashMap<>();
    private final Map<String, List<Document>> documentCache = new HashMap<>();

    public ClaimAdjudicationService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper, VectorStore vectorStore, ClaimDecisionDB claimDecisionDB, ClaimDecisionEvidenceDB claimDecisionEvidenceDB) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.vectorStore = vectorStore;
        this.claimDecisionDB = claimDecisionDB;
        this.claimDecisionEvidenceDB = claimDecisionEvidenceDB;
        this.ragCache = new HashMap<>();
    }

    @Tool(description = "Adjudicates a claim based on invoice summary and policy evidence using AI.")
    public ClaimEvidence adjudicate(ClaimAdjudicationRequest claimAdjudicationRequest) {

        if(ragCache.isEmpty() || !ragCache.containsKey("ASPL-HI-784512"))
        {
        var request = SearchRequest.builder()
                .query(claimAdjudicationRequest.invoiceSummaryText())
                .filterExpression("policyNumber == 'ASPL-HI-784512' && customerId == 'RAJESH KUMAR'")
                .topK(5)
                .build();

        ragCache.put("ASPL-HI-784512", request);
            List<Document> matches = vectorStore.similaritySearch(ragCache.get("ASPL-HI-784512"));
            List<String> evidenceChunks = matches.stream()
                    .map(Document::getText)
                    .toList();

            documentCache.put("ASPL-HI-784512", matches);
            evidenceChunkCache.put("ASPL-HI-784512", evidenceChunks);
        }


        //List<Document> matches = vectorStore.similaritySearch(ragCache.get("ASPL-HI-784512"));
        /*List<String> evidenceChunks = matches.stream()
                .map(Document::getText)
                .toList();*/

        // Ask LLM to produce decision + letter (grounded on evidence)
        String system = """
                    You are an automated insurance claim processing system that evaluates medical claims against policy documents.
                    
                    CRITICAL: Your response must be ONLY valid JSON. No explanations, no markdown, no additional text.
                    
                    MANDATORY POLICY-DRIVEN PROCESS (follow exactly):
                    
                    STEP 1: Analyze POLICY EVIDENCE CHUNKS to extract:
                    - Which services/procedures are covered vs excluded
                    - Deductible amounts specified in the policy
                    - Co-payment percentages for different service types
                    - Any special conditions or limits
                    
                    STEP 2: Match invoice items to policy coverage rules
                    - For each item in the invoice, check if it's covered based on policy terms
                    - Apply any service-specific exclusions mentioned in policy
                    
                    STEP 3: Calculate covered amount
                    - Sum only the amounts for services that are covered per policy
                    - Exclude any services marked as excluded in policy documents
                    
                    STEP 4: Apply policy-specified deductible
                    - Subtract deductible amount as stated in policy evidence
                    - If no deductible mentioned, assume 0
                    
                    STEP 5: Apply policy-specified co-payments
                    - Apply co-payment percentages as defined in policy for each service type
                    - Different service categories may have different co-payment rates
                    
                    IMPORTANT: Base ALL calculations on the actual policy terms in the evidence chunks.
                    Do NOT use hardcoded assumptions about coverage, deductibles, or co-payments.
                    
                    DECISION RULES:
                    - PARTIAL: If some services covered, others excluded per policy
                    - APPROVED: If all services covered per policy
                    - DENIED: If no services covered per policy
                    
                    RESPONSE FORMAT - Return ONLY this JSON with policy-based calculations:
                    {
                      "decision": "PARTIAL",
                      "payableAmount": [calculated amount based on policy terms],
                      "reasons": ["Policy-based reasons for coverage/exclusion"],
                      "letter": "Explanation referencing specific policy terms"
                    }
                    """;

        String user = """
                    INVOICE SUMMARY:
                    %s
    
                    POLICY EVIDENCE CHUNKS (use these as the only source of truth):
                    %s
                    """.formatted(claimAdjudicationRequest.invoiceSummaryText(),
                evidenceChunkCache.get("ASPL-HI-784512").stream().map(c -> "- " + c).collect(Collectors.joining("\n")));
        // Implementation for verifying claim and generating decision letter

        String response = chatClient.prompt()
                .system(system)
                .user(user)
                .options(ChatOptions.builder()
                        .temperature(0.0)
                        .build())
                .call()
                .content();


        JsonNode node;
        try {
            node = objectMapper.readTree(response);
        } catch (Exception e) {
            // fallback if model returned non-JSON (rare but happens)
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("decision", "NEEDS_INFO");
            objectNode.putNull("payableAmount");
            objectNode.putArray("reasons").add("Model output was not valid JSON");
            objectNode.put("letter", response);
            node = objectNode;
        }

        String decision = node.path("decision").asText("NEEDS_INFO");
        Double payable = node.path("payableAmount").isNumber() ? node.path("payableAmount").asDouble() : null;
        String reasonsJson = node.path("reasons").isArray() ? node.path("reasons").toString() : "[]";
        String letter = node.path("letter").asText("");

        // 4) Save decision + evidence in DB
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
       

       return new ClaimEvidence(documentCache.get("ASPL-HI-784512"), claimDecision, evidenceChunkCache.get("ASPL-HI-784512"));

    }

    @Tool(description = "Save the Claim Evidence into Claim Decision Evidence Database.")
    @Transactional
    public void saveIntoClaimEvidenceDB(ClaimEvidence claimEvidence) {
   
        List<ClaimDecisionEvidence> existingEvidences = claimEvidence.matches().stream().map(
                d -> {
                    ClaimDecisionEvidence claimDecisionEvidence = new ClaimDecisionEvidence();
                    claimDecisionEvidence.setChunkText(d.getText());
                    claimDecisionEvidence.setDecisionId(claimEvidence.claimDecision().getId());
                    if (d.getMetadata().get("score") instanceof Number n) {
                        claimDecisionEvidence
                                .setScore(BigDecimal.valueOf(n.doubleValue()).setScale(4, RoundingMode.HALF_UP));
                    } else {
                        claimDecisionEvidence.setScore(null);
                    }
                    return claimDecisionEvidence;
                }

        ).collect(Collectors.toList());

        claimDecisionEvidenceDB.saveAll(existingEvidences);

    }

    @Tool(description = "Save the Adjudicated result into Claim Decision Database.")
    public void saveIntoClaimDecisionDB(ClaimEvidence claimEvidence) {
        claimDecisionDB.save(claimEvidence.claimDecision());
    }

    @Tool(description = "Get the Claim Decision Data from Claim Evidence.")
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

}