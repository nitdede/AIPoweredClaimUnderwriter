package com.ai.claim.underwriter.controller;

import com.ai.claim.underwriter.model.ClaimProcessingResult;
import com.ai.claim.underwriter.model.ExtractRequest;
import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.service.InvoiceAgentService;
import com.ai.claim.underwriter.service.ReActAgentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/claims")
public class ClaimController {

    public final InvoiceAgentService invoiceAgentService;
    private final ReActAgentService reActAgentService; 

    public ClaimController(InvoiceAgentService invoiceAgentService, ReActAgentService reActAgentService) {
        this.invoiceAgentService = invoiceAgentService;
        this.reActAgentService = reActAgentService;
    }

    @PostMapping("readInvoice")
    private ResponseEntity<ExtractedInvoice> readAndSaveInvoiceIntoDB(@RequestBody ExtractRequest invoice) {

        return  ResponseEntity.ok(invoiceAgentService.processInvoiceAutonomously(invoice));
    }

    @PostMapping("/process-react")
    public ClaimProcessingResult processWithReAct(@RequestBody ExtractRequest request, @RequestParam String policyNumber) {
        return reActAgentService.processWithReAct(request,policyNumber);
    }
}
