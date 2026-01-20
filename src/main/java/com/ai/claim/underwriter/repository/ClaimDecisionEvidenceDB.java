package com.ai.claim.underwriter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.claim.underwriter.entity.ClaimDecisionEvidence;

import java.util.List;

@Repository
public interface ClaimDecisionEvidenceDB extends JpaRepository<ClaimDecisionEvidence, Long> {
    
    /**
     * Find all evidences for a specific decision
     * @param decisionId the ClaimDecision ID
     * @return List of ClaimDecisionEvidence records
     */
    List<ClaimDecisionEvidence> findByDecisionId(Long decisionId);
}
