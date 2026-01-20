package com.ai.claim.underwriter.controller;

import com.ai.claim.underwriter.model.HelpDeskRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/helpdesk-call")
public class HelpDeskController {


    private final ChatClient chatClient;
    private String userIssueDescription;

    public HelpDeskController(@Qualifier("helpDeskClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/helpdesk")
    public ResponseEntity<String> handleHelpDeskRequest(@RequestHeader("userName") String userName, @RequestParam("message") String prompt) {
        String answer = chatClient.prompt()
                .user(prompt)
                .toolContext(Map.of("userName", userName))
                .advisors(advisorSpec -> advisorSpec.param("CONVERSATION_ID", userName))
                .call().content();

        return ResponseEntity.ok(answer);
    }

    @PostMapping("/helpUser")
    public ResponseEntity<String> handleUserQuery(@RequestBody HelpDeskRequest helpDeskRequest) {

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

        String answer = chatClient.prompt()
                .user(promptWithContext)
                .advisors(advisorSpec -> advisorSpec.param("CONVERSATION_ID", userName))
                .call().content();

        return ResponseEntity.ok(answer);
    }
}
