package com.ai.claim.underwriter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ai.claim.underwriter.entity.ClaimDecision;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimDecisionDB extends JpaRepository<ClaimDecision, Long> {
    
    /**
     * Find ClaimDecision by claim_id
     * @param claimId the claim ID to search for
     * @return Optional containing the ClaimDecision if found
     */
    Optional<ClaimDecision> findByClaimId(Long claimId);
    
    /**
     * Find the most recent ClaimDecision by claim_id
     * @param claimId the claim ID to search for
     * @return Optional containing the most recent ClaimDecision if found
     */
    @Query("SELECT cd FROM ClaimDecision cd WHERE cd.claimId = :claimId ORDER BY cd.createdAt DESC")
    Optional<List<ClaimDecision>> findLatestByClaimId(@Param("claimId") Long claimId);
    
    /**
     * Find ClaimDecision with all associated evidence by claim_id using JOIN FETCH
     * This executes a single SQL query with LEFT JOIN for optimal performance
     * @param claimId the claim ID to search for
     * @return Optional containing ClaimDecision with eagerly loaded evidences
     */
    @Query("SELECT cd FROM ClaimDecision cd LEFT JOIN FETCH cd.evidences WHERE cd.claimId = :claimId")
    Optional<ClaimDecision> findByClaimIdWithEvidence(@Param("claimId") Long claimId);
    
    /**
     * Find the most recent ClaimDecision with evidence by claim_id using JOIN FETCH
     * @param claimId the claim ID to search for
     * @return Optional containing the latest ClaimDecision with eagerly loaded evidences
     */
    @Query("SELECT cd FROM ClaimDecision cd LEFT JOIN FETCH cd.evidences WHERE cd.claimId = :claimId ORDER BY cd.createdAt DESC")
    Optional<ClaimDecision> findLatestByClaimIdWithEvidence(@Param("claimId") Long claimId);
}
