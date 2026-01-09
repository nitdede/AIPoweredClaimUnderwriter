package com.ai.claim.underwriter.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_ai_result")
public class ClaimAIResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "patient_name", length = 100)
    private String patientName;
    
    @Column(name = "policy_number", length = 50)
    private String policyNumber;
    
    @Column(name = "hospital_name", length = 150)
    private String hospitalName;
    
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;
    
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "currency", length = 10)
    private String currency;
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    @Column(name = "ai_status", length = 20)
    private String aiStatus;
    
    @Column(name = "ai_output")
    @JdbcTypeCode(SqlTypes.JSON)
    private String aiOutput;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor
    public ClaimAIResult() {}

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getAiStatus() {
        return aiStatus;
    }

    public void setAiStatus(String aiStatus) {
        this.aiStatus = aiStatus;
    }

    public String getAiOutput() {
        return aiOutput;
    }

    public void setAiOutput(String aiOutput) {
        this.aiOutput = aiOutput;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
