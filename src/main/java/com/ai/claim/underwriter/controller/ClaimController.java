package com.ai.claim.underwriter.controller;

import com.ai.claim.underwriter.exception.FileProcessingException;
import com.ai.claim.underwriter.model.ClaimProcessingResult;
import com.ai.claim.underwriter.model.ExtractRequest;
import com.ai.claim.underwriter.service.ReActAgentService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/claims")
public class ClaimController {

    private final List<String> allowedTypes = Arrays.asList("application/pdf", "text/plain");

    private final ReActAgentService reActAgentService;

    public ClaimController(ReActAgentService reActAgentService) {
        this.reActAgentService = reActAgentService;
    }

    @PostMapping("/process-react")
    public ClaimProcessingResult processWithReAct(@RequestBody ExtractRequest request, @RequestParam String policyNumber, @RequestParam String userName) {
        return reActAgentService.processWithReAct(request, policyNumber, userName);
    }

    @PostMapping(value = "/process-claim", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClaimProcessingResult processClaimFile(
            @RequestPart("file") MultipartFile file, 
            @RequestParam(value = "policyNumber", required = true) String policyNumber, 
            @RequestParam(value = "patientName", required = true) String patientName) throws IOException {

        String invoice;
        if (!allowedTypes.contains(file.getContentType())) {
            throw new FileProcessingException("Unsupported file type: " + file.getContentType());
        }

        if (file.getContentType().equals(MediaType.APPLICATION_PDF_VALUE)) {
            invoice = extractFileData(file);
        } else {
            byte[] bytes = file.getBytes();
            invoice = new String(bytes, StandardCharsets.UTF_8);
        }

        ExtractRequest request = new ExtractRequest(invoice);
        return reActAgentService.processWithReAct(request, policyNumber, patientName);

    }

    private String extractFileData(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             PDDocument doc = PDDocument.load(is)) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);   // keeps lines more natural
            return stripper.getText(doc);
        }


    }
}
