package com.ai.claim.underwriter.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitExceededException extends ClaimUnderwriterException {

    public RateLimitExceededException(String message) {
        super(message, "RATE_LIMIT_EXCEEDED");
    }

    public RateLimitExceededException(String clientId, int maxRequests) {
        super("Rate limit exceeded for client: " + clientId + ". Maximum allowed: " + maxRequests + " requests per minute",
              "RATE_LIMIT_EXCEEDED",
              createDetails(clientId, maxRequests));
    }

    private static Map<String, Object> createDetails(String clientId, int maxRequests) {
        Map<String, Object> map = new HashMap<>();
        map.put("clientId", clientId);
        map.put("maxRequests", maxRequests);
        return map;
    }
}
