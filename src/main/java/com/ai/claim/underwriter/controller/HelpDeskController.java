package com.ai.claim.underwriter.controller;

import com.ai.claim.underwriter.exception.SensitiveKeywordException;
import com.ai.claim.underwriter.model.HelpDeskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/helpdesk-call")
public class HelpDeskController {

    private static final Logger logger = LoggerFactory.getLogger(HelpDeskController.class);
    private final ChatClient chatClient;
    private String userIssueDescription;

    public HelpDeskController(@Qualifier("helpDeskClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/helpUser")
    public Flux<String> handleUserQuery(@RequestBody HelpDeskRequest helpDeskRequest) {

        String issueDescription = helpDeskRequest.issueDescription();
        String claimId = helpDeskRequest.claimId();
        String userName = helpDeskRequest.customerName();
        String policyNumber = helpDeskRequest.policyNumber();

        if(userIssueDescription == null || userIssueDescription.isEmpty()) {
            userIssueDescription = issueDescription;
        }

        // Include user context in the prompt so AI knows the name and claim ID
        String promptWithContext = String.format(
                "Customer Name: %s\nClaim ID: %s\n\nissueDescription: %s\npolicyNumber: %s",
                userName, claimId, issueDescription,policyNumber
        );

        return chatClient.prompt()
                .user(promptWithContext)
                .advisors(advisorSpec -> advisorSpec.param("CONVERSATION_ID", userName))
                .stream().content()
                .onErrorResume(ex -> {
                    logger.info("Exception caught in reactive stream. Type: {}, Message: {}", ex.getClass().getName(), ex.getMessage());

                    // Walk through the exception chain to find the root cause
                    Throwable current = ex;
                    Throwable actualException = ex;
                    int depth = 0;

                    while (current != null && depth < 10) {
                        logger.info("Exception chain [{}]: Type: {}, Message: {}", depth, current.getClass().getName(), current.getMessage());

                        if (current instanceof SensitiveKeywordException || current instanceof IllegalArgumentException) {
                            actualException = current;
                            break;
                        }
                        current = current.getCause();
                        depth++;
                    }

                    if (actualException instanceof SensitiveKeywordException) {
                        logger.warn("Sensitive keyword detected in user input: {}", actualException.getMessage());
                        return Flux.just("⚠️ Your request has been blocked. " + actualException.getMessage());
                    } else if (actualException instanceof IllegalArgumentException) {
                        logger.warn("Invalid input detected: {}", actualException.getMessage());
                        return Flux.just("⚠️ Invalid input: " + actualException.getMessage());
                    } else {
                        logger.error("Unexpected error in help desk chat: {}", ex.getMessage(), ex);
                        return Flux.just("❌ An error occurred while processing your request. Please try again later.");
                    }
                });
    }
}
