package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.tools.InvoiceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceExtractorService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final InvoiceContext invoiceContext;

    public InvoiceExtractorService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper, InvoiceContext invoiceContext) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.invoiceContext = invoiceContext;
    }

    @Tool(description = "Extracts structured invoice data from invoice text and validate the extracted data.")
    public Map<String, Object>  extract(String invoiceText) {
        System.out.println("Extracting structured invoice data from text");

        String system = """
                You are an invoice data extraction system. Your job is to extract structured data from medical invoice text and validate the extracted data.
                
                CRITICAL: Your response must be ONLY valid JSON. No explanations, no markdown, no additional text.
                
                IMPORTANT: Confidence scores greater than 0.7 are considered valid. Do not attempt re-extraction for scores above this threshold.
                
                Extract the following fields from the invoice text:
                - patientName: The patient's full name
                - invoiceNumber: The invoice/bill number
                - dateOfService: The date of service (as a string, e.g., "Jun 23 2023")
                - totalAmount: The total/net amount as a number (no currency symbol)
                - currency: The currency code (e.g., "INR", "USD")
                - hospitalName: The name of the hospital or medical facility
                - lineItems: An array of services/procedures, each with:
                  - desc: Description of the service
                  - amount: Cost as a number
                  - confidence: Your confidence in this extraction (0.0 to 1.0)
                - confidence: An object with confidence scores for each field (optional)
                
                RESPONSE FORMAT - Return ONLY this JSON:
                {
                  "patientName": "Patient Name",
                  "invoiceNumber": "INV-12345",
                  "dateOfService": "Jun 23 2023",
                  "totalAmount": 6300.0,
                  "currency": "INR",
                  "hospitalName": "Hospital Name",
                  "lineItems": [
                    {"desc": "Blood Test", "amount": 2500.0, "confidence": 0.95},
                    {"desc": "X-Ray", "amount": 1800.0, "confidence": 0.90}
                  ],
                  "confidence": {"patientName": 0.95, "invoiceNumber": 0.90}
                }
                """;

        String user = """
                INVOICE SUMMARY:
                %s
                """.formatted(invoiceText);

        String response = chatClient.prompt()
                .system(system)
                .user(user)
                .options(ChatOptions.builder().temperature(0.0).build())
                .call()
                .content();

        try {
            // Sanitize the response - strip markdown and fix expressions
            String sanitizedResponse = sanitizeJsonResponse(response);
            ExtractedInvoice invoice = objectMapper.readValue(sanitizedResponse, ExtractedInvoice.class);
            // Store in context for the save tool to use
            invoiceContext.setLastExtractedInvoice(invoice);

            System.out.println("Extracted invoice: " + invoice);

            List<String> issues = new ArrayList<>();

            if (invoice.patientName() == null || invoice.patientName().isBlank()) {
                issues.add("Missing patient name");
            }
            if (invoice.invoiceNumber() == null || invoice.invoiceNumber().isBlank()) {
                issues.add("Missing invoice number");
            }
            if (invoice.totalAmount() == null || invoice.totalAmount() <= 0) {
                issues.add("Invalid or missing total amount");
            }
            if (invoice.dateOfService() == null || invoice.dateOfService().isBlank()) {
                issues.add("Missing date of service");
            }

            // Build validation result
            Map<String, Object> result = new HashMap<>();
            result.put("valid", issues.isEmpty());
            result.put("issues", issues);
            result.put("invoice", invoice);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse invoice: " + response, e);
        }
    }

    /**
     * Sanitizes JSON response by:
     * 1. Stripping markdown code blocks (```json ... ```)
     * 2. Evaluating arithmetic expressions
     */
    private String sanitizeJsonResponse(String json) {
        // Step 1: Strip markdown code blocks if present
        json = json.trim();
        if (json.startsWith("```")) {
            // Remove opening ```json or ``` 
            int firstNewline = json.indexOf('\n');
            if (firstNewline > 0) {
                json = json.substring(firstNewline + 1);
            }
            // Remove closing ```
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();
        }
        
        // Step 2: Fix arithmetic expressions in JSON (e.g., 2500 + 1800 + 20000)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(\"\\w+\"\\s*:\\s*)(\\d+(?:\\.\\d+)?(?:\\s*[+\\-*/]\\s*\\d+(?:\\.\\d+)?)+)");
        java.util.regex.Matcher matcher = pattern.matcher(json);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String expression = matcher.group(2);
            try {
                // Evaluate the arithmetic expression
                double value = evaluateExpression(expression);
                matcher.appendReplacement(result, prefix + value);
            } catch (Exception e) {
                // If evaluation fails, keep original
                matcher.appendReplacement(result, matcher.group(0));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Simple arithmetic expression evaluator for + and - operations
     */
    private double evaluateExpression(String expression) {
        // Remove spaces and split by + or -
        expression = expression.replaceAll("\\s+", "");
        double result = 0;
        String[] addParts = expression.split("\\+");
        for (String part : addParts) {
            if (part.contains("-")) {
                String[] subParts = part.split("-");
                double subResult = Double.parseDouble(subParts[0]);
                for (int i = 1; i < subParts.length; i++) {
                    subResult -= Double.parseDouble(subParts[i]);
                }
                result += subResult;
            } else {
                result += Double.parseDouble(part);
            }
        }
        return result;
    }
}
