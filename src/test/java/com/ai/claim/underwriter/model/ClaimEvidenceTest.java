package com.ai.claim.underwriter.model;

import com.ai.claim.underwriter.entity.ClaimDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClaimEvidenceTest {

    private List<Document> sampleDocuments;
    private ClaimDecision sampleClaimDecision;

    @BeforeEach
    void setUp() {
        // Prepare sample documents
        Document doc1 = new Document("Policy covers emergency services");
        Document doc2 = new Document("Deductible is $500");
        sampleDocuments = Arrays.asList(doc1, doc2);

        // Prepare sample claim decision
        sampleClaimDecision = new ClaimDecision();
        sampleClaimDecision.setClaimId(12345L);
        sampleClaimDecision.setDecision("APPROVED");
    }

    @Test
    void testConstructorAndGetters() {
        // Arrange
        List<String> evidenceChunks = Arrays.asList("Evidence 1", "Evidence 2");
        String itemizedDecisions = "{\"service1\":\"covered\",\"service2\":\"not covered\"}";

        // Act
        ClaimEvidence claimEvidence = new ClaimEvidence(
                sampleDocuments, sampleClaimDecision, evidenceChunks, itemizedDecisions
        );

        // Assert
        assertNotNull(claimEvidence);
        assertEquals(sampleDocuments, claimEvidence.matches());
        assertEquals(sampleClaimDecision, claimEvidence.claimDecision());
        assertEquals(evidenceChunks, claimEvidence.evidenceChunks());
        assertEquals(itemizedDecisions, claimEvidence.itemizedDecisions());
    }

    @Test
    void testConstructorWithNullValues() {
        // Act
        ClaimEvidence claimEvidence = new ClaimEvidence(null, null, null, null);

        // Assert
        assertNotNull(claimEvidence);
        assertNull(claimEvidence.matches());
        assertNull(claimEvidence.claimDecision());
        assertNull(claimEvidence.evidenceChunks());
        assertNull(claimEvidence.itemizedDecisions());
    }

    @Test
    void testWithEmptyLists() {
        // Arrange
        List<Document> emptyDocs = Collections.emptyList();
        List<String> emptyEvidence = Collections.emptyList();

        // Act
        ClaimEvidence claimEvidence = new ClaimEvidence(
                emptyDocs, sampleClaimDecision, emptyEvidence, ""
        );

        // Assert
        assertTrue(claimEvidence.matches().isEmpty());
        assertTrue(claimEvidence.evidenceChunks().isEmpty());
        assertEquals("", claimEvidence.itemizedDecisions());
    }

    @Test
    void testEquals() {
        // Arrange
        List<String> evidence = Arrays.asList("Evidence");
        ClaimEvidence evidence1 = new ClaimEvidence(
                sampleDocuments, sampleClaimDecision, evidence, "decisions"
        );
        ClaimEvidence evidence2 = new ClaimEvidence(
                sampleDocuments, sampleClaimDecision, evidence, "decisions"
        );
        ClaimEvidence evidence3 = new ClaimEvidence(
                Collections.emptyList(), null, Collections.emptyList(), ""
        );

        // Assert
        assertEquals(evidence1, evidence2);
        assertNotEquals(evidence1, evidence3);
    }

    @Test
    void testHashCode() {
        // Arrange
        List<String> evidence = Arrays.asList("Evidence");
        ClaimEvidence evidence1 = new ClaimEvidence(
                sampleDocuments, sampleClaimDecision, evidence, "decisions"
        );
        ClaimEvidence evidence2 = new ClaimEvidence(
                sampleDocuments, sampleClaimDecision, evidence, "decisions"
        );

        // Assert
        assertEquals(evidence1.hashCode(), evidence2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        List<String> evidence = Arrays.asList("Test evidence");
        ClaimEvidence claimEvidence = new ClaimEvidence(
                sampleDocuments, sampleClaimDecision, evidence, "test decisions"
        );

        // Act
        String result = claimEvidence.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("matches"));
    }

    @Test
    void testWithMultipleDocuments() {
        // Arrange
        Document doc1 = new Document("Policy section 1");
        Document doc2 = new Document("Policy section 2");
        Document doc3 = new Document("Policy section 3");
        List<Document> documents = Arrays.asList(doc1, doc2, doc3);

        List<String> evidenceChunks = Arrays.asList(
                "Section 1 evidence",
                "Section 2 evidence",
                "Section 3 evidence"
        );

        // Act
        ClaimEvidence claimEvidence = new ClaimEvidence(
                documents, sampleClaimDecision, evidenceChunks, "multiple sections"
        );

        // Assert
        assertEquals(3, claimEvidence.matches().size());
        assertEquals(3, claimEvidence.evidenceChunks().size());
        assertEquals("Policy section 1", claimEvidence.matches().get(0).getText());
    }

    @Test
    void testWithDocumentMetadata() {
        // Arrange
        Document docWithMetadata = new Document(
                "Policy content",
                Map.of("source", "policy.pdf", "page", 5)
        );
        List<Document> documents = Collections.singletonList(docWithMetadata);

        // Act
        ClaimEvidence claimEvidence = new ClaimEvidence(
                documents, sampleClaimDecision, Collections.singletonList("Evidence"), "decisions"
        );

        // Assert
        assertEquals(1, claimEvidence.matches().size());
        Document retrievedDoc = claimEvidence.matches().get(0);
        assertEquals("Policy content", retrievedDoc.getText());
        assertEquals("policy.pdf", retrievedDoc.getMetadata().get("source"));
    }

    @Test
    void testCompleteClaimEvidence() {
        // Arrange
        List<Document> documents = Arrays.asList(
                new Document("Emergency services are covered at 100%"),
                new Document("Deductible: $500 per year"),
                new Document("Out-of-pocket maximum: $5000")
        );

        ClaimDecision decision = new ClaimDecision();
        decision.setClaimId(99999L);
        decision.setDecision("PARTIAL");

        List<String> evidenceChunks = Arrays.asList(
                "Emergency room visit covered",
                "Lab tests partially covered at 80%",
                "Medications not covered"
        );

        String itemizedDecisions = "{\"emergency\":\"100%\",\"lab\":\"80%\",\"medication\":\"0%\"}";

        // Act
        ClaimEvidence claimEvidence = new ClaimEvidence(
                documents, decision, evidenceChunks, itemizedDecisions
        );

        // Assert
        assertEquals(3, claimEvidence.matches().size());
        assertEquals("PARTIAL", claimEvidence.claimDecision().getDecision());
        assertEquals(3, claimEvidence.evidenceChunks().size());
        assertTrue(claimEvidence.itemizedDecisions().contains("emergency"));
        assertTrue(claimEvidence.itemizedDecisions().contains("80%"));
    }
}
