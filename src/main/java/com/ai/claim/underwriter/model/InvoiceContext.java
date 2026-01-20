package com.ai.claim.underwriter.model;

import org.springframework.stereotype.Component;

/**
 * Thread-safe context holder to share extracted invoice data between tools.
 * Uses ThreadLocal to ensure each request has its own isolated context.
 * This bypasses the unreliable LLM final response - we capture the result
 * directly from the extract tool and return it.
 */
@Component
public class InvoiceContext {
    private final ThreadLocal<ExtractedInvoice> lastExtractedInvoice = new ThreadLocal<>();

    public void setLastExtractedInvoice(ExtractedInvoice invoice) {
        this.lastExtractedInvoice.set(invoice);
    }

    public ExtractedInvoice getLastExtractedInvoice() {
        return lastExtractedInvoice.get();
    }

    public void clear() {
        lastExtractedInvoice.remove();
    }
}
