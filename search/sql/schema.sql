-- ============================================================================
-- search/sql/schema.sql
-- 별칭(aliases) 테이블 전용 DDL: 테이블/인덱스/트리거
-- rag_documents_* 테이블 DDL은 ingest/sql/schema.sql 에서 관리
-- ============================================================================

-- 별칭 테이블 (ingredient / brand / product / efficacy)
CREATE TABLE IF NOT EXISTS ingredient_aliases (
  canonical  TEXT NOT NULL,
  alias      TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (canonical, alias)
);

CREATE TABLE IF NOT EXISTS brand_aliases (
  canonical  TEXT NOT NULL,
  alias      TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (canonical, alias)
);

CREATE TABLE IF NOT EXISTS product_aliases (
  canonical  TEXT NOT NULL,
  alias      TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (canonical, alias)
);

CREATE TABLE IF NOT EXISTS efficacy_aliases (
  canonical  TEXT NOT NULL,
  alias      TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now(),
  PRIMARY KEY (canonical, alias)
);



-- 조회 성능 인덱스 (alias, canonical)
CREATE INDEX IF NOT EXISTS idx_ingredient_aliases_alias     ON ingredient_aliases (alias);
CREATE INDEX IF NOT EXISTS idx_brand_aliases_alias          ON brand_aliases (alias);
CREATE INDEX IF NOT EXISTS idx_product_aliases_alias        ON product_aliases (alias);
CREATE INDEX IF NOT EXISTS idx_efficacy_aliases_alias     ON efficacy_aliases (alias);

CREATE INDEX IF NOT EXISTS idx_ingredient_aliases_canonical ON ingredient_aliases (canonical);
CREATE INDEX IF NOT EXISTS idx_brand_aliases_canonical      ON brand_aliases (canonical);
CREATE INDEX IF NOT EXISTS idx_product_aliases_canonical    ON product_aliases (canonical);
CREATE INDEX IF NOT EXISTS idx_efficacy_aliases_canonical ON efficacy_aliases (canonical);


-- updated_at 자동 갱신 트리거
CREATE OR REPLACE FUNCTION touch_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_touch_updated_at_ing ON ingredient_aliases;
CREATE TRIGGER trg_touch_updated_at_ing
BEFORE UPDATE ON ingredient_aliases
FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

DROP TRIGGER IF EXISTS trg_touch_updated_at_brand ON brand_aliases;
CREATE TRIGGER trg_touch_updated_at_brand
BEFORE UPDATE ON brand_aliases
FOR EACH ROW EXECUTE FUNCTION touch_updated_at();

DROP TRIGGER IF EXISTS trg_touch_updated_at_prod ON product_aliases;
CREATE TRIGGER trg_touch_updated_at_prod
BEFORE UPDATE ON product_aliases
FOR EACH ROW EXECUTE FUNCTION touch_updated_at();


DROP TRIGGER IF EXISTS trg_touch_updated_at_eff ON efficacy_aliases;
CREATE TRIGGER trg_touch_updated_at_eff
BEFORE UPDATE ON efficacy_aliases
FOR EACH ROW EXECUTE FUNCTION touch_updated_at_eff();
