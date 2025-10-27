# -*- coding: utf-8 -*-
import time
import uuid
import requests
import json
from typing import Tuple

from .config import (
    LLM_BASE_URL, LLM_MODEL, LLM_TIMEOUT,
    LLM_TEMPERATURE, LLM_TOP_P, LLM_MAX_TOKENS,
    DEBUG_LLM
)
from .errors import (
    LLMError, LLMModelNotFound, LLMTimeout, LLMConnectionError
)

_STOP_TOKENS = ["[답변 종료]", "\n[답변 종료]"]

def call_llm(prompt: str) -> Tuple[str, float, str]:
    t0 = time.time()
    req_id = str(uuid.uuid4())
    url = f"{LLM_BASE_URL.rstrip('/')}/api/generate"

    payload = {
        "model": LLM_MODEL,
        "prompt": prompt,
        "stream": False,  # ✅ 스트리밍 비활성화
        "options": {
            "temperature": LLM_TEMPERATURE,
            "top_p": LLM_TOP_P,
            "num_predict": LLM_MAX_TOKENS,
            "repeat_penalty": 1.05,
            "stop": _STOP_TOKENS,
        },
    }

    try:
        r = requests.post(url, json=payload, timeout=LLM_TIMEOUT)
    except requests.exceptions.ReadTimeout as e:
        raise LLMTimeout(f"LLM timeout: {e}") from e
    except requests.exceptions.ConnectionError as e:
        raise LLMConnectionError(f"LLM connection error: {e}") from e
    except requests.exceptions.RequestException as e:
        raise LLMError(f"LLM request error: {e}") from e

    latency_ms = (time.time() - t0) * 1000.0

    if r.status_code == 404:
        raise LLMModelNotFound(f"model {LLM_MODEL} not found (404)")

    try:
        r.raise_for_status()
    except requests.HTTPError as e:
        body = r.text[:200] if r.text else ""
        raise LLMError(f"LLM http {r.status_code}: {body}") from e

    # ✅ JSON 안전 파싱 (NDJSON 대응)
    try:
        data = r.json()
    except Exception:
        text = r.text.strip()
        if text.startswith("{") and text.endswith("}"):
            data = json.loads(text)
        else:
            lines = [ln for ln in text.splitlines() if ln.strip().startswith("{")]
            if lines:
                data = json.loads(lines[-1])
            else:
                raise LLMError(f"Invalid JSON response: {text[:200]}")

    answer = (data.get("response") or "").strip()
    model_name = (data.get("model") or LLM_MODEL)

    # ✅ 빈 응답 처리 강화
    if not answer:
        if data.get("done") is True:
            answer = "(no text returned from LLM)"
        else:
            raise LLMError(f"Empty or malformed response from LLM: {data}")

    if DEBUG_LLM:
        print(f"[LLM] model={model_name} latency_ms={latency_ms:.1f} len={len(answer)} req_id={req_id}")

    return answer, latency_ms, model_name
