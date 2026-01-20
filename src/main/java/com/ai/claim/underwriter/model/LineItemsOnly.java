package com.ai.claim.underwriter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LineItemsOnly(
        @JsonProperty("lineItems") List<ExtractedInvoice.LineItem> lineItems
) {}
