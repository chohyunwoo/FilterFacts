# search/quick_hybrid_test_v5.py
from typing import List

from search.config import (
    DEFAULT_CATEGORY,
    EVIDENCE_MAX,
    MAX_PER_SOURCE,
    MAX_CONTEXT_CHARS,
)
from search.question import parse_query
from search.service import hybrid_search
from search.context_builder import build_context
from search.prompt_template import render_prompt
from search.controller import generate_answer # LLM 호출

QS: List[str] = [
    "임신 준비 중인데 홍삼이 피로개선에는 좋지만 혈압엔 괜찮아?",
    "서흥에서 만든 눈에좋은 루테인오메가3라는게 눈에 도움이 돼?",
    "내가 다이어트 중인데 대두배아열수추출물이 도움이 될까?",
    "한풍네이처팜에서 만든 뼈건강이라는 제품은 골다공증에 도움이 되나?",
]

def run():
    for q in QS:
        print("\n" + "=" * 30)
        print("[TEST] Q:", q)

        # 1) 질문 파싱
        parsed = parse_query(q)
        print("[PARSED]", parsed)

        # 2) 하이브리드 검색
        items = hybrid_search(
            category=DEFAULT_CATEGORY,
            query_text=q,
            must_ingredients=parsed.get("must_ingredients", []),
            soft_efficacies=parsed.get("soft_efficacies", []),
            must_brands=parsed.get("must_brands", []),
            must_products=parsed.get("must_products", []),
            k=12,
        )

        # 2-1) context_builder 시그니처에 맞게 변환
        parsed_for_builder = {
            "category": DEFAULT_CATEGORY,
            "brand": (parsed.get("must_brands") or [None])[0],
            "product": (parsed.get("must_products") or [None])[0],
            "ingredients": parsed.get("must_ingredients", []),
            "functions": parsed.get("soft_efficacies", []),
            "must_terms": (parsed.get("must_ingredients", [])
                           + parsed.get("soft_efficacies", [])),
        }

        candidates = []
        for it in items:
            meta = it.get("metadata") or {}
            text = (it.get("chunk") or it.get("content") or it.get("text") or "")
            candidates.append({
                "text": text,
                "scores": {
                    "combined": float(it.get("score", 0.0)),
                    "vector": float(it.get("vec", 0.0)),
                    "fts": float(it.get("beta", 0.0)),
                    "trgm": float(it.get("gamma", 0.0)),
                },
                "source_id": it.get("source_id", ""),
                "source": ("rag_documents_food"
                           if DEFAULT_CATEGORY == "functional_food"
                           else "rag_documents_cosmetic"),
                "meta": {
                    "category": DEFAULT_CATEGORY,
                    "brand": (meta.get("brand") or meta.get("ltd") or meta.get("company")),
                    "product": (meta.get("product") or meta.get("product_name")),
                    "ingredients": ([meta.get("raw_material")] if meta.get("raw_material") else []),
                    "functions": (meta.get("functionalities") or meta.get("function") or []),
                    "approval_type": (meta.get("approval_type") or meta.get("approval")),
                    "approval_id": meta.get("approval_id"),
                    "url": meta.get("url"),
                    "updated_at": meta.get("updated_at"),
                },
            })

        # 2-2) 컨텍스트 빌드 (구시그니처: raw_question, parsed_for_builder, candidates)
        out = build_context(q, parsed_for_builder, candidates)
        if isinstance(out, tuple) and len(out) == 2:
            payload, debug = out
        else:
            payload, debug = out, {}

        # 2-3) 프롬프트 프리뷰
        prompt = render_prompt(payload)
        print("\n[PRMPT] preview:\n", prompt[:800], "...\n")

        # 2-4) 증거/제한 요약
        evidence = payload.get("evidence", [])
        print(
            f"[CTX] evidence={len(evidence)} "
            f"(max={EVIDENCE_MAX}, per_source={MAX_PER_SOURCE}, chars<= {MAX_CONTEXT_CHARS})"
        )
        if not evidence:
            print("  -> ⚠️ evidence가 0개입니다. verdict_hint:", payload.get("verdict_hint"))
        else:
            for i, ev in enumerate(evidence[:3], 1):
                m = (ev.get("meta") or {})
                print(
                    f"  [{i}] {ev.get('id')} src={ev.get('source_id')} | "
                    f"title={ev.get('title')!r} | brand={m.get('brand')} | product={m.get('product')}"
                )

        print(
            "[DBG] after_group_dedupe=",
            (debug or {}).get("after_group_dedupe"),
            "after_caps=",
            (debug or {}).get("after_caps"),
            "chars_total=",
            (debug or {}).get("chars_total"),
        )

        # 3) 실제 LLM 호출 (질문 텍스트만 전달)
        answer_text = generate_answer(
            question_text=q,
            category=DEFAULT_CATEGORY,
            k=12,
        )
        print("\n[ANSWER]")
        print(answer_text)

        # 4) 검색 결과 요약
        print("[RESULT] top =", len(items))
        for i, it in enumerate(items[:5], 1):
            meta = it.get("metadata") or {}
            ltd = meta.get("ltd") or meta.get("brand") or meta.get("company")
            prod = meta.get("product") or meta.get("product_name")
            rawm = meta.get("raw_material")
            print(
                f"  {i:02d}. score={it['score']:.3f} "
                f"(v={it['vec']:.3f}, β={it['beta']:.3f}, γ={it['gamma']:.3f}, m={it['meta']}) "
                f"src={it['source_id']} | "
                f"ltd={ltd} | product={prod} | raw_material={rawm}"
            )

if __name__ == "__main__":
    run()
