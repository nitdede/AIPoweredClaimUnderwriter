package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.model.ExtractRequest;
import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.tools.DataBaseTools;
import com.ai.claim.underwriter.tools.InvoiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class InvoiceAgentService {
    private static final Logger log = LoggerFactory.getLogger(InvoiceAgentService.class);
    private final ChatClient agentClient;
    private final InvoiceContext invoiceContext;

    public InvoiceAgentService(ChatClient.Builder builder, InvoiceExtractorService extractor, 
                                DataBaseTools dataBaseTools, InvoiceContext invoiceContext) {
        this.agentClient = builder.defaultTools(extractor, dataBaseTools)
                .build();
        this.invoiceContext = invoiceContext;
    }

    /**
     * Process invoice using agentic AI with tools.
     * The extract tool stores the result in InvoiceContext, which we retrieve directly
     * instead of relying on the LLM's potentially malformed final response.
     */
    public ExtractedInvoice processInvoiceAutonomously(ExtractRequest invoiceRequest) {
        // Clear any previous extraction
        invoiceContext.clear();
        
        String systemPrompt = """
                You are an invoice processing agent.
                
                AVAILABLE TOOLS:
                - extract: Extracts structured data from invoice text
                - saveInvoiceData: Saves extracted invoice data to database
                
                WORKFLOW:
                1. Call the extract tool with the invoice text
                2. Call saveInvoiceData to save the extracted data
                3. Respond with "DONE" when complete
                
                Do not repeat any action that was already successful.
                """;

        String userPrompt = """
                Process this medical invoice - extract the data and save it to database:
                
                %s
                """.formatted(invoiceRequest.invoiceText());

        // Let the agent execute tools - we don't care about the final response content
        try {
            String response = agentClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
            log.debug("Agent response: {}", response);
        } catch (Exception e) {
            log.warn("Agent execution had an issue, but checking if extraction succeeded: {}", e.getMessage());
        }
        
        // Get the extracted invoice directly from context (set by the extract tool)
        ExtractedInvoice extractedInvoice = invoiceContext.getLastExtractedInvoice();
        
        if (extractedInvoice == null) {
            throw new RuntimeException("Invoice extraction failed - no data was extracted");
        }
        
        log.info("Returning extracted invoice from context: {}", extractedInvoice.invoiceNumber());
        return extractedInvoice;
    }
}
