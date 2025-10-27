# ingest/normalize.py
import re
import unicodedata
from typing import Dict, Any, List

_ws_re = re.compile(r"\s+")
_commas_re = re.compile(r"\s*,\s*")

def normalize_text(s: str) -> str:
    if s is None:
        return ""
    # 유니코드 정규화
    s = unicodedata.normalize("NFKC", str(s))
    # 양끝/중복 공백 정리
    s = _ws_re.sub(" ", s).strip()
    # 불필요한 중복 기호 예: "~~", "!!" 등은 상황에 맞게 축약 가능
    return s

def _split_comma(v: str) -> List[str]:
    v = normalize_text(v)
    if not v:
        return []
    parts = _commas_re.split(v)
    # 빈 문자열 제거 + 중복 제거(순서 유지)
    seen, out = set(), []
    for p in parts:
        if p and p not in seen:
            seen.add(p)
            out.append(p)
    return out

# normalize.py

def normalize_metadata(md: Dict[str, Any]) -> Dict[str, Any]:
    out = {}
    for k, v in md.items():
        if isinstance(v, str):
            out[k] = normalize_text(v)
        else:
            out[k] = v

    # 기존: ingredient만 리스트화 했다면 ↓ 확장
    comma_list_targets = [
        "ingredient",        # 혹시 다른 소스에서 들어올 수도 있으니 유지
        "raw_materials",     # BPMF: 성분 복수
        "functionalities",   # BPMF: 효능 복수
        "company_list"       # MF: 업체 리스트(있으면 분리)
    ]
    for key in comma_list_targets:
        if key in out and isinstance(out[key], str):
            out[f"{key}_list"] = _split_comma(out[key])

    return out

