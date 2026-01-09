# AIPoweredClaimUnderwriter

Lightweight Spring Boot service to extract invoice data and adjudicate insurance claims using AI agents.

## Prerequisites
- Java 17+ (or configured in `pom.xml`)
- Maven (`./mvnw` available)
- PostgreSQL running and reachable (default configured in `src/main/resources/application.yml`)
- (Optional) Ollama or other AI service configured per `application.yml` for embeddings/chat

## Build & Run
Build:
```bash
./mvnw clean package
```
Run locally:
```bash
./mvnw spring-boot:run
```

Configuration: see `src/main/resources/application.yml` for datasource and AI settings.

## API Endpoints
Base host: `http://localhost:8080`

- **GET /ingestion/test**
  - Description: health check for ingestion service.
  - Response: plain text `Service is working!`
  - Example:
    ```bash
    curl http://localhost:8080/ingestion/test
    ```

- **POST /ingestion/saveDocument**
  - Description: saves policy metadata and triggers RAG ingestion using the policy file.
  - Request body: `PolicyMataData` JSON
    ```json
    {
      "policyId": "policy-123",
      "customerId": "cust-456",
      "policyNumber": "POL-0001"
    }
    ```
  - Response: `String` message from the ingestion service.
  - Example:
    ```bash
    curl -X POST -H "Content-Type: application/json" \
      -d '{"policyId":"p1","customerId":"c1","policyNumber":"PN123"}' \
      http://localhost:8080/ingestion/saveDocument
    ```

- **POST /claims/readInvoice**
  - Description: extract invoice fields from raw invoice text and persist AI extraction results.
  - Request body: `ExtractRequest` JSON
    ```json
    { "invoiceText": "<raw invoice text here>" }
    ```
  - Response: `ExtractedInvoice` JSON
    ```json
    {
      "patientName": "John Doe",
      "invoiceNumber": "INV-001",
      "dateOfService": "2025-12-01",
      "totalAmount": 1234.56,
      "currency": "USD",
      "hospitalName": "General Hospital",
      "lineItems": [ { "desc":"Service A", "amount":100.0, "confidence":0.95 } ],
      "confidence": { "patientName": 0.98 }
    }
    ```
  - Example:
    ```bash
    curl -X POST -H "Content-Type: application/json" \
      -d '{"invoiceText":"Patient: John Doe\nTotal: 1234.56"}' \
      http://localhost:8080/claims/readInvoice
    ```

- **POST /claims/process-react**
  - Description: runs the ReAct agent pipeline using the same `ExtractRequest` input and returns adjudication result.
  - Request body: `ExtractRequest` (same as readInvoice)
  - Response: `ClaimProcessingResult` JSON (example):
    ```json
    {
      "status": "success",
      "claimId": 123,
      "decision": "APPROVED",
      "payableAmount": 1000.0,
      "reasons": ["In policy coverage"],
      "letter": "Decision explanation...",
      "errorMessage": null
    }
    ```
  - Example:
    ```bash
    curl -X POST -H "Content-Type: application/json" \
      -d '{"invoiceText":"..."}' \
      http://localhost:8080/claims/process-react
    ```

## Data Model / Database Tables
Default datasource is PostgreSQL (`jdbc:postgresql://localhost:5432/insurance_ai`) configured in `application.yml`.

1) Table: `claim_ai_result`
   - id INTEGER PRIMARY KEY GENERATED
   - patient_name VARCHAR(100)
   - policy_number VARCHAR(50)
   - hospital_name VARCHAR(150)
   - invoice_number VARCHAR(50)
   - total_amount NUMERIC(10,2)
   - currency VARCHAR(10)
   - confidence_score NUMERIC(3,2)
   - ai_status VARCHAR(20)
   - ai_output JSON
   - created_at TIMESTAMP

2) Table: `claim_decisions`
   - id BIGINT PRIMARY KEY GENERATED
   - claim_id BIGINT NOT NULL
   - decision VARCHAR(30) NOT NULL
   - payable_amount NUMERIC(10,2)
   - reasons JSON
   - letter TEXT
   - created_at TIMESTAMP

3) Table: `claim_decision_evidence`
   - id BIGINT PRIMARY KEY GENERATED
   - decision_id BIGINT NOT NULL  -- references claim_decisions(id)
   - chunk_text TEXT NOT NULL
   - score NUMERIC(10,6)
   - created_at TIMESTAMPTZ DEFAULT now()

4) Vectorstore table configured by application.yml: `policy_chunks` (pgvector)

### Example SQL (Postgres)
```sql
CREATE TABLE claim_ai_result (
  id serial PRIMARY KEY,
  patient_name varchar(100),
  policy_number varchar(50),
  hospital_name varchar(150),
  invoice_number varchar(50),
  total_amount numeric(10,2),
  currency varchar(10),
  confidence_score numeric(3,2),
  ai_status varchar(20),
  ai_output jsonb,
  created_at timestamp
);

CREATE TABLE claim_decisions (
  id bigserial PRIMARY KEY,
  claim_id bigint NOT NULL,
  decision varchar(30) NOT NULL,
  payable_amount numeric(10,2),
  reasons jsonb,
  letter text,
  created_at timestamp
);

CREATE TABLE claim_decision_evidence (
  id bigserial PRIMARY KEY,
  decision_id bigint NOT NULL REFERENCES claim_decisions(id),
  chunk_text text NOT NULL,
  score numeric(10,6),
  created_at timestamptz DEFAULT now()
);
```

## Notes & Next Steps
- Adjust `spring.datasource` in `src/main/resources/application.yml` to match your DB credentials.
- If you want a CI workflow or GitHub Actions to build and run tests on push, I can add it.

---
Generated from project source; controller signatures are in `src/main/java/com/ai/claim/underwriter/controller`.
