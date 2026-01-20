package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.entity.ClaimDecision;
import com.ai.claim.underwriter.exception.PolicyNotFoundException;
import com.ai.claim.underwriter.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReActAgentServiceTest {

    private static final Executor DIRECT_EXECUTOR = Runnable::run;

    private ChatClient chatClient;
    private ChatClient.Builder chatClientBuilder;
    private ChatClient.ChatClientRequestSpec requestSpec;
    private ChatClient.CallResponseSpec responseSpec;

    @Mock
    private InvoiceExtractorService extractorService;

    @Mock
    private InvoiceContext invoiceContext;

    @Mock
    private ClaimAdjudicationService claimAdjudicationService;

    @Mock
    private DataBaseOperationService dataBaseOperationService;

    private ObjectMapper objectMapper;
    private ReActAgentService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Use RETURNS_DEEP_STUBS for fluent API mocking
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        chatClientBuilder = mock(ChatClient.Builder.class);
        requestSpec = mock(ChatClient.ChatClientRequestSpec.class, RETURNS_DEEP_STUBS);
        responseSpec = mock(ChatClient.CallResponseSpec.class);
        
        when(chatClientBuilder.build()).thenReturn(chatClient);

        service = new ReActAgentService(
                chatClientBuilder,
                extractorService,
                invoiceContext,
                objectMapper,
                claimAdjudicationService,
                dataBaseOperationService
        );
    }







    @Test
    void processWithReAct_exceedsMaxIterations_returnsErrorOrLastResult() {
        // Arrange
        String invoiceText = "Test invoice";
        String policyNumber = "POL-99999";
        String patientName = "Test Patient";

        ExtractRequest request = new ExtractRequest(invoiceText);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.system(any(org.springframework.core.io.Resource.class))).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);

        // Never returns FINAL ANSWER, keeps looping
        when(responseSpec.content())
                .thenReturn("THOUGHT: Keep processing\nACTION: unknown_action()");

        // Act
        ClaimProcessingResult result = service.processWithReAct(request, policyNumber, patientName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("error");
        assertThat(result.errorMessage()).contains("did not complete within");
    }

    @Test
    void messageTrimming_withLargeMessageHistory_trimsToLastFive() {
        // Arrange
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messages.add(new UserMessage("Message " + i));
        }

        // Act
        List<Message> trimmed = service.messageTrimming(messages);

        // Assert
        assertThat(trimmed).hasSize(5);
    }

    @Test
    void messageTrimming_withNullMessages_returnsEmptyList() {
        // Act
        List<Message> trimmed = service.messageTrimming(null);

        // Assert
        assertThat(trimmed).isNotNull();
        assertThat(trimmed).isEmpty();
    }

    @Test
    void messageTrimming_withFewerThanFiveMessages_returnsAll() {
        // Arrange
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage("Message 1"));
        messages.add(new AssistantMessage("Response 1"));
        messages.add(new UserMessage("Message 2"));

        // Act
        List<Message> trimmed = service.messageTrimming(messages);

        // Assert
        assertThat(trimmed).hasSize(3);
        assertThat(trimmed).isEqualTo(messages);
    }

    @Test
    void processWithReAct_withValidateAction_executesValidation() {
        // Arrange
        String invoiceText = "Patient: Test\nAmount: $100";
        String policyNumber = "POL-111";
        String patientName = "Test";

        ExtractRequest request = new ExtractRequest(invoiceText);

        ExtractedInvoice invoice = new ExtractedInvoice(
                patientName,
                "INV-111",
                "2024-01-15",
                100.0,
                "USD",
                "Hospital",
                List.of(),
                Map.of("patientName", 0.9)
        );

        Map<String, Object> extractResult = new HashMap<>();
        extractResult.put("invoice", invoice);
        extractResult.put("issues", List.of());

        when(extractorService.extract(anyString())).thenReturn(extractResult);
        when(invoiceContext.getLastExtractedInvoice()).thenReturn(invoice);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.system(any(org.springframework.core.io.Resource.class))).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content())
                .thenReturn("ACTION: extract(text)")
                .thenReturn("ACTION: validate(data)")
                .thenReturn("FINAL ANSWER: Done");

        // Act
        ClaimProcessingResult result = service.processWithReAct(request, policyNumber, patientName);

        // Assert
        assertThat(result).isNotNull();
        verify(extractorService).extract(anyString());
    }

    @Test
    void processWithReAct_withUnknownAction_asksForClarification() {
        // Arrange
        String invoiceText = "Test invoice";
        String policyNumber = "POL-222";
        String patientName = "Test";

        ExtractRequest request = new ExtractRequest(invoiceText);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.system(any(org.springframework.core.io.Resource.class))).thenReturn(requestSpec);
        when(requestSpec.options(any())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content())
                .thenReturn("THOUGHT: Some thought")  // No ACTION
                .thenReturn("FINAL ANSWER: OK");

        // Act
        ClaimProcessingResult result = service.processWithReAct(request, policyNumber, patientName);

        // Assert
        assertThat(result).isNotNull();
    }




}
