package com.ai.claim.underwriter.model;

import com.ai.claim.underwriter.entity.ClaimDecision;
import org.springframework.ai.document.Document;

import java.util.List;

public record ClaimEvidence(List<Document> matches, ClaimDecision claimDecision, List<String> evidenceChunks ) {
}
