# ingest/chunk.py
import re
from typing import List

# 한국어/영어 문장단위 split (간단 버전)
_SENT_SPLIT = re.compile(r"(?<=[\.!\?。！？])\s+|\n+")

def chunk_text(text: str, min_size: int = 400, max_size: int = 800) -> List[str]:
    sentences = [s.strip() for s in _SENT_SPLIT.split(text) if s.strip()]
    chunks, buf = [], ""
    for s in sentences:
        # 새 문장 추가했을 때 길이 기준 확인
        if not buf:
            buf = s
        elif len(buf) + 1 + len(s) <= max_size:
            buf = f"{buf} {s}"
        else:
            # 현재 버퍼가 충분히 크지 않다면(짧으면) 약간 오버되더라도 붙여서 내보냄
            if len(buf) < min_size:
                buf = f"{buf} {s}"
            chunks.append(buf.strip())
            buf = ""
    if buf:
        chunks.append(buf.strip())

    # 극단적으로 긴 문장은 max_size 기반으로 하드 컷
    out = []
    for c in chunks:
        if len(c) <= max_size:
            out.append(c)
        else:
            for i in range(0, len(c), max_size):
                out.append(c[i:i+max_size].strip())
    return out
