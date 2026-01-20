package com.ai.claim.underwriter.tools;

import com.ai.claim.underwriter.model.InvoiceContext;
import com.ai.claim.underwriter.model.ExtractedInvoice;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceContextTest {

    @Test
    void setGetAndClear() {
        InvoiceContext context = new InvoiceContext();
        ExtractedInvoice invoice = new ExtractedInvoice(
                "Rajesh",
                "INV-1",
                "2024-01-01",
                100.0,
                "INR",
                "Hospital",
                List.of(),
                Map.of()
        );

        assertThat(context.getLastExtractedInvoice()).isNull();
        context.setLastExtractedInvoice(invoice);
        assertThat(context.getLastExtractedInvoice()).isEqualTo(invoice);
        context.clear();
        assertThat(context.getLastExtractedInvoice()).isNull();
    }
}
