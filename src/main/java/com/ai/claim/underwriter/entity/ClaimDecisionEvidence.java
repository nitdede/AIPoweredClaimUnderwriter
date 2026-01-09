package com.ai.claim.underwriter.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "claim_decision_evidence")
public class ClaimDecisionEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // decision_id BIGINT NOT NULL REFERENCES claim_decisions(id)
    @Column(name = "decision_id", nullable = false)
    private Long decisionId;

    // chunk_text TEXT NOT NULL
    @Column(name = "chunk_text", columnDefinition = "text", nullable = false)
    private String chunkText;

    // score NUMERIC(10,6)
    @Column(name = "score", precision = 10, scale = 6)
    private BigDecimal score;

    // created_at TIMESTAMPTZ DEFAULT now()
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public ClaimDecisionEvidence() {
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(Long decisionId) {
        this.decisionId = decisionId;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
