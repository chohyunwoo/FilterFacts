# normalize.py
import re

_RULES = [
    (r"\s+", " "),
    (r"[()\[\]{}]", " "),
    (r"[·・∙·･︰:;…]", " "),
    (r"[^\w가-힣\s]", " "),  # 숫자/영문/한글/공백 외 제거
]

_STOP_TERMS = {"등","복합물","함유","유지","제품","농축액","스틱","정","환"}

def normalize_text(s: str) -> str:
    s = (s or "").strip().lower()
    for pat, rep in _RULES:
        s = re.sub(pat, rep, s)
    toks = [t for t in s.split() if t and t not in _STOP_TERMS]
    return " ".join(toks)

def collapse_spaces(s: str) -> str:
    return re.sub(r"\s+", "", s or "")

def normalize_metadata(md: dict) -> dict:
    """
    뷰 row(dict)를 메타데이터 JSONB로 정규화.
    - 콤마로 연결된 성분/효능 → 리스트화
    - 불필요한 공백 제거
    """
    out = {}
    for k, v in (md or {}).items():
        if v is None:
            continue
        if isinstance(v, str):
            v = v.strip()
            if "," in v:  # 콤마로 구분된 값은 리스트로 변환
                arr = [normalize_text(x) for x in v.split(",") if x.strip()]
                out[k] = arr
                out[f"{k}_list"] = arr
            else:
                out[k] = v
        else:
            out[k] = v
    return out
