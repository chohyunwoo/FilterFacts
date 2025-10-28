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

_STOP_TOKENS = ["[답변 종료]", "\n[답변 종료]"]

def call_llm(prompt: str) -> Tuple[str, float, str]:
    """
    Ollama /api/generate 호출.
    스트리밍 NDJSON 대응 버전.
    """
    t0 = time.time()
    req_id = str(uuid.uuid4())
    url = f"{LLM_BASE_URL.rstrip('/')}/api/generate"

    payload = {
        "model": LLM_MODEL,
        "prompt": prompt,
        "stream": False,
        "options": {
            "temperature": LLM_TEMPERATURE,
            "top_p": LLM_TOP_P,
            "num_predict": LLM_MAX_TOKENS,
            "repeat_penalty": 1.05,
            "stop": _STOP_TOKENS,
        },
    }

    try:
        # 스트리밍 여부와 상관없이 chunk로 읽음
        with requests.post(url, json=payload, timeout=LLM_TIMEOUT, stream=True) as r:
            if r.status_code == 404:
                raise LLMModelNotFound(f"model {LLM_MODEL} not found (404)")
            r.raise_for_status()

            chunks = []
            for line in r.iter_lines():
                if not line:
                    continue
                try:
                    data = requests.utils.json.loads(line.decode("utf-8"))
                    resp_part = data.get("response")
                    if resp_part:
                        chunks.append(resp_part)
                    if data.get("done"):
                        break
                except Exception:
                    continue  # NDJSON 깨진 줄 무시

    except requests.exceptions.ReadTimeout as e:
        raise LLMTimeout(f"LLM timeout: {e}") from e
    except requests.exceptions.ConnectionError as e:
        raise LLMConnectionError(f"LLM connection error: {e}") from e
    except requests.exceptions.RequestException as e:
        raise LLMError(f"LLM request error: {e}") from e

    latency_ms = (time.time() - t0) * 1000.0
    answer = "".join(chunks).strip()
    model_name = LLM_MODEL

    if not answer:
        raise LLMError("Empty response from LLM")

    if DEBUG_LLM:
        print(f"[LLM] model={model_name} latency_ms={latency_ms:.1f} len={len(answer)} req_id={req_id}")

    return answer, latency_ms, model_name
