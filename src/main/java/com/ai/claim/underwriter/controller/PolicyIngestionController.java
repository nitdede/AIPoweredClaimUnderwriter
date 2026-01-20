package com.ai.claim.underwriter.controller;

import com.ai.claim.underwriter.exception.FileProcessingException;
import com.ai.claim.underwriter.model.PolicyMataData;
import com.ai.claim.underwriter.service.PolicyIngestionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/ingestion")
public class PolicyIngestionController {

    private final List<String> allowedTypes = Arrays.asList("application/pdf", "text/plain");
    private final PolicyIngestionService policyIngestionService;

    public PolicyIngestionController(PolicyIngestionService policyIngestionService) {
        this.policyIngestionService = policyIngestionService;
    }

    @PostMapping("/savePolicyDocument")
    public ResponseEntity<String> savePolicyDocument(@RequestPart("policyId") String policyId, @RequestPart("customerId") String customerId, @RequestPart("policyNumber") String policyNumber, @RequestPart("policy")MultipartFile file) throws IOException {

        if (!allowedTypes.contains(file.getContentType())) {
            throw new FileProcessingException("Unsupported file type: " + file.getContentType());
        }

        PolicyMataData policyMataData = new PolicyMataData(policyId, customerId, policyNumber);
        return ResponseEntity.ok(policyIngestionService.performRAG(file, policyMataData));
    }
}
