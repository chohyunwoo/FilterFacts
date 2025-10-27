from __future__ import annotations
from typing import List, Dict, Tuple, Any
import re
import math
import uuid
import time

from . import config


# ---------------------------
# Public API
# ---------------------------
def build_context(
    raw_question: str,
    parsed: Dict[str, Any],
    candidates: List[Dict[str, Any]],
    *,
    cfg=config,
) -> Tuple[Dict[str, Any], Dict[str, Any]]:
    started = time.time()
    trace_id = _make_trace_id()

    cat = (parsed or {}).get("category") or cfg.DEFAULT_CATEGORY
    cands = list(candidates or [])
    total_candidates = len(cands)

    step1 = _prefer_category(cands, cat)
    step2, must_mode = _apply_must_filter(step1, parsed, cfg)
    step3, dropped_map = _group_and_dedupe(step2, cfg)
    step4_sorted = _rank_by_score(step3)
    step4 = step4_sorted[: int(cfg.EVIDENCE_MAX)]
    step5, cut_info = _truncate_by_sentence(step4, int(cfg.MAX_CONTEXT_CHARS))

    evidences_with_ids = _assign_ids(step5)
    payload = _to_payload(
        raw_question=raw_question,
        parsed=parsed,
        evidence=evidences_with_ids,
        cfg=cfg,
        audit_info=dict(
            trace_id=trace_id,
            candidates_count=total_candidates,
            selected_count=len(evidences_with_ids),
            selection_strategy="category_first > MUST({}) > group_dedupe > rank > cap".format(
                must_mode
            ),
            truncation_mode=cfg.TRUNCATION_MODE,
            truncated_chars=cut_info.get("truncated_chars", 0),
        ),
    )

    selection_debug = dict(
        trace_id=trace_id,
        total_candidates=total_candidates,
        after_category=len(step1),
        must_mode=must_mode,
        after_must=len(step2),
        after_group_dedupe=len(step3),
        kept_ids=[e.get("source_id") for e in step3],
        dropped_reasons=dropped_map,
        top5_before=[_debug_topk_item(x) for x in step4_sorted[:5]],
        after_caps=len(step4),
        chars_total=cut_info.get("kept_chars", 0),
    )

    selection_debug["latency_ms"] = int((time.time() - started) * 1000)
    return payload, selection_debug


# ---------------------------
# Selection helpers
# ---------------------------
def _prefer_category(cands: List[Dict[str, Any]], category: str) -> List[Dict[str, Any]]:
    if not cands:
        return []

    def is_match(c):
        meta = c.get("meta") or {}
        src = c.get("source", "")
        meta_cat = (meta.get("category") or "").strip().lower()
        want = (category or "").strip().lower()
        if src.endswith("food") and want == "functional_food":
            return True
        if src.endswith("cosmetic") and want == "functional_cosmetic":
            return True
        return meta_cat == want

    matched = [c for c in cands if is_match(c)]
    return matched if matched else cands


def _apply_must_filter(cands: List[Dict[str, Any]], parsed: Dict[str, Any], cfg) -> Tuple[List[Dict[str, Any]], str]:
    if not cands:
        return [], "none"

    ingredients = set((parsed or {}).get("ingredients") or [])
    functions = set((parsed or {}).get("functions") or [])
    must_order = cfg.MUST_BACKOFF_ORDER or ["both", "either", "none"]

    def cand_has_terms(c) -> Tuple[bool, bool]:
        text = (c.get("text") or "").lower()
        meta = c.get("meta") or {}
        hay = " ".join(
            [
                text,
                " ".join(_as_list(meta.get("ingredients"))).lower(),
                " ".join(_as_list(meta.get("functions"))).lower(),
                (meta.get("brand") or "").lower(),
                (meta.get("product") or "").lower(),
            ]
        )
        # (변경①) 완화: 일부 단어만 포함해도 True
        ing_ok = any((_norm(t) in hay) for t in ingredients) if ingredients else False
        func_ok = any((_norm(t) in hay) for t in functions) if functions else False
        return ing_ok, func_ok

    for mode in must_order:
        if mode not in {"both", "either", "none"}:
            continue
        if mode == "none":
            return list(cands), "none"

        picked = []
        for c in cands:
            ing_ok, func_ok = cand_has_terms(c)
            if mode == "both" and ingredients and functions:
                if ing_ok and func_ok:
                    picked.append(c)
            elif mode == "either" and (ingredients or functions):
                if (ingredients and ing_ok) or (functions and func_ok):
                    picked.append(c)

        if picked:
            return picked, mode

    return list(cands), "none"


def _group_and_dedupe(cands: List[Dict[str, Any]], cfg) -> Tuple[List[Dict[str, Any]], Dict[str, str]]:
    """
    (변경②)
    - 같은 source_id + view_type 조합에서 최대 MAX_PER_SOURCE개만 유지
    - 텍스트 유사/중복 제거
    """
    if not cands:
        return [], {}

    max_per = int(cfg.MAX_PER_SOURCE)
    dropped: Dict[str, str] = {}
    kept: List[Dict[str, Any]] = []

    per_source_count = {}
    seen_hashes = set()

    for c in _rank_by_score(cands):
        src_id = str(c.get("source_id") or "")
        meta = c.get("meta") or {}
        view_type = "MF" if "raw_material" in (meta or {}) else "BPMF"
        key = f"{src_id}::{view_type}"

        per_source_count.setdefault(key, 0)
        if per_source_count[key] >= max_per:
            dropped[key] = "exceed_per_source"
            continue

        tnorm = _minify_text(c.get("text") or "")
        h = _fast_hash(tnorm + key)   # (변경) view_type 포함한 해시
        if h in seen_hashes:
            dropped[key or f"@{len(dropped)+1}"] = "duplicate_text"
            continue

        kept.append(c)
        seen_hashes.add(h)
        per_source_count[key] += 1

    return kept, dropped


def _rank_by_score(cands: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    return sorted(
        cands,
        key=lambda x: float(((x.get("scores") or {}).get("combined")) or 0.0),
        reverse=True,
    )


def _truncate_by_sentence(evidence: List[Dict[str, Any]], max_chars: int) -> Tuple[List[Dict[str, Any]], Dict[str, int]]:
    kept_chars = 0
    truncated_chars = 0
    out: List[Dict[str, Any]] = []

    for item in evidence:
        text = item.get("text") or item.get("chunk") or ""
        text = _soft_trim(text)
        if len(text) > max_chars:
            cut = _cut_to_boundary(text, max_chars)
            truncated_chars += len(text) - len(cut)
            text = cut

        remain = max_chars - kept_chars
        if remain <= 0:
            break

        if len(text) <= remain:
            new_text = text
        else:
            new_text = _cut_to_boundary(text, remain)
            truncated_chars += (len(text) - len(new_text))

        kept_chars += len(new_text)
        new_item = dict(item)
        new_item["text"] = new_text
        out.append(new_item)

    return out, {"kept_chars": kept_chars, "truncated_chars": truncated_chars}


def _assign_ids(evidence: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    out = []
    for i, e in enumerate(evidence, start=1):
        d = dict(e)
        d["id"] = f"src_{i:03d}"
        out.append(d)
    return out


# ---------------------------
# Payload builder
# ---------------------------
def _to_payload(raw_question: str, parsed: Dict[str, Any], evidence: List[Dict[str, Any]], cfg, audit_info: Dict[str, Any]) -> Dict[str, Any]:
    ev_items = []
    for e in evidence:
        meta = e.get("meta") or {}
        ev_items.append(
            dict(
                id=e.get("id"),
                source=e.get("source"),
                title=_compose_title(meta),
                chunk=e.get("text") or e.get("chunk") or "",
                meta=dict(
                    category=meta.get("category"),
                    brand=meta.get("brand"),
                    product=meta.get("product"),
                    ingredients=_as_list(meta.get("ingredients")),
                    functions=_as_list(meta.get("functions")),
                    approval_type=meta.get("approval_type") or "미확인",
                    approval_id=meta.get("approval_id"),
                    url=meta.get("url"),
                    updated_at=meta.get("updated_at"),
                ),
                scores=dict(e.get("scores") or {}),
                source_id=e.get("source_id"),
            )
        )

    verdict_hint = None
    if not ev_items:
        verdict_hint = dict(
            status="⚠️",
            # (변경③) 사용자 노출용 전문 멘트
            reasons=["현 데이터베이스(식약처·공신력 자료)에 관련 정보가 확인되지 않았습니다. 추가 검색어를 시도해 주세요."],
        )

    payload = dict(
        query_info=dict(raw_question=raw_question or "", parsed=parsed or {}),
        evidence=ev_items,
        verdict_hint=verdict_hint,
        limits=dict(max_chars=int(cfg.MAX_CONTEXT_CHARS), truncation=str(cfg.TRUNCATION_MODE)),
        audit=dict(
            trace_id=audit_info.get("trace_id"),
            candidates_count=int(audit_info.get("candidates_count", 0)),
            selected_count=int(audit_info.get("selected_count", 0)),
            selection_strategy=audit_info.get("selection_strategy"),
            truncation_mode=audit_info.get("truncation_mode"),
            truncated_chars=int(audit_info.get("truncated_chars", 0)),
        ),
    )
    return payload


# ---------------------------
# Utilities
# ---------------------------
_SENT_SPLIT_RE = re.compile(r"(?<=[.!?。！？])\s+|\n+")

def _cut_to_boundary(text: str, limit: int) -> str:
    if len(text) <= limit:
        return text
    snippet = text[:limit]
    parts = _SENT_SPLIT_RE.split(snippet)
    if not parts:
        return snippet
    out = ""
    consumed = 0
    for p in parts:
        if not p:
            continue
        if consumed + len(p) > limit:
            break
        if out:
            out += " "
        out += p
        consumed += len(p) + 1
    return out if out else snippet

def _soft_trim(s: str) -> str:
    s = (s or "").strip()
    s = re.sub(r"\s+", " ", s)
    return s

def _minify_text(s: str) -> str:
    s = _soft_trim(s).lower()
    s = re.sub(r"[^a-z0-9가-힣\s\.\,\:\;\-\(\)\[\]]", " ", s)
    s = re.sub(r"\s+", " ", s)
    return s.strip()

def _fast_hash(s: str) -> int:
    h = 1469598103934665603
    for ch in s.encode("utf-8", "ignore"):
        h ^= ch
        h *= 1099511628211
        h &= 0xFFFFFFFFFFFFFFFF
    return h

def _as_list(v: Any) -> List[str]:
    if v is None:
        return []
    if isinstance(v, (list, tuple, set)):
        return [str(x) for x in v]
    return [str(v)]

def _compose_title(meta: Dict[str, Any]) -> str:
    brand = (meta.get("brand") or "").strip()
    product = (meta.get("product") or "").strip()
    ing = ", ".join(_as_list(meta.get("ingredients")))
    func = ", ".join(_as_list(meta.get("functions")))
    parts = [x for x in [brand, product] if x]
    head = " ".join(parts) if parts else (ing or func or "").strip()
    return head or ""

def _norm(s: str) -> str:
    return (s or "").strip().lower()

def _debug_topk_item(c: Dict[str, Any]) -> Dict[str, Any]:
    sc = c.get("scores") or {}
    return dict(
        source_id=c.get("source_id"),
        combined=float(sc.get("combined") or 0.0),
        vector=float(sc.get("vector") or 0.0),
        fts=float(sc.get("fts") or 0.0),
        trgm=float(sc.get("trgm") or 0.0),
    )

def _make_trace_id() -> str:
    return uuid.uuid4().hex
