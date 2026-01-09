package com.ai.claim.underwriter.model;

public class ExtractResponse {
    public String status; // OK / NEEDS_INFO
    public ClaimExtractionResult result;
    public String message;

    public static ExtractResponse ok(ClaimExtractionResult r) {
        var x = new ExtractResponse();
        x.status = "OK";
        x.result = r;
        x.message = "Extraction completed.";
        return x;
    }

    public static ExtractResponse needsInfo(ClaimExtractionResult r, String msg) {
        var x = new ExtractResponse();
        x.status = "NEEDS_INFO";
        x.result = r;
        x.message = msg;
        return x;
    }
}
