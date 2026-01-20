package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.model.PolicyMataData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
@ExtendWith(MockitoExtension.class)
class PolicyIngestionServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ResourceLoader resourceLoader;

    private PolicyIngestionService service;

    @BeforeEach
    void setUp() {
        service = new PolicyIngestionService(vectorStore, resourceLoader);
    }

    @Test
    void performRag_addsMetadataAndStoresChunks() throws IOException {
        Resource resource = new ByteArrayResource(
                "Policy text for ingestion.".getBytes(StandardCharsets.UTF_8)
        );
        when(resourceLoader.getResource("classpath:policy.txt")).thenReturn(resource);

        PolicyMataData metadata = new PolicyMataData("pid-1", "cust-1", "pol-1");

        String result = service.performRAG("classpath:policy.txt", metadata);

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());
        List<Document> stored = captor.getValue();

        assertThat(stored).isNotEmpty();
        for (Document doc : stored) {
            assertThat(doc.getMetadata()).containsEntry("policyId", "pid-1");
            assertThat(doc.getMetadata()).containsEntry("customerId", "cust-1");
            assertThat(doc.getMetadata()).containsEntry("policyNumber", "pol-1");
        }

        assertThat(result).startsWith("Policy stored in vector DB. Chunks:");
    }
}
*/
