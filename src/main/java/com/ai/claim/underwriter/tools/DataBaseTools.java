package com.ai.claim.underwriter.tools;

import com.ai.claim.underwriter.entity.ClaimAIResult;
import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.entity.ClaimDecisionEvidence;
import com.ai.claim.underwriter.model.ClaimAdjudicationRequest;
import com.ai.claim.underwriter.model.ClaimExtractionResult;
import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.repository.ClaimAIResultDB;
import com.ai.claim.underwriter.repository.ClaimDecisionDB;
import com.ai.claim.underwriter.repository.ClaimDecisionEvidenceDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataBaseTools {
    private final ClaimAIResultDB claimAIResultDB;
    private final ObjectMapper objectMapper;
    private final ClaimDecisionDB claimDecisionDB;
    private final ClaimDecisionEvidenceDB claimDecisionEvidenceDB;
    private final InvoiceContext invoiceContext;

    public DataBaseTools(ClaimAIResultDB claimAIResultDB, ObjectMapper objectMapper, 
                        ClaimDecisionDB claimDecisionDB, ClaimDecisionEvidenceDB claimDecisionEvidenceDB,
                        InvoiceContext invoiceContext) {
        this.claimAIResultDB = claimAIResultDB;
        this.objectMapper = objectMapper;
        this.claimDecisionDB = claimDecisionDB;
        this.claimDecisionEvidenceDB = claimDecisionEvidenceDB;
        this.invoiceContext = invoiceContext;
    }

    @Tool(description = "Saves the previously extracted invoice data to database. Call this after the extract tool has been used.")
    public String saveInvoiceData(ExtractedInvoice invoice) {
        System.out.println("Saving extracted invoice data into DB");
        
        // Get the invoice from context (set by extract tool)
       // ExtractedInvoice invoice = invoiceContext.getLastExtractedInvoice();
        
        if (invoice == null) {
            throw new IllegalArgumentException("No extracted invoice found. Please call the extract tool first.");
        }
        
        // Convert ExtractedInvoice to ClaimExtractionResult
        ClaimExtractionResult result = new ClaimExtractionResult();
        result.patientName = invoice.patientName();
        result.invoiceNumber = invoice.invoiceNumber();
        result.dateOfService = invoice.dateOfService();
        result.totalAmount = invoice.totalAmount();
        result.currency = invoice.currency();
        result.hospitalName = invoice.hospitalName();
        result.confidence = invoice.confidence();
        
        // Convert line items
        if (invoice.lineItems() != null) {
            result.lineItems = invoice.lineItems().stream()
                .map(item -> {
                    ClaimExtractionResult.LineItem lineItem = new ClaimExtractionResult.LineItem();
                    lineItem.description = item.desc();
                    lineItem.amount = item.amount();
                    lineItem.confidence = item.confidence();
                    return lineItem;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Save to DB
        saveResult(result);
        
        System.out.println("Successfully saved invoice: " + invoice.invoiceNumber());
        return "Successfully saved invoice data to database with invoice number: " + invoice.invoiceNumber();
    }

    public void saveResult(ClaimExtractionResult result) {
        System.out.println("Saving ClaimExtractionResult into DB");
        ClaimAIResult claimAIResult = new ClaimAIResult();

        // Map simple fields
        claimAIResult.setPatientName(result.patientName);
        claimAIResult.setPolicyNumber(result.policyNumber);
        claimAIResult.setHospitalName(result.hospitalName);
        claimAIResult.setInvoiceNumber(result.invoiceNumber);

        // totalAmount -> BigDecimal
        if (result.totalAmount != null) {
            claimAIResult.setTotalAmount(BigDecimal.valueOf(result.totalAmount).setScale(2, RoundingMode.HALF_UP));
        }

        claimAIResult.setCurrency(result.currency);

        // Compute a confidence score (average of provided confidence values)
        BigDecimal confidenceScore = BigDecimal.ZERO;
        if (result.confidence != null && !result.confidence.isEmpty()) {
            double sum = 0.0;
            int count = 0;
            for (Double v : result.confidence.values()) {
                if (v != null) {
                    sum += v;
                    count++;
                }
            }
            double avg = count > 0 ? sum / count : 0.0;
            confidenceScore = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
        }
        claimAIResult.setConfidenceScore(confidenceScore);

        // aiStatus depends on whether more info is needed
        claimAIResult.setAiStatus(needMoreInfo(result) ? "NEEDS_INFO" : "COMPLETED");

        // Store the raw AI output as JSON
        try {
            claimAIResult.setAiOutput(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            // fallback minimal JSON
            claimAIResult.setAiOutput("{}");
        }

        claimAIResult.setCreatedAt(LocalDateTime.now());

        // Save
        claimAIResultDB.save(claimAIResult);

    }

    @Tool(description = "Saves the claim decision into the database along with evidence documents."
    )
    private void saveClaimDecision(ClaimAdjudicationRequest claimAdjudicationRequest, String decision, Double payable, String reasonsJson, String letter, List<Document> matches) {
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
        claimDecisionDB.save(claimDecision);

        ClaimDecisionEvidence claimDecisionEvidence = new ClaimDecisionEvidence();
        for (Document d : matches) {
            claimDecisionEvidence.setDecisionId(claimDecision.getId());
            claimDecisionEvidence.setChunkText(d.getText());
            if (d.getMetadata().get("score") instanceof Number n) {
                claimDecisionEvidence.setScore(BigDecimal.valueOf(n.doubleValue()).setScale(4, RoundingMode.HALF_UP));
            } else {
                claimDecisionEvidence.setScore(null);
            }
            claimDecisionEvidenceDB.save(claimDecisionEvidence);
        }
    }

    public boolean needMoreInfo(ClaimExtractionResult result){

        double pConf = result.confidence!= null && result.confidence.get("policyNumber") != null ? result.confidence.get("policyNumber") : 0.0;
        double tConf = result.confidence!= null && result.confidence.get("totalAmount") != null ? result.confidence.get("totalAmount") : 0.0;

        boolean missingPolicyNumber = result.policyNumber == null || result.policyNumber.isBlank();
        boolean missingTotalAmount = result.totalAmount == null;

        return missingPolicyNumber || missingTotalAmount || pConf < 0.7 || tConf < 0.7;

    }
}
