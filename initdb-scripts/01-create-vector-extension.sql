-- Create pgvector extension and example table used by the app
-- This script runs only on fresh database initialization
CREATE EXTENSION IF NOT EXISTS vector;

-- Create table `policy_chunks` to match application.yml vectorstore settings
-- dimension should match the app's embedding model (use 768 for Ollama embeddings)
CREATE TABLE policy_chunks (
   id UUID PRIMARY KEY,
   content TEXT NOT NULL,
   metadata JSON,
   embedding VECTOR(1536) NOT NULL
);

CREATE TABLE IF NOT EXISTS claim_decisions (
    id BIGSERIAL PRIMARY KEY,
    claim_id BIGINT NOT NULL,
    decision VARCHAR(30) NOT NULL,          -- APPROVED / PARTIAL / DENIED / NEEDS_INFO
    payable_amount NUMERIC(10,2),
    reasons JSONB,
    letter TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS claim_decision_evidence (
    id BIGSERIAL PRIMARY KEY,
    decision_id BIGINT NOT NULL REFERENCES claim_decisions(id),
    chunk_text TEXT NOT NULL,
    score NUMERIC(10,6),
    created_at TIMESTAMPTZ DEFAULT now()
    );


CREATE TABLE IF NOT EXISTS policies (
    policy_id      BIGSERIAL PRIMARY KEY,
    policy_code    TEXT UNIQUE NOT NULL,     -- e.g. 'NSC-CS-POL-001'
    version        TEXT NOT NULL,            -- e.g. '1.0'
    title          TEXT NOT NULL,
    effective_date DATE,
    content_text   TEXT NOT NULL,            -- full markdown/text
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS claim_ai_result (
    id SERIAL PRIMARY KEY,
    patient_name VARCHAR(100),
    policy_number VARCHAR(50),
    hospital_name VARCHAR(150),
    invoice_number VARCHAR(50),
    total_amount NUMERIC(10,2),
    currency VARCHAR(10),
    confidence_score NUMERIC(3,2),
    ai_status VARCHAR(20),
    ai_output JSONB,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now()
);

CREATE TABLE helpdesk_tickets (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    issue_description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    assigned_to VARCHAR(255),
    created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    eta TIMESTAMP WITHOUT TIME ZONE
);


-- Create HNSW index for cosine similarity (application uses vector_cosine_ops)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE c.relname = 'policy_chunks_index' AND n.nspname = 'public'
  ) THEN
CREATE INDEX policy_chunks_index ON public.policy_chunks USING hnsw (embedding vector_cosine_ops);
END IF;
END;
$$ LANGUAGE plpgsql;
