# search/llm_client.py
# -*- coding: utf-8 -*-
import time
import uuid
import requests
from typing import Tuple

from .config import (
    LLM_BASE_URL, LLM_MODEL, LLM_TIMEOUT,
    LLM_TEMPERATURE, LLM_TOP_P, LLM_MAX_TOKENS,
    DEBUG_LLM
)
from .errors import (
    LLMError, LLMModelNotFound, LLMTimeout, LLMConnectionError
)

# 모델이 답안을 끝낼 때 넣는 스탑 시퀀스
_STOP_TOKENS = ["[답변 종료]", "\n[답변 종료]"]

def call_llm(prompt: str) -> Tuple[str, float, str]:
    """
    Ollama /api/generate 호출.
    반환: (answer_text, latency_ms, model_name)
    실패 시 예외를 raise하여 상위(API)에서 5xx로 매핑.
    """
    t0 = time.time()
    req_id = str(uuid.uuid4())
    url = f"{LLM_BASE_URL.rstrip('/')}/api/generate"

    payload = {
        "model": LLM_MODEL,
        "prompt": prompt,
        "stream": False,
        "options": {
            # 디코딩/길이 관련
            "temperature": LLM_TEMPERATURE,
            "top_p": LLM_TOP_P,
            "num_predict": LLM_MAX_TOKENS,   # .env에서 512 권장
            "repeat_penalty": 1.05,
            # 조기 종료
            "stop": _STOP_TOKENS,
        },
    }

    try:
        r = requests.post(url, json=payload, timeout=LLM_TIMEOUT)  # .env에서 60 권장
    except requests.exceptions.ReadTimeout as e:
        raise LLMTimeout(f"LLM timeout: {e}") from e
    except requests.exceptions.ConnectionError as e:
        raise LLMConnectionError(f"LLM connection error: {e}") from e
    except requests.exceptions.RequestException as e:
        raise LLMError(f"LLM request error: {e}") from e

    latency_ms = (time.time() - t0) * 1000.0

    if r.status_code == 404:
        # 모델 미로딩/오타
        raise LLMModelNotFound(f"model {LLM_MODEL} not found (404)")

    try:
        r.raise_for_status()
    except requests.HTTPError as e:
        body = r.text[:200] if r.text else ""
        raise LLMError(f"LLM http {r.status_code}: {body}") from e

    data = r.json() or {}
    answer = (data.get("response") or "").strip()
    model_name = (data.get("model") or LLM_MODEL)

    if not answer:
        raise LLMError("Empty response from LLM")

    if DEBUG_LLM:
        print(f"[LLM] model={model_name} latency_ms={latency_ms:.1f} len={len(answer)} req_id={req_id}")

    return answer, latency_ms, model_name
