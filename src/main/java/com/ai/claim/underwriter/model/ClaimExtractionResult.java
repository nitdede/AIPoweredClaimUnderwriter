package com.ai.claim.underwriter.model;

import java.util.List;
import java.util.Map;

public class ClaimExtractionResult {

    public String patientName;
    public String policyNumber;
    public String hospitalName;
    public String invoiceNumber;
    public String dateOfService;  // YYYY-MM-DD
    public String currency;       // INR, USD...

    public List<LineItem> lineItems;

    public Double totalAmount;

    // confidence map for critical fields
    public Map<String, Double> confidence;

    public static class LineItem {
        public String description;
        public Double amount;
        public Double confidence;
    }
}
