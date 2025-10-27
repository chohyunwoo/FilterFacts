-- ============================================================================
-- search/sql/seed_aliases.sql
-- 별칭 초기 시드 DML: rag_documents_food / rag_documents_cosmetic 의 metadata에서
-- canonical=alias 형태로 seed. (중복은 무시)
-- 실행 순서: ingest/sql/schema.sql → ingest 파이프라인 적재 완료 → 본 파일 실행
-- ============================================================================

BEGIN;

-- 문자열 정규화: lower + trim + 연속 공백 단일화
-- (함수 없이 inline 처리: lower(regexp_replace(trim(x), '\s+', ' ', 'g')) )

-- =========================
-- 1) INGREDIENT ALIASES
-- =========================

-- 1-1) FOOD: metadata.raw_materials_list 배열에서 안전 추출 (name/value/raw_material/string 혼합)
INSERT INTO ingredient_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(txt), '\s+', ' ', 'g')) AS canonical,
  lower(regexp_replace(trim(txt), '\s+', ' ', 'g')) AS alias
FROM rag_documents_food d
CROSS JOIN LATERAL (
  SELECT COALESCE(
           NULLIF(e->>'name',''),
           NULLIF(e->>'value',''),
           NULLIF(e->>'raw_material',''),
           CASE WHEN jsonb_typeof(e) = 'string' THEN trim(both '"' from e::text) END
         ) AS txt
  FROM jsonb_array_elements(COALESCE(d.metadata->'raw_materials_list','[]'::jsonb)) AS e
) t
WHERE t.txt IS NOT NULL AND t.txt <> ''
ON CONFLICT (canonical, alias) DO NOTHING;

-- 1-2) FOOD: metadata.raw_material (단일 문자열)
INSERT INTO ingredient_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(d.metadata->>'raw_material'), '\s+', ' ', 'g')) AS canonical,
  lower(regexp_replace(trim(d.metadata->>'raw_material'), '\s+', ' ', 'g')) AS alias
FROM rag_documents_food d
WHERE COALESCE(d.metadata->>'raw_material','') <> ''
ON CONFLICT (canonical, alias) DO NOTHING;

-- 1-3) COSMETIC: metadata.raw_materials_list 배열
INSERT INTO ingredient_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(txt), '\s+', ' ', 'g')) AS canonical,
  lower(regexp_replace(trim(txt), '\s+', ' ', 'g')) AS alias
FROM rag_documents_cosmetic d
CROSS JOIN LATERAL (
  SELECT COALESCE(
           NULLIF(e->>'name',''),
           NULLIF(e->>'value',''),
           NULLIF(e->>'raw_material',''),
           CASE WHEN jsonb_typeof(e) = 'string' THEN trim(both '"' from e::text) END
         ) AS txt
  FROM jsonb_array_elements(COALESCE(d.metadata->'raw_materials_list','[]'::jsonb)) AS e
) t
WHERE t.txt IS NOT NULL AND t.txt <> ''
ON CONFLICT (canonical, alias) DO NOTHING;

-- 1-4) COSMETIC: metadata.raw_material (단일 문자열)
INSERT INTO ingredient_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(d.metadata->>'raw_material'), '\s+', ' ', 'g')) AS canonical,
  lower(regexp_replace(trim(d.metadata->>'raw_material'), '\s+', ' ', 'g')) AS alias
FROM rag_documents_cosmetic d
WHERE COALESCE(d.metadata->>'raw_material','') <> ''
ON CONFLICT (canonical, alias) DO NOTHING;

-- =========================
-- 2) BRAND ALIASES
-- =========================

-- 2-1) FOOD: metadata.brand
INSERT INTO brand_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(d.metadata->>'ltd'), '\s+', ' ', 'g')) AS canonical,
  lower(regexp_replace(trim(d.metadata->>'ltd'), '\s+', ' ', 'g')) AS alias
FROM rag_documents_food d
WHERE COALESCE(d.metadata->>'ltd','') <> ''
ON CONFLICT (canonical, alias) DO NOTHING;

-- 2-2) COSMETIC: metadata.brand
INSERT INTO brand_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(d.metadata->>'ltd'), '\s+', ' ', 'g')) AS canonical,
  lower(regexp_replace(trim(d.metadata->>'ltd'), '\s+', ' ', 'g')) AS alias
FROM rag_documents_cosmetic d
WHERE COALESCE(d.metadata->>'ltd','') <> ''
ON CONFLICT (canonical, alias) DO NOTHING;


-- =========================
-- 3) PRODUCT ALIASES
-- =========================

-- 3-1) FOOD: metadata.product_name
INSERT INTO product_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(d.metadata->>'product'), '\s+', ' ', 'g')) AS canonical,
  lower(regexp_replace(trim(d.metadata->>'product'), '\s+', ' ', 'g')) AS alias
FROM rag_documents_food d
WHERE COALESCE(d.metadata->>'product','') <> ''
ON CONFLICT (canonical, alias) DO NOTHING;

-- 3-2) COSMETIC: metadata.product_name
INSERT INTO product_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(d.metadata->>'product'), '\s+', ' ', 'g')) AS canonical,
  lower(regexp_replace(trim(d.metadata->>'product'), '\s+', ' ', 'g')) AS alias
FROM rag_documents_cosmetic d
WHERE COALESCE(d.metadata->>'product','') <> ''
ON CONFLICT (canonical, alias) DO NOTHING;

-- =========================
-- 3) EFFICACY ALIASES
-- =========================


-- 4-1) FOOD: metadata.functionalities (단일 문자열)
INSERT INTO efficacy_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(d.metadata->>'functionalities'), '\s+', ' ', 'g')),
  lower(regexp_replace(trim(d.metadata->>'functionalities'), '\s+', ' ', 'g'))
FROM rag_documents_food d
WHERE COALESCE(d.metadata->>'functionalities','') <> ''
ON CONFLICT DO NOTHING;

-- 4-2) FOOD: metadata.functionalities_list (배열)
INSERT INTO efficacy_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(x), '\s+', ' ', 'g')),
  lower(regexp_replace(trim(x), '\s+', ' ', 'g'))
FROM rag_documents_food d
CROSS JOIN LATERAL (
  SELECT e FROM jsonb_array_elements_text(COALESCE(d.metadata->'functionalities_list','[]'::jsonb)) AS e
) t(x)
WHERE COALESCE(x,'') <> ''
ON CONFLICT DO NOTHING;

-- 4-3) COSMETIC: 동일
INSERT INTO efficacy_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(d.metadata->>'functionalities'), '\s+', ' ', 'g')),
  lower(regexp_replace(trim(d.metadata->>'functionalities'), '\s+', ' ', 'g'))
FROM rag_documents_cosmetic d
WHERE COALESCE(d.metadata->>'functionalities','') <> ''
ON CONFLICT DO NOTHING;

INSERT INTO efficacy_aliases (canonical, alias)
SELECT DISTINCT
  lower(regexp_replace(trim(x), '\s+', ' ', 'g')),
  lower(regexp_replace(trim(x), '\s+', ' ', 'g'))
FROM rag_documents_cosmetic d
CROSS JOIN LATERAL (
  SELECT e FROM jsonb_array_elements_text(COALESCE(d.metadata->'functionalities_list','[]'::jsonb)) AS e
) t(x)
WHERE COALESCE(x,'') <> ''
ON CONFLICT DO NOTHING;

COMMIT;
