package com.ai.claim.underwriter.controller;

import com.ai.claim.underwriter.model.HelpDeskRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/helpdesk-call")
public class HelpDeskController {


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
                .stream().content();
    }
}
