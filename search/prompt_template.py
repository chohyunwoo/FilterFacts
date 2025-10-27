# -*- coding: utf-8 -*-
from typing import Dict, List
from .config import CITATION_STYLE, _envf, _enfi

MAX_PROMPT_CHARS = _enfi("MAX_PROMPT_CHARS", 8000)
_CITE_FMT = CITATION_STYLE  # 예: "[{n}]"

def _view_type_of(ev: Dict) -> str:
    """
    컨텍스트 빌더 스키마(meta) 기준으로 MF/BPMF 분류.
    - meta.view_type 또는 ev.kind가 있으면 우선 사용
    - 없으면 휴리스틱: product/functions 있으면 BPMF,
      product 없고 raw_material/ingredients만 있으면 MF,
      그 외는 BPMF 기본
    """
    meta = ev.get("meta") or {}
    vt = (meta.get("view_type") or ev.get("kind") or "").strip().upper()
    if vt in ("MF", "BPMF"):
        return vt

    product = (meta.get("product") or "").strip()
    funcs = meta.get("functions") or []
    raw_mat = (meta.get("raw_material") or "").strip() or " ".join(meta.get("ingredients") or [])
    if raw_mat and not product:
        return "MF"
    if product or funcs:
        return "BPMF"
    return "BPMF"

def _mk_context_summary(payload: Dict) -> str:
    """
    CONTEXT 요약: Raw Question + MF/BPMF 리스트 + 카운트
    컨텍스트 빌더의 payload 스키마(query_info/evidence/meta)에 맞춤.
    """
    qinfo = payload.get("query_info") or {}
    q = qinfo.get("raw_question") or payload.get("raw_question") or payload.get("question") or ""
    evidence: List[Dict] = payload.get("evidence") or []

    lines = [f"- Raw Question: {q}"]

    mf_ids: List[str] = []
    bpmf_ids: List[str] = []

    # 최대 8개만 요약 표기
    for i, ev in enumerate(evidence, 1):
        vt = _view_type_of(ev)
        title = (ev.get("title") or ev.get("id") or f"src_{i:03d}").strip()
        tag = f"{ev.get('id') or f'src_{i:03d}'}:{title}"
        if vt == "MF":
            mf_ids.append(tag)
        else:
            bpmf_ids.append(tag)

    if mf_ids:
        lines.append(f"- MF 근거: " + ", ".join(mf_ids[:8]))
    if bpmf_ids:
        lines.append(f"- BPMF 근거: " + ", ".join(bpmf_ids[:8]))
    lines.append(f"- Evidence count: {len(evidence)} (MF={len(mf_ids)}, BPMF={len(bpmf_ids)})")
    return "\n".join(lines)

def render_prompt(payload: Dict) -> str:
    """
    모델에게 주는 SYSTEM/CONTEXT/USER 프롬프트를 간결하게 구성.
    - evidence는 컨텍스트 빌더 출력(payload['evidence'])를 그대로 사용
    - 인용 표기 스타일은 config.CITATION_STYLE(예: "[{n}]")을 표현적으로 안내
    - '근거 현황' 섹션 문구 포함(클라이언트 전달용)
    """
    context_summary = _mk_context_summary(payload)

    system = f"""# SYSTEM
- 너는 식약처(MFDS) 고시/개별인정 등 공신력 자료 기반의 사실 검증 전문가다.
- 컨텍스트(evidence) **밖의 정보는 사용하지 마라**. 근거가 부족하면 **'⚠️ 근거 미확정'**으로만 답하라.
- 모든 주장에는 **반드시 인용 표기**를 붙여라. 인용 표기 형식: "{_CITE_FMT}" (evidence 순번 기준).
- **원료-기능(MF) 근거**와 **제품 라벨(BPMF) 근거**를 **명확히 구분**하라.
- 의료적·치료적 단정 표현 금지. '**~에 도움을 줄 수 있음**' 수준만 허용.
- 판단은 **✅/⚠️/❌** 중 하나만 사용하라.
"""

    context = f"""# CONTEXT (요약)
{context_summary}
"""

    # 출력 형식(간결 고정)
    user = """# USER
- 아래 CONTEXT를 근거로 **정해진 섹션**에 맞춰 결과를 간결하게 작성하라.
- 각 문장 또는 불릿 끝에 필요한 인용 표기 **{n}**를 붙여라.
- **원료-기능(MF)** 근거와 **제품(BPMF)** 근거를 분리해 작성하라.
- CONTEXT에 직접 연결된 근거가 없으면, 판단은 **⚠️ 근거 미확정**으로 하고,
  '근거 현황' 섹션에 다음 문구를 포함하라:
  - “본 판단은 식약처 고시·개별인정 자료를 정규화한 사내 DB에 근거합니다. 현 시점 해당 주장과 직접 연결되는 근거를 확인하지 못했습니다.”
- 출력 형식은 아래만 허용한다.

[답변 시작]
### 요약
- (핵심 한 줄)

### 판단
- (아이콘만) ✅ / ⚠️ / ❌

### 근거
**원료-기능(MF) 근거**
- (있으면 불릿으로 요약 및 인용)

**제품(BPMF) 근거**
- (있으면 불릿으로 요약 및 인용)

### 근거 현황
- (필요 시 한 줄: 사내 DB 기반·직접 근거 유무)

### 참고·주의
- (컨텍스트에 있다면 한 줄)
[답변 종료]
"""

    prompt = f"{system}\n\n{context}\n\n{user}"
    if len(prompt) > MAX_PROMPT_CHARS:
        prompt = prompt[:MAX_PROMPT_CHARS]
    return prompt
