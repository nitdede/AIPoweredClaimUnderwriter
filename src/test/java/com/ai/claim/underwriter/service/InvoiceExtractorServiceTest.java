package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.model.ExtractedInvoice;
import com.ai.claim.underwriter.model.LineItemsOnly;
import com.ai.claim.underwriter.model.MetadataOnly;
import com.ai.claim.underwriter.model.InvoiceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class InvoiceExtractorServiceTest {

    private ChatClient.Builder chatClientBuilder;
    private ChatClient chatClient;
    private InvoiceContext invoiceContext;
    private Executor executor;
    private InvoiceExtractorService service;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        invoiceContext = new InvoiceContext();
        executor = Runnable::run; // Direct execution for tests
        service = new InvoiceExtractorService(chatClientBuilder, invoiceContext, executor);
    }





    @Test
    void extract_throwsWhenExceptionOccurs() {
        when(chatClient.prompt().system(any(Resource.class)).user(anyString()).options(any(ChatOptions.class))
                .call().entity(MetadataOnly.class)).thenThrow(new RuntimeException("AI call failed"));

        assertThatThrownBy(() -> service.extract("invoice text"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse invoice");
    }
}
