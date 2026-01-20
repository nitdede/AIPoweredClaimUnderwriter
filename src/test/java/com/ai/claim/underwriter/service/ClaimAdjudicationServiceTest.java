package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.entity.ClaimDecisionEvidence;
import com.ai.claim.underwriter.model.ClaimAdjudicationRequest;
import com.ai.claim.underwriter.model.ClaimAdjudicationResponse;
import com.ai.claim.underwriter.model.ClaimEvidence;
import com.ai.claim.underwriter.repository.ClaimDecisionDB;
import com.ai.claim.underwriter.repository.ClaimDecisionEvidenceDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class ClaimAdjudicationServiceTest {

    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ClaimDecisionDB claimDecisionDB;

    @Mock
    private ClaimDecisionEvidenceDB claimDecisionEvidenceDB;

    private ChatClient.Builder chatClientBuilder;
    private ChatClient chatClient;
    private ClaimAdjudicationService service;
    private DataBaseOperationService dataBaseOperationService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        service = new ClaimAdjudicationService(
                chatClientBuilder,
                new ObjectMapper(),
                vectorStore,
                claimDecisionDB,
                claimDecisionEvidenceDB,
                DIRECT_EXECUTOR
        );
    }









    @Test
    void getClaimDecisionData_mapsValues() {
        ClaimDecision decision = new ClaimDecision();
        decision.setClaimId(123L);
        decision.setDecision("PARTIAL");
        decision.setPayableAmount(new BigDecimal("42.00"));
        decision.setLetter("letter");

        ClaimEvidence evidence = new ClaimEvidence(List.of(), decision, List.of("e1"), "[]");

        ClaimAdjudicationResponse response = service.getClaimDecisionData(evidence);

        assertThat(response.claimId()).isEqualTo(123L);
        assertThat(response.decision()).isEqualTo("PARTIAL");
        assertThat(response.payableAmount()).isEqualTo(42.0);
        assertThat(response.evidenceChunks()).containsExactly("e1");
        assertThat(response.letter()).isEqualTo("letter");
    }


}
