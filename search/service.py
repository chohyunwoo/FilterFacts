# search/service.py
from typing import Dict, List, Tuple
from psycopg2 import sql as psql
from .config import (
    CATEGORY_TABLE,
    SEARCH_ALPHA, SEARCH_BETA, SEARCH_GAMMA, SEARCH_DELTA,
    TRGM_MIN_SIM,
)
from .db import get_conn, embed_text
    # embed_text: Ollama 등 임베딩 엔진 호출
from .normalize import normalize_text, collapse_spaces


def table_for_category(category: str) -> Tuple[psql.Identifier, psql.Identifier]:
    if category not in CATEGORY_TABLE:
        raise ValueError("category must be 'functional_food' or 'functional_cosmetic'")
    schema, table = CATEGORY_TABLE[category]
    return psql.Identifier(schema), psql.Identifier(table)


def _dynamic_trgm_threshold(q: str, base: float = TRGM_MIN_SIM) -> float:
    """짧은 토큰은 임계값 상향, 긴 토큰은 하향 (대략적인 휴리스틱)."""
    L = max(1, len(q.replace(" ", "")))
    if L <= 4:
        return max(0.20, min(0.36, base + 0.06))   # 0.34~0.36 부근
    if L <= 7:
        return max(0.20, min(0.34, base + 0.02))   # 0.30~0.32 부근
    return max(0.18, min(0.32, base - 0.04))       # 0.24~0.28 부근


def _make_alias_patterns(terms: List[str]) -> List[str]:
    """
    ILIKE ANY용 패턴 배열 생성. 공백 있는/없는 버전 모두 포함.
    ex) '대두 배아 열수 추출물' -> '%대두 배아 열수 추출물%', '%대두배아열수추출물%'
    """
    patterns: List[str] = []
    for t in terms:
        nz = normalize_text(t)
        if not nz:
            continue
        coll = collapse_spaces(nz)
        patterns.append(f"%{nz}%")
        if coll != nz:
            patterns.append(f"%{coll}%")
    # 중복 제거(순서 유지)
    seen = set()
    return [p for p in patterns if not (p in seen or seen.add(p))]


def _safe_patterns(arr: List[str]) -> List[str]:
    """
    LIKE ANY 배열이 비었을 때, 과매칭 방지를 위한 안전 패턴.
    절대 매치되지 않을 문자열을 넣는다.
    """
    return arr if arr else ["%__never_match_token__%"]


def hybrid_search(category: str,
                  query_text: str,
                  must_ingredients: List[str] = None,
                  soft_efficacies: List[str] = None,
                  must_brands: List[str] = None,
                  must_products: List[str] = None,
                  k: int = 12) -> List[Dict]:
    """
    step1: MUST ingredient + SOFT efficacy
    step2: MUST ingredient only
    step3: no MUST (완화)
    """
    must_ingredients = must_ingredients or []
    soft_efficacies  = soft_efficacies  or []
    must_brands      = must_brands or []
    must_products    = must_products or []

    # 질의 정규화
    q_norm = normalize_text(query_text)
    q_coll = collapse_spaces(q_norm)

    # 별칭 패턴 (ingredient / brand / product)
    alias_patterns_ing   = _make_alias_patterns(must_ingredients)
    alias_patterns_brand = _make_alias_patterns(must_brands)
    alias_patterns_prod  = _make_alias_patterns(must_products)

    # 효능 질의(eff_q):
    #  - soft_efficacies가 있으면 OR 결합(" | ")
    #  - 없으면 질문 자체(q_norm)를 websearch 질의로 폴백
    if soft_efficacies:
        eff_terms = [normalize_text(e) for e in soft_efficacies if normalize_text(e)]
        eff_q = " | ".join(eff_terms) if eff_terms else ""
    else:
        eff_q = q_norm  # 폴백: 질문 자체를 효능 질의로 사용

    # 동적 trgm 임계값
    dyn_trgm = _dynamic_trgm_threshold(q_norm, TRGM_MIN_SIM)

    print("\n[hybrid] category               =", category)
    print("[hybrid] query_raw              =", query_text)
    print("[hybrid] query_norm/col         =", q_norm, "/", q_coll)
    print("[hybrid] must_ingredients       =", must_ingredients)
    print("[hybrid] alias_patterns_ing(n)  =", len(alias_patterns_ing))
    print("[hybrid] must_brands            =", must_brands)
    print("[hybrid] alias_patterns_brand(n)=", len(alias_patterns_brand))
    print("[hybrid] must_products          =", must_products)
    print("[hybrid] alias_patterns_prod(n) =", len(alias_patterns_prod))
    print("[hybrid] soft_efficacies        =", soft_efficacies)
    print("[hybrid] eff_q                  =", eff_q)
    print("[hybrid] weights α/β/γ/δ        =", SEARCH_ALPHA, SEARCH_BETA, SEARCH_GAMMA, SEARCH_DELTA)
    print("[hybrid] trgm_min_sim(base/dyn) =", TRGM_MIN_SIM, "/", dyn_trgm)

    q_vec = embed_text(q_norm)
    schema_id, table_id = table_for_category(category)

    # 백오프 단계
    steps = [
        (True,  True),   # step1: MUST ingredient + SOFT efficacy
        (True,  False),  # step2: MUST ingredient only
        (False, False),  # step3: relax all
    ]

    hits: List[Dict] = []
    with get_conn() as conn, conn.cursor() as cur:
        for sidx, (use_must_ing, use_soft_eff) in enumerate(steps, 1):
            params = {
                "qvec": q_vec,
                "qnorm": q_norm,
                "qcoll": q_coll,
                "trgm_min": dyn_trgm,
                "k": k * (1 + sidx),
                "alpha": SEARCH_ALPHA, "beta": SEARCH_BETA, "gamma": SEARCH_GAMMA, "delta": SEARCH_DELTA,
                # 배열 파라미터 (안전 패턴 적용)
                "alias_patterns_ing":   _safe_patterns(alias_patterns_ing),
                "alias_patterns_brand": _safe_patterns(alias_patterns_brand),
                "alias_patterns_prod":  _safe_patterns(alias_patterns_prod),
                "eff_q": eff_q or "",
            }

            # --- 필터 조각 ---
            ing_filter = psql.SQL("")
            eff_filter = psql.SQL("")

            if use_must_ing and must_ingredients:
                # raw_material / raw_materials_list (+ 보조 키 약간 확장) 에 대해 ILIKE ANY
                ing_filter = psql.SQL("""
                  AND (
                    (
                      metadata ? 'raw_material'
                      AND lower(metadata->>'raw_material') ILIKE ANY (%(alias_patterns_ing)s)
                    )
                    OR (
                      jsonb_typeof(COALESCE(metadata->'raw_materials_list','[]'::jsonb)) = 'array'
                      AND EXISTS (
                        SELECT 1
                        FROM jsonb_array_elements_text(COALESCE(metadata->'raw_materials_list','[]'::jsonb)) AS v(txt)
                        WHERE lower(v.txt) ILIKE ANY (%(alias_patterns_ing)s)
                      )
                    )
                    OR (
                      metadata ? 'raw_material_kr'
                      AND lower(metadata->>'raw_material_kr') ILIKE ANY (%(alias_patterns_ing)s)
                    )
                    OR (
                      jsonb_typeof(COALESCE(metadata->'ingredients_list','[]'::jsonb)) = 'array'
                      AND EXISTS (
                        SELECT 1
                        FROM jsonb_array_elements_text(COALESCE(metadata->'ingredients_list','[]'::jsonb)) AS v3(txt)
                        WHERE lower(v3.txt) ILIKE ANY (%(alias_patterns_ing)s)
                      )
                    )
                    OR (
                      metadata ? 'ingredient'
                      AND lower(metadata->>'ingredient') ILIKE ANY (%(alias_patterns_ing)s)
                    )
                    OR (
                      metadata ? 'ingredients'
                      AND lower(metadata->>'ingredients') ILIKE ANY (%(alias_patterns_ing)s)
                    )
                  )
                """)

            # (변경) soft_efficacies가 실제로 있을 때만 효능 필터 적용
            if use_soft_eff and soft_efficacies:
                # 효능 필터(소프트): metadata.functionalities / functionalities_list / content_tsv (+ 라벨 클레임 보조) OR 매칭
                eff_filter = psql.SQL("""
                  AND (
                    -- 1) 단일 문자열 키(functionalities / functionality / funtionalities(오타))
                    COALESCE(
                      to_tsvector(
                        'simple',
                        COALESCE(
                          metadata->>'functionalities',
                          metadata->>'functionality',
                          metadata->>'funtionalities',
                          ''
                        )
                      ),
                      to_tsvector('simple','')
                    ) @@ websearch_to_tsquery('simple', %(eff_q)s)

                    OR

                    -- 2) 배열 키(functionalities_list): 요소들을 문자열로 합쳐서 매칭
                    (
                      jsonb_typeof(COALESCE(metadata->'functionalities_list','null'::jsonb)) = 'array'
                      AND to_tsvector(
                            'simple',
                            COALESCE((
                              SELECT string_agg(lower(trim(x)), ' ')
                              FROM jsonb_array_elements_text(metadata->'functionalities_list') AS x
                            ), '')
                          ) @@ websearch_to_tsquery('simple', %(eff_q)s)
                    )

                    OR

                    -- 3) 본문(content_tsv)에도 효능 질의가 나오면 통과
                    content_tsv @@ websearch_to_tsquery('simple', %(eff_q)s)

                    OR

                    -- 4) (보조) 라벨/클레임 유사 키
                    to_tsvector(
                      'simple',
                      COALESCE(
                        metadata->>'claims',
                        metadata->>'label_claims',
                        ''
                      )
                    ) @@ websearch_to_tsquery('simple', %(eff_q)s)
                  )
                """)

            # --- 메인 쿼리 ---
            # vec(α) + content trigram(β) + aux trigram(γ) + meta boost(δ)
            sql_q = psql.SQL("""
              WITH q AS (
                SELECT %(qvec)s::vector AS qvec,
                       %(qnorm)s::text  AS qnorm,
                       %(qcoll)s::text  AS qcoll
              ), base AS (
                SELECT id, source_id, chunk_id, content, metadata,

                       -- α: 벡터 유사도
                       1 - (embedding <=> (SELECT qvec FROM q)) AS vec_score,

                       -- β: content trigram
                       similarity(lower(content), (SELECT qnorm FROM q)) AS beta_score,

                       -- γ: 공백 제거 비교까지의 trigram 보조
                       GREATEST(
                         similarity(lower(content), (SELECT qnorm FROM q)),
                         similarity(regexp_replace(lower(content), '\s','', 'g'), (SELECT qcoll FROM q))
                       ) AS gamma_score,

                       -- δ: 메타 부스트 (ingredient/brand/product 매치)
                       (
                         -- ingredient: raw_material / raw_materials_list (+확장 키)
                         (CASE WHEN metadata ? 'raw_material'
                               AND lower(metadata->>'raw_material') ILIKE ANY (%(alias_patterns_ing)s)
                               THEN 1 ELSE 0 END)
                         +
                         (CASE WHEN jsonb_typeof(COALESCE(metadata->'raw_materials_list','[]'::jsonb)) = 'array'
                               AND EXISTS (
                                 SELECT 1
                                 FROM jsonb_array_elements_text(COALESCE(metadata->'raw_materials_list','[]'::jsonb)) AS v(txt)
                                 WHERE lower(v.txt) ILIKE ANY (%(alias_patterns_ing)s)
                               )
                               THEN 1 ELSE 0 END)
                         +
                         (CASE WHEN metadata ? 'raw_material_kr'
                               AND lower(metadata->>'raw_material_kr') ILIKE ANY (%(alias_patterns_ing)s)
                               THEN 1 ELSE 0 END)
                         +
                         (CASE WHEN jsonb_typeof(COALESCE(metadata->'ingredients_list','[]'::jsonb)) = 'array'
                               AND EXISTS (
                                 SELECT 1
                                 FROM jsonb_array_elements_text(COALESCE(metadata->'ingredients_list','[]'::jsonb)) AS v3(txt)
                                 WHERE lower(v3.txt) ILIKE ANY (%(alias_patterns_ing)s)
                               )
                               THEN 1 ELSE 0 END)
                         +
                         (CASE WHEN metadata ? 'ingredient'
                               AND lower(metadata->>'ingredient') ILIKE ANY (%(alias_patterns_ing)s)
                               THEN 1 ELSE 0 END)
                         +
                         (CASE WHEN metadata ? 'ingredients'
                               AND lower(metadata->>'ingredients') ILIKE ANY (%(alias_patterns_ing)s)
                               THEN 1 ELSE 0 END)
                         +
                         -- brand/company
                         (CASE WHEN lower(COALESCE(metadata->>'ltd','')) ILIKE ANY (%(alias_patterns_brand)s) THEN 1 ELSE 0 END)
                         +
                         (CASE WHEN jsonb_typeof(COALESCE(metadata->'company_list','[]'::jsonb)) = 'array'
                               AND EXISTS (
                                 SELECT 1
                                 FROM jsonb_array_elements_text(COALESCE(metadata->'company_list','[]'::jsonb)) AS v2(txt)
                                 WHERE lower(v2.txt) ILIKE ANY (%(alias_patterns_brand)s)
                               )
                               THEN 1 ELSE 0 END)
                         +
                         (CASE WHEN lower(COALESCE(metadata->>'brand','')) ILIKE ANY (%(alias_patterns_brand)s) THEN 1 ELSE 0 END)
                         +
                         -- product
                         (CASE WHEN lower(COALESCE(metadata->>'product','')) ILIKE ANY (%(alias_patterns_prod)s) THEN 1 ELSE 0 END)
                         +
                         (CASE WHEN lower(COALESCE(metadata->>'product_name','')) ILIKE ANY (%(alias_patterns_prod)s) THEN 1 ELSE 0 END)
                       )::int AS meta_score

                FROM {schema}.{table}
                WHERE true
                {ing_filter}
                {eff_filter}
              )
              SELECT id, source_id, chunk_id, content, metadata,
                     %(alpha)s*vec_score + %(beta)s*beta_score + %(gamma)s*gamma_score + %(delta)s*(meta_score) AS score,
                     vec_score, beta_score, gamma_score, meta_score
              FROM base
              ORDER BY score DESC
              LIMIT %(k)s
            """).format(
                schema=schema_id,
                table=table_id,
                ing_filter=ing_filter,
                eff_filter=eff_filter,
            )

            cur.execute(sql_q, params)
            rows = cur.fetchall()

            print(f"[hybrid] step{sidx} hits =", len(rows))
            for i, r in enumerate(rows[:5]):
                sid, preview = r[1], (r[3] or "")[:100].replace("\n", " ")
                print(f"  - {i+1:02d} src={sid} | score={r[5]:.3f} (vec={r[6]:.3f}, β={r[7]:.3f}, γ={r[8]:.3f}, meta={r[9]}) | {preview}")

            for row in rows:
                hits.append(dict(
                    id=row[0], source_id=row[1], chunk_id=row[2], content=row[3], metadata=row[4],
                    score=float(row[5]), vec=float(row[6]), beta=float(row[7]), gamma=float(row[8]), meta=int(row[9]),
                    step=sidx
                ))

            # (변경) 조기 종료 기준을 전체 hits 누적 기준으로 완화
            if len(hits) >= k:
                break

    return hits[:k]
