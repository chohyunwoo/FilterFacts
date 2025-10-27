# -*- coding: utf-8 -*-
import re
import time
import uuid
from typing import Optional, List, Tuple

from .config import DEFAULT_CATEGORY, _envf
from .question import parse_query
from .service import hybrid_search
from .context_builder import build_context
from .prompt_template import render_prompt
from .llm_client import call_llm, LLM_MODEL

DEBUG_CTRL   = _envf("DEBUG_CTRL", "1") in ("1","true","TRUE","yes","YES")
DEBUG_PRMPT  = _envf("DEBUG_PRMPT","1") in ("1","true","TRUE","yes","YES")

_VERDICT_EMOJI_RE = re.compile(r"[✅⚠️❌]")

# --- 간단 정규화/동의어 유틸 ---
_ws_re = re.compile(r"\s+")
def _norm(s: str) -> str:
    s = s or ""
    s = s.lower()
    s = _ws_re.sub("", s)
    s = re.sub(r"[^\w가-힣]", "", s)
    return s

# 질문에서 주장 후보 추출(파서 보완)
def _claim_terms_from_question(q: str, parsed_soft: List[str]) -> List[str]:
    qn = _norm(q)
    terms = set(t for t in parsed_soft if t)

    # 휴리스틱 키워드
    if "눈" in q:
        terms.update(["눈 건강","시력","황반색소밀도 유지"])
    if "골다공증" in q:
        terms.update(["골다공증","골다공증 위험 감소","뼈 건강"])
    if "피로" in q:
        terms.add("피로개선")
    if "혈압" in q:
        terms.update(["혈압","혈압 개선","혈압 관리"])
    if "혈행" in q or "혈액흐름" in qn:
        terms.update(["혈행 개선","혈액 흐름 개선"])

    # 정규화 버전도 같이 보관
    out = set()
    for t in terms:
        if not t: 
            continue
        out.add(t)
        out.add(_norm(t))
    return list(out)

# evidence에서 기능 문구 모으기
def _functions_from_evidence(evidence: List[dict]) -> List[str]:
    funs = []
    for ev in evidence or []:
        m = ev.get("meta") or {}
        # meta.functions (리스트/문자열 모두 대응)
        fs = m.get("functions") or []
        if isinstance(fs, str):
            fs = [fs]
        for f in fs:
            if f:
                funs.append(str(f))
        # 텍스트에서 흔한 패턴 보강
        txt = (ev.get("text") or ev.get("content") or "").strip()
        for pat in ["눈 건강","시력","황반색소","골다공증","뼈","혈압","혈행","피로개선",
                    "기억력 개선","항산화","혈중 중성지질","면역력 증진"]:
            if pat in txt:
                funs.append(pat)
    # 정규화된 버전도 추가
    out = set()
    for f in funs:
        out.add(f)
        out.add(_norm(f))
    return list(out)

def _extract_verdict_symbol(answer_text: str) -> str:
    m_section = re.search(r"(?s)###\s*판단(.*?)(###|$)", answer_text)
    if m_section:
        m_icon = _VERDICT_EMOJI_RE.search(m_section.group(1))
        if m_icon:
            return m_icon.group(0)
    m_any = _VERDICT_EMOJI_RE.search(answer_text)
    if m_any:
        return m_any.group(0)
    return "⚠️"

def _client_safe_text(answer_text: str) -> str:
    # 내부 표현 정리: "데이터 한계" → "근거 현황"
    answer_text = answer_text.replace("데이터 한계", "근거 현황")
    # 시스템/오류류 문구 정제
    answer_text = answer_text.replace("시스템 오류", "내부 처리 중단")
    # 안내 문구 보강(표준 문장)
    answer_text = answer_text.replace(
        "현재 DB 기준, 식약처 저장 정보 없음",
        "현 시점 사내 DB(식약처 고시·개별인정 자료 정규화)에 등록된 직접 근거를 확인하지 못했습니다"
    )
    return answer_text

def _verdict_guard(question_text: str, payload: dict, llm_verdict: str) -> str:
    """
    BPMF만 있을 때도 질문 주장과 기능이 '직접·동의어 수준'으로 연결되면 ✅,
    연결이 없으면 ⚠️ 로 보정.
    """
    ev = payload.get("evidence", []) or []
    # 근거 유형 집계
    mf_cnt = sum(1 for e in ev if (e.get("kind") or "").upper() == "MF")
    bpmf_cnt = sum(1 for e in ev if (e.get("kind") or "").upper() == "BPMF")

    # 질문 주장 후보
    parsed = payload.get("parsed") or {}
    claim_terms = _claim_terms_from_question(question_text, parsed.get("functions", []))
    claim_norm = set(_norm(t) for t in claim_terms)

    # 근거 기능 후보
    funs = _functions_from_evidence(ev)
    fun_norm = set(_norm(f) for f in funs)

    def has_direct_match() -> bool:
        if not claim_norm or not fun_norm:
            return False
        # 부분 포함 허용
        for c in claim_norm:
            for f in fun_norm:
                if c and f and (c in f or f in c):
                    return True
        return False

    # 1) 반증 로직은 모델/프롬프트에 맡기고 여기선 미적용(데이터 부족)
    # 2) 매칭 보정
    if bpmf_cnt > 0 and has_direct_match():
        return "✅"
    if bpmf_cnt > 0 and not has_direct_match():
        return "⚠️"
    # MF만 있는데도 매치가 없으면 ⚠️
    if mf_cnt > 0 and not has_direct_match():
        return "⚠️"
    # 둘 다 없으면 ⚠️
    if mf_cnt == 0 and bpmf_cnt == 0:
        return "⚠️"
    # 기본값: 모델 판정 유지
    return llm_verdict or "⚠️"

def generate_answer(
    question_text: str,
    category: Optional[str] = None,
    k: int = 12,
) -> str:
    req_id = str(uuid.uuid4())
    category = category or DEFAULT_CATEGORY

    t0 = time.time()
    parsed = parse_query(question_text)

    items = hybrid_search(
        category=category,
        query_text=question_text,
        must_ingredients=parsed.get("must_ingredients", []),
        soft_efficacies=parsed.get("soft_efficacies", []),
        must_brands=parsed.get("must_brands", []),
        must_products=parsed.get("must_products", []),
        k=k,
    )

    parsed_for_builder = {
        "category": category,
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
                       if category == "functional_food"
                       else "rag_documents_cosmetic"),
            "meta": {
                "category": category,
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

    payload, _debug = build_context(
        question_text,
        parsed_for_builder,
        candidates,
    )

    # 프롬프트 생성
    prompt = render_prompt(payload)
    if DEBUG_PRMPT:
        print("\n[CTRL] ===== prompt (first 600 chars) =====")
        print(prompt[:600])
        print("[CTRL] ====================================\n")

    # LLM 호출
    llm_answer, llm_latency_ms, model_name = call_llm(prompt)

    # 모델 판정 추출
    model_symbol = _extract_verdict_symbol(llm_answer)

    # 컨트롤러 가드로 보정
    fixed_symbol = _verdict_guard(question_text, payload, model_symbol)

    # 헤더
    ev_count = (
        payload.get("evidence_count")
        or len(payload.get("evidence", []))
        or payload.get("summary", {}).get("evidence_count")
        or 0
    )
    total_latency_ms = (time.time() - t0) * 1000.0
    header = (
        f"VERDICT: {fixed_symbol} | MODEL: {model_name} | "
        f"EVIDENCE: {int(ev_count)} | REQ: {req_id} | "
        f"LATENCY_MS: {int(total_latency_ms)}"
    )
    if DEBUG_CTRL:
        print(f"[CTRL] header: {header}")

    # 클라이언트 안전 문구로 정제
    llm_answer = _client_safe_text(llm_answer)

    # 본문 내 잘못 표시된 verdict 아이콘이 있으면 보정(가장 첫 줄만 교체)
    llm_answer = re.sub(r"^VERDICT:\s*[✅⚠️❌]", f"VERDICT: {fixed_symbol}", llm_answer, count=1)

    return f"{header}\n{llm_answer}"
