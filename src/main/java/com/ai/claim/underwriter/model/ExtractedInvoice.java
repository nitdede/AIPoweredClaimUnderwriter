package com.ai.claim.underwriter.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record ExtractedInvoice(
        @JsonProperty("patientName") String patientName,
        @JsonProperty("invoiceNumber") String invoiceNumber,
        @JsonProperty("dateOfService") String dateOfService,
        @JsonProperty("totalAmount") Double totalAmount,
        @JsonProperty("currency") String currency,
        @JsonProperty("hospitalName") String hospitalName,
        @JsonProperty("lineItems") List<LineItem> lineItems,
        @JsonProperty("confidence") Map<String, Double> confidence
) {
    public record LineItem(
            @JsonProperty("desc") String desc, 
            @JsonProperty("amount") Double amount, 
            @JsonProperty("confidence") Double confidence
    ) {}
}
