-- 기능성식품용 테이블
CREATE TABLE IF NOT EXISTS rag_documents_food (
  id          BIGSERIAL PRIMARY KEY,
  source_id   TEXT,
  chunk_id    INTEGER,
  category    TEXT DEFAULT 'functional_food',
  content     TEXT NOT NULL,
  metadata    JSONB DEFAULT '{}'::jsonb,
  embedding   vector(768),
  tsv         tsvector,
  created_at  timestamptz DEFAULT now(),
  content_tsv tsvector GENERATED ALWAYS AS (
    to_tsvector('simple', COALESCE(content, ''))
  ) STORED
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_rag_food_source_chunk ON rag_documents_food (source_id, chunk_id);
CREATE INDEX IF NOT EXISTS rag_food_content_trgm_gin ON rag_documents_food USING gin (content gin_trgm_ops);
CREATE INDEX IF NOT EXISTS rag_food_tsv_gin         ON rag_documents_food USING gin (content_tsv);
CREATE INDEX IF NOT EXISTS rag_food_embedding_hnsw  ON rag_documents_food USING hnsw (embedding vector_l2_ops);

-- 기능성화장품용 테이블
CREATE TABLE IF NOT EXISTS rag_documents_cosmetic (
  id          BIGSERIAL PRIMARY KEY,
  source_id   TEXT,
  chunk_id    INTEGER,
  category    TEXT DEFAULT 'functional_cosmetic',
  content     TEXT NOT NULL,
  metadata    JSONB DEFAULT '{}'::jsonb,
  embedding   vector(768),
  tsv         tsvector,
  created_at  timestamptz DEFAULT now(),
  content_tsv tsvector GENERATED ALWAYS AS (
    to_tsvector('simple', COALESCE(content, ''))
  ) STORED
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_rag_cosmetic_source_chunk ON rag_documents_cosmetic (source_id, chunk_id);
CREATE INDEX IF NOT EXISTS rag_cosmetic_content_trgm_gin ON rag_documents_cosmetic USING gin (content gin_trgm_ops);
CREATE INDEX IF NOT EXISTS rag_cosmetic_tsv_gin         ON rag_documents_cosmetic USING gin (content_tsv);
CREATE INDEX IF NOT EXISTS rag_cosmetic_embedding_hnsw  ON rag_documents_cosmetic USING hnsw (embedding vector_l2_ops);


