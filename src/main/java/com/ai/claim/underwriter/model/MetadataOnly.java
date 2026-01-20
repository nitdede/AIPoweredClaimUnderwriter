package com.ai.claim.underwriter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MetadataOnly(
        @JsonProperty("patientName") String patientName,
        @JsonProperty("invoiceNumber") String invoiceNumber,
        @JsonProperty("dateOfService") String dateOfService,
        @JsonProperty("totalAmount") Double totalAmount,
        @JsonProperty("currency") String currency,
        @JsonProperty("hospitalName") String hospitalName
) {}
