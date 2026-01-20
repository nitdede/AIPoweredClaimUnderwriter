package com.ai.claim.underwriter.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "claim_decisions")
public class ClaimDecision {

    // Getters and Setters
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

    @JsonIgnore
    @OneToMany(mappedBy = "claimDecision", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClaimDecisionEvidence> evidences = new ArrayList<>();

    // Default constructor
    public ClaimDecision() {}

}
