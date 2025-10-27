# search/llm_client.py
# -*- coding: utf-8 -*-
import time
import uuid
import json
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
    NDJSON 스트리밍 응답(Ollama ≥0.12.4)도 자동 지원.
    """
    t0 = time.time()
    req_id = str(uuid.uuid4())
    url = f"{LLM_BASE_URL.rstrip('/')}/api/generate"

    payload = {
        "model": LLM_MODEL,
        "prompt": prompt,
        "stream": False,  # 한 번에 다 받기
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
        r = requests.post(url, json=payload, timeout=LLM_TIMEOUT)
    except requests.exceptions.ReadTimeout as e:
        raise LLMTimeout(f"LLM timeout: {e}") from e
    except requests.exceptions.ConnectionError as e:
        raise LLMConnectionError(f"LLM connection error: {e}") from e
    except requests.exceptions.RequestException as e:
        raise LLMError(f"LLM request error: {e}") from e

    latency_ms = (time.time() - t0) * 1000.0

    # 모델이 없거나 로딩 안 된 경우
    if r.status_code == 404:
        raise LLMModelNotFound(f"model {LLM_MODEL} not found (404)")

    # 일반 HTTP 에러 처리
    try:
        r.raise_for_status()
    except requests.HTTPError as e:
        body = r.text[:200] if r.text else ""
        raise LLMError(f"LLM http {r.status_code}: {body}") from e

    # -------------------------------
    # ✅ NDJSON 대응 부분 (핵심 수정)
    # -------------------------------
    answer_parts = []
    model_name = LLM_MODEL

    try:
        # Ollama ≥0.12.4: NDJSON 스트림 형식
        lines = [ln.strip() for ln in r.text.splitlines() if ln.strip()]
        for line in lines:
            try:
                obj = json.loads(line)
                if "response" in obj:
                    answer_parts.append(obj["response"])
                if "model" in obj:
                    model_name = obj["model"]
            except json.JSONDecodeError:
                # 중간에 깨진 JSON이 있어도 무시
                continue

        # Ollama ≤0.12.3: 단일 JSON 대응
        if not answer_parts and r.text.strip().startswith("{"):
            try:
                obj = r.json()
                if "response" in obj:
                    answer_parts.append(obj["response"])
                if "model" in obj:
                    model_name = obj["model"]
            except Exception:
                pass

    except Exception as e:
        raise LLMError(f"Failed to parse LLM response: {e}") from e

    answer = "".join(answer_parts).strip()

    if not answer:
        raise LLMError("Empty response from LLM")

    if DEBUG_LLM:
        print(f"[LLM] model={model_name} latency_ms={latency_ms:.1f} len={len(answer)} req_id={req_id}")

    return answer, latency_ms, model_name
