package com.ai.claim.underwriter.repository;

import com.ai.claim.underwriter.entity.ClaimAIResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimAIResultDB extends JpaRepository<ClaimAIResult, Integer> {
}
