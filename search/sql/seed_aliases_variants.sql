-- ============================================================================
-- search/sql/seed_aliases_variants.sql
-- 안전한 파생형(표기 변형) 일괄 추가: 공백/하이픈/괄호/법인표기 등
-- 재실행 안전: ON CONFLICT DO NOTHING
-- ============================================================================

BEGIN;

-- =========================
-- INGREDIENT
-- =========================

-- 공백/하이픈 제거
INSERT INTO ingredient_aliases (canonical, alias)
SELECT c.canonical,
       regexp_replace(regexp_replace(c.canonical, '\s+', '', 'g'), '-', '', 'g') AS alias
FROM (SELECT DISTINCT canonical FROM ingredient_aliases) c
WHERE regexp_replace(regexp_replace(c.canonical, '\s+', '', 'g'), '-', '', 'g') <> c.canonical
ON CONFLICT (canonical, alias) DO NOTHING;

-- 괄호 내용 제거 버전 (예: "홍삼(국산)" -> "홍삼")
INSERT INTO ingredient_aliases (canonical, alias)
SELECT c.canonical,
       trim(regexp_replace(c.canonical, '\s*\([^)]*\)\s*', ' ', 'g')) AS alias
FROM (SELECT DISTINCT canonical FROM ingredient_aliases) c
WHERE c.canonical ~ '\('
  AND trim(regexp_replace(c.canonical, '\s*\([^)]*\)\s*', ' ', 'g')) <> c.canonical
ON CONFLICT (canonical, alias) DO NOTHING;

-- =========================
-- BRAND
-- =========================

-- 법인표기 제거 (주식회사/㈜/(주)/co., ltd/ltd/inc/corp 등)
INSERT INTO brand_aliases (canonical, alias)
WITH base AS (SELECT DISTINCT canonical FROM brand_aliases)
SELECT b.canonical,
       trim(regexp_replace(
         b.canonical,
         '(주식회사|㈜|\(주\)|\bco\.?\s*ltd\.?\b|\bltd\.?\b|\binc\.?\b|\bcorp\.?\b)',
         '',
         'gi'
       )) AS alias
FROM base b
WHERE trim(regexp_replace(
        b.canonical,
        '(주식회사|㈜|\(주\)|\bco\.?\s*ltd\.?\b|\bltd\.?\b|\binc\.?\b|\bcorp\.?\b)',
        '',
        'gi'
      )) <> b.canonical
ON CONFLICT (canonical, alias) DO NOTHING;

-- 공백 제거
INSERT INTO brand_aliases (canonical, alias)
SELECT c.canonical,
       regexp_replace(c.canonical, '\s+', '', 'g') AS alias
FROM (SELECT DISTINCT canonical FROM brand_aliases) c
WHERE regexp_replace(c.canonical, '\s+', '', 'g') <> c.canonical
ON CONFLICT (canonical, alias) DO NOTHING;

-- =========================
-- PRODUCT
-- =========================

-- 공백/하이픈 제거
INSERT INTO product_aliases (canonical, alias)
SELECT c.canonical,
       regexp_replace(regexp_replace(c.canonical, '\s+', '', 'g'), '-', '', 'g') AS alias
FROM (SELECT DISTINCT canonical FROM product_aliases) c
WHERE regexp_replace(regexp_replace(c.canonical, '\s+', '', 'g'), '-', '', 'g') <> c.canonical
ON CONFLICT (canonical, alias) DO NOTHING;

-- (선택) 용량/단위 제거 (뒤쪽 숫자+단위 토큰 간단 제거)
INSERT INTO product_aliases (canonical, alias)
SELECT c.canonical,
       trim(regexp_replace(c.canonical, '\b\d+\s*(ml|mg|g|정|포|개|캡슐)\b', '', 'gi')) AS alias
FROM (SELECT DISTINCT canonical FROM product_aliases) c
WHERE trim(regexp_replace(c.canonical, '\b\d+\s*(ml|mg|g|정|포|개|캡슐)\b', '', 'gi')) <> c.canonical
ON CONFLICT (canonical, alias) DO NOTHING;

-- =========================
-- PRODUCT
-- =========================

-- 공백 제거
INSERT INTO efficacy_aliases (canonical, alias)
SELECT c.canonical, regexp_replace(c.canonical, '\s+', '', 'g')
FROM (SELECT DISTINCT canonical FROM efficacy_aliases) c
WHERE regexp_replace(c.canonical, '\s+', '', 'g') <> c.canonical
ON CONFLICT DO NOTHING;

-- 하이픈/괄호 등 단순 정리(선택)
INSERT INTO efficacy_aliases (canonical, alias)
SELECT c.canonical,
       trim(regexp_replace(regexp_replace(c.canonical, '-', ' ', 'g'), '\s*\([^)]*\)\s*', ' ', 'g'))
FROM (SELECT DISTINCT canonical FROM efficacy_aliases) c
WHERE trim(regexp_replace(regexp_replace(c.canonical, '-', ' ', 'g'), '\s*\([^)]*\)\s*', ' ', 'g')) <> c.canonical
ON CONFLICT DO NOTHING;

COMMIT;
