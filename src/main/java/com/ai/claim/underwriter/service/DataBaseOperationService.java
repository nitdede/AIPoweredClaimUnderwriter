package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.entity.ClaimAIResult;
import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.entity.ClaimDecisionEvidence;
import com.ai.claim.underwriter.model.ClaimAdjudicationRequest;
import com.ai.claim.underwriter.model.ClaimEvidence;
import com.ai.claim.underwriter.model.ClaimExtractionResult;
import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.repository.ClaimAIResultDB;
import com.ai.claim.underwriter.repository.ClaimDecisionDB;
import com.ai.claim.underwriter.repository.ClaimDecisionEvidenceDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataBaseOperationService {
    private final ClaimAIResultDB claimAIResultDB;
    private final ObjectMapper objectMapper;
    private final ClaimDecisionDB claimDecisionDB;
    private final ClaimDecisionEvidenceDB claimDecisionEvidenceDB;

    public DataBaseOperationService(ClaimAIResultDB claimAIResultDB, ObjectMapper objectMapper,
                                    ClaimDecisionDB claimDecisionDB, ClaimDecisionEvidenceDB claimDecisionEvidenceDB) {
        this.claimAIResultDB = claimAIResultDB;
        this.objectMapper = objectMapper;
        this.claimDecisionDB = claimDecisionDB;
        this.claimDecisionEvidenceDB = claimDecisionEvidenceDB;
    }

    public void saveInvoiceData(ExtractedInvoice invoice) {

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
    }

    public void saveResult(ClaimExtractionResult result) {
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

    @Transactional
    public void saveIntoClaimEvidenceDB(ClaimEvidence claimEvidence) {

        List<ClaimDecisionEvidence> existingEvidences = claimEvidence.matches().stream().map(
                d -> {
                    ClaimDecisionEvidence claimDecisionEvidence = new ClaimDecisionEvidence();
                    claimDecisionEvidence.setChunkText(d.getText());
                    // Set the relationship object, not just the ID (decision_id is insertable=false)
                    claimDecisionEvidence.setClaimDecision(claimEvidence.claimDecision());
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

    public ClaimDecision saveIntoClaimDecisionDB(ClaimEvidence claimEvidence) {
        return claimDecisionDB.save(claimEvidence.claimDecision());
    }

    public boolean needMoreInfo(ClaimExtractionResult result){

        double pConf = result.confidence!= null && result.confidence.get("policyNumber") != null ? result.confidence.get("policyNumber") : 0.0;
        double tConf = result.confidence!= null && result.confidence.get("totalAmount") != null ? result.confidence.get("totalAmount") : 0.0;

        boolean missingPolicyNumber = result.policyNumber == null || result.policyNumber.isBlank();
        boolean missingTotalAmount = result.totalAmount == null;

        return missingPolicyNumber || missingTotalAmount || pConf < 0.7 || tConf < 0.7;

    }
}
