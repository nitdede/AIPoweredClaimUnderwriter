package com.ai.claim.underwriter.controller;

import com.ai.claim.underwriter.model.PolicyMataData;
import com.ai.claim.underwriter.service.PolicyIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/ingestion")
public class PolicyIngestionController {

    private final PolicyIngestionService policyIngestionService;
    public PolicyIngestionController(PolicyIngestionService policyIngestionService) {
        this.policyIngestionService = policyIngestionService;
    }


    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Service is working!");
    }

    @PostMapping("/saveDocument")
    public ResponseEntity<String> saveDocument(@RequestBody PolicyMataData policyMataData) throws IOException {

        String filePath = "classpath:policy.txt";
        return ResponseEntity.ok(policyIngestionService.performRAG(filePath, policyMataData));
    }
}
