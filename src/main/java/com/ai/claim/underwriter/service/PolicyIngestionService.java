package com.ai.claim.underwriter.service;

import com.ai.claim.underwriter.model.PolicyMataData;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class PolicyIngestionService {

    private final VectorStore vectorStore;
    private final ResourceLoader resourceLoader;

    public PolicyIngestionService(VectorStore vectorStore, ResourceLoader resourceLoader) {
        this.vectorStore = vectorStore;
        this.resourceLoader = resourceLoader;
    }

    public String performRAG(String policyDocumentPath, PolicyMataData metadata) throws IOException {

        // ✅ Add metadata to each chunk BEFORE storing
        Map<String, String> meta = Map.of(
                "policyId", metadata.policyId(),
                "customerId", metadata.customerId(),
                "policyNumber", metadata.policyNumber()
        );

        // 1. Load text file as Document
        Resource resource = resourceLoader.getResource(policyDocumentPath);
        List<Document> documents = new TextReader(resource).get();

        // 2. Split into chunks
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.split(documents);

        // Add metadata to each chunk
        List<Document> chunksWithMeta = chunks.stream().map(chunk -> {
            chunk.getMetadata().putAll(meta);
            return chunk;
        }).toList();


        // 3. Embed + save to pgvector (automatic)
        vectorStore.add(chunks);

        return "Policy stored in vector DB. Chunks:"  + chunks.size();
    }
}
