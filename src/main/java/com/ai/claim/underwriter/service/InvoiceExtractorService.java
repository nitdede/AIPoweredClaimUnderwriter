package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.model.InvoiceContext;
import com.ai.claim.underwriter.model.LineItemsOnly;
import com.ai.claim.underwriter.model.MetadataOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.ai.claim.underwriter.utils.AbstractConstant.*;

@Service
public class InvoiceExtractorService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceExtractorService.class);
    private final ChatClient chatClient;
    private final InvoiceContext invoiceContext;
    private final Executor blockingTaskExecutor;

    @Value("classpath:/templates/metadataExtractionPrompt.st")
    Resource metadataExtractionPrompt;

    @Value("classpath:/templates/lineItemsExtractionPrompt.st")
    Resource lineItemsExtractionPrompt;

    public InvoiceExtractorService(ChatClient.Builder chatClientBuilder, 
                                   InvoiceContext invoiceContext,
                                   @Qualifier("blockingTaskExecutor") Executor blockingTaskExecutor) {
        this.chatClient = chatClientBuilder.build();
        this.invoiceContext = invoiceContext;
        this.blockingTaskExecutor = blockingTaskExecutor;
    }

    public Map<String, Object> extract(String invoiceText) {

        List<String> issues = new ArrayList<>();
        logger.info("Extracting structured invoice data using two-phase parallel approach");

        try {
            // Phase 1: Extract metadata (parallel)
            CompletableFuture<MetadataOnly> metadataFuture = CompletableFuture.supplyAsync(() -> {
                logger.info("Phase 1: Extracting metadata");
                return extractMetadataOnly(invoiceText);
            }, blockingTaskExecutor);

            // Phase 2: Extract line items (parallel)
            CompletableFuture<LineItemsOnly> itemsFuture = CompletableFuture.supplyAsync(() -> {
                logger.info("Phase 2: Extracting line items");
                String itemizedSection = extractItemizedSection(invoiceText);
                List<String> chunks = chunkTextByLines(itemizedSection, 80);
                List<CompletableFuture<LineItemsOnly>> futures = chunks.stream()
                        .map(chunk -> CompletableFuture.supplyAsync(() -> extractLineItemsOnly(chunk), blockingTaskExecutor))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                List<LineItemsOnly> parts = futures.stream()
                        .map(CompletableFuture::join)
                        .toList();

                return mergeLineItems(parts);
            }, blockingTaskExecutor);

            // Wait for both phases to complete
            CompletableFuture.allOf(metadataFuture, itemsFuture).join();

            MetadataOnly metadata = metadataFuture.get();
            LineItemsOnly items = itemsFuture.get();

            logger.info("Both phases completed. Merging results...");

            // Merge results into ExtractedInvoice
            ExtractedInvoice invoice = new ExtractedInvoice(
                    metadata.patientName(),
                    metadata.invoiceNumber(),
                    metadata.dateOfService(),
                    metadata.totalAmount(),
                    metadata.currency(),
                    metadata.hospitalName(),
                    items.lineItems(),
                    Map.of() // confidence map
            );

            // Store in context for the save tool to use
            invoiceContext.setLastExtractedInvoice(invoice);

            logger.info(EXTRACTED_INVOICE + "patientName={}, invoiceNumber={}, lineItems count={}",
                    invoice.patientName(), invoice.invoiceNumber(),
                    invoice.lineItems() != null ? invoice.lineItems().size() : 0);

            // Validate
            if (invoice.patientName() == null || invoice.patientName().isBlank()) {
                issues.add(MISSING_PATIENT_NAME);
                throw new RuntimeException(MISSING_PATIENT_NAME);
            }
            if (invoice.invoiceNumber() == null || invoice.invoiceNumber().isBlank()) {
                issues.add(MISSING_INVOICE_NUMBER);
            }
            if (invoice.totalAmount() == null || invoice.totalAmount() <= 0) {
                issues.add(INVALID_OR_MISSING_TOTAL_AMOUNT);
            }
            if (invoice.dateOfService() == null || invoice.dateOfService().isBlank()) {
                issues.add(MISSING_DATE_OF_SERVICE);
            }

            // Build validation result
            Map<String, Object> result = new HashMap<>();
            result.put("valid", issues.isEmpty());
            result.put("issues", issues);
            result.put("invoice", invoice);

            return result;

        } catch (Exception e) {
            logger.error("Failed to extract invoice: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse invoice: " + e.getMessage(), e);
        }
    }

    /**
     * Phase 1: Extract only metadata fields
     */
    private MetadataOnly extractMetadataOnly(String invoiceText) {
        String user = """
                INVOICE TEXT:
                %s
                """.formatted(invoiceText);

        return chatClient.prompt()
                .system(metadataExtractionPrompt)
                .user(user)
                .options(ChatOptions.builder().model("gpt-4o").temperature(0.0).build())
                .call()
                .entity(MetadataOnly.class);
    }

    /**
     * Phase 2: Extract only line items
     */
    private LineItemsOnly extractLineItemsOnly(String itemizedText) {
        String user = """
                ITEMIZED SERVICES SECTION:
                %s
                """.formatted(itemizedText);

        return chatClient.prompt()
                .system(lineItemsExtractionPrompt)
                .user(user)
                .options(ChatOptions.builder().temperature(0.0).build())
                .call()
                .entity(LineItemsOnly.class);
    }

    /**
     * Extract the itemized/line items section from the full invoice text
     */
    private String extractItemizedSection(String fullText) {
        // Common section markers in invoices
        String[] markers = {
                "ITEMIZED SERVICES", "LINE ITEMS", "SERVICES:", "CHARGES:",
                "BILL DETAILS", "PARTICULARS", "DIAGNOSTICS", "PHARMACY",
                "PROCEDURE DETAILS", "ROOM / SERVICE", "CHARGES BREAKDOWN",
                "BILLING DETAILS", "SERVICE DETAILS", "TREATMENT CHARGES",
                "INVESTIGATION", "CONSULTATION", "CONSUMABLES", "MEDICINE"
        };

        int earliestStart = -1;

        for (String marker : markers) {
            int index = fullText.toUpperCase().indexOf(marker);
            if (index != -1 && (earliestStart == -1 || index < earliestStart)) {
                earliestStart = index;
            }
        }

        if (earliestStart != -1) {
            // Extract from first marker to end
            String section = fullText.substring(earliestStart);
            logger.debug("Extracted itemized section starting at position {}, length: {}", earliestStart, section.length());
            return section;
        }

        // Fallback: return full text
        logger.warn("No itemized section markers found, using full invoice text");
        return fullText;
    }

    private List<String> chunkTextByLines(String text, int maxLinesPerChunk) {
        String[] lines = text.split("\\r?\\n");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int lineCount = 0;

        for (String line : lines) {
            current.append(line).append("\n");
            lineCount++;
            if (lineCount >= maxLinesPerChunk) {
                chunks.add(current.toString());
                current.setLength(0);
                lineCount = 0;
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private LineItemsOnly mergeLineItems(List<LineItemsOnly> parts) {
        Map<String, ExtractedInvoice.LineItem> merged = new LinkedHashMap<>();
        for (LineItemsOnly part : parts) {
            if (part == null || part.lineItems() == null) {
                continue;
            }
            for (var item : part.lineItems()) {
                String key = (item.desc() + "|" + item.amount()).toLowerCase().trim();
                merged.putIfAbsent(key, item);
            }
        }
        return new LineItemsOnly(new ArrayList<>(merged.values()));
    }

}
