package com.ai.claim.underwriter.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_decisions")
public class ClaimDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "claim_id", nullable = false)
    private Long claimId;
    
    @Column(name = "decision", length = 30, nullable = false)
    private String decision;
    
    @Column(name = "payable_amount", precision = 10, scale = 2)
    private BigDecimal payableAmount;
    
    @Column(name = "reasons")
    @JdbcTypeCode(SqlTypes.JSON)
    private String reasons;
    
    @Column(name = "letter", columnDefinition = "TEXT")
    private String letter;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor
    public ClaimDecision() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public BigDecimal getPayableAmount() {
        return payableAmount;
    }

    public void setPayableAmount(BigDecimal payableAmount) {
        this.payableAmount = payableAmount;
    }

    public String getReasons() {
        return reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
