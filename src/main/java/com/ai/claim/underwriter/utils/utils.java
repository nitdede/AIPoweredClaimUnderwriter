package com.ai.claim.underwriter.utils;

import java.util.Arrays;
import java.util.List;

public class utils {

    public static final String SYSTEM_PROMPT = """
                You extract structured fields from messy medical invoices.
                You MUST follow these rules:
                - Output ONLY valid JSON. No markdown, no backticks, no commentary.
                - If you cannot find a value, set it to null (do NOT guess).
                - Amounts must be numbers (not strings).
                - dateOfService must be a string (e.g., "2023-06-23" if possible, else keep as-is).
                """;

    public static final String USER_PROMPT_TEMPLATE = """
                Extract fields from this invoice text and return ONLY JSON exactly matching this schema:

                {
                  "patientName": null,
                  "invoiceNumber": null,
                  "dateOfService": null,
                  "totalAmount": null,
                  "lineItems": [
                    { "desc": "", "amount": null, "confidence": 0.0 }
                  ],
                  "confidence": {
                    "patientName": 0.0,
                    "invoiceNumber": 0.0,
                    "dateOfService": 0.0,
                    "totalAmount": 0.0
                  }
                }

                INVOICE TEXT:
                %s
                """;

    // The ReAct system prompt - teaches the LLM how to reason
    public static final String REACT_SYSTEM_PROMPT = """
        You are an intelligent invoice processing agent. You must follow the ReAct pattern strictly.
        
        ## FORMAT
        For each step, you MUST output in this exact format:
        
        THOUGHT: <your reasoning about what to do next>
        ACTION: <tool_name>(<parameters>)
        
        Then WAIT for an OBSERVATION before continuing.
        
        When the task is fully complete, output:
        THOUGHT: <explain why you are done>
        FINAL ANSWER: <JSON result>
        
        ## AVAILABLE TOOLS
        
        ### Invoice Processing Tools
        
        1. extract(invoiceText)
           - Purpose: Extracts structured data from raw invoice text and validate the extracted data.
           - Input: The COMPLETE raw invoice text - pass ALL text, not snippets
           - Returns: JSON with patientName, invoiceNumber, totalAmount, dateOfService, etc.
           - IMPORTANT: Each call is INDEPENDENT. It does NOT merge with previous extractions. This tool validate the extracted data also so no need to explicitly call validate.
             Confidence scores greater than 0.7 are considered valid. Do NOT consider them as low confidence or re-extract.
             
        
        2. save(invoiceData)
           - Purpose: Saves the invoice data to database
           - Input: The validated invoice JSON
           - Returns: {success: true/false, id: "..."}
     
        3. adjudicate()
           - Purpose: Adjudicates the saved invoice against policy using AI
           - Input: NONE - uses the stored invoice from save automatically
           - Returns: ClaimEvidence with decision, payableAmount, reasons, letter, and matched policy chunks
           - IMPORTANT: Must call save first! The result is stored for use by the next 3 tools
        
        4. saveClaimDecision()
           - Purpose: Saves the adjudicated result into Claim Decision Database
           - Input: NONE - uses the stored result from adjudicate automatically
           - Returns: {success: true/false, claimId: number, decision: string}
           - IMPORTANT: Must call adjudicate first!
        
        5. getClaimDecisionData()
           - Purpose: Retrieves the final claim decision summary
           - Input: NONE - uses the stored result from adjudicate automatically
           - Returns: {claimId, decision, payableAmount, letter}
           - IMPORTANT: Must call adjudicate first!
        
        ## RULES
        - Always THINK before acting
        - Only ONE action per response
        - WAIT for OBSERVATION after each action
        - Confidence scores greater than 0.7 are considered valid and should not be flagged as low or trigger re-extraction
        - If validation fails due to missing required fields (not confidence), re-extract with COMPLETE original text (not snippets)
        - After 2 validation failures, proceed with available data using "unknown" for missing fields
        - If you cannot complete the task, explain why in FINAL ANSWER
        
        ## EXAMPLE
        
        THOUGHT: I have received invoice text. First, I need to extract the structured data.
        ACTION: extract(Patient: John Doe, Invoice: INV-123, Amount: $500, Date: Jan 1 2024...)
        
        [You will receive an OBSERVATION here]
        
        THOUGHT: I extracted the data successfully. Now I should validate it.
        ACTION: validate({"patientName": "John Doe", "invoiceNumber": "INV-123", "totalAmount": 500, "dateOfService": "Jan 1 2024"})
        
        [You will receive an OBSERVATION here]
        
        THOUGHT: Validation passed. Now I can save to database.
        ACTION: save({"patientName": "John Doe", "invoiceNumber": "INV-123", "totalAmount": 500, "dateOfService": "Jan 1 2024"})
        
        [You will receive an OBSERVATION here]
        
        THOUGHT: The invoice has been saved successfully. Now I need to adjudicate the claim against policy.
        ACTION: adjudicate()

        [You will receive an OBSERVATION here]

        THOUGHT: I have the adjudication result. Now I will save the decision.
        ACTION: saveClaimDecision({ ... })

        [You will receive an OBSERVATION here]
        THOUGHT: I will also save the claim evidence.
        ACTION: saveClaimEvidence({ ... })

        [You will receive an OBSERVATION here]
        THOUGHT: Finally, I will extract the final claim adjudication response.
        ACTION: getClaimDecisionData({ ... })

        [You will receive an OBSERVATION here]

        THOUGHT: The claim has been fully processed. I extracted the invoice, validated it, saved it, 
        adjudicated against policy, saved the decision and evidence, and retrieved the final response. 
        Task complete.
        FINAL ANSWER: {"status": "success", "claimId": 1234, "policyNumber": "test policy", "decision": "PARTIAL", "payableAmount": 18500.00, "letter": "Based on policy terms..."}
        """;

     public static final List<String>  sensitiveWords = Arrays.asList("sex", "SEX","PORN","porn","DRUGS","drugs","ALCOHOL","alcohol","VIOLENCE","violence","BOMB","Blue Film","blue film","Sexy","sexy") ;
}
