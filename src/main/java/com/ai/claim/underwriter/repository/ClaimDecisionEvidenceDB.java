package com.ai.claim.underwriter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.claim.underwriter.entity.ClaimDecisionEvidence;

@Repository
public interface ClaimDecisionEvidenceDB extends JpaRepository<ClaimDecisionEvidence, Long> {
    
}
