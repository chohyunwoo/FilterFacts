# search/errors.py
# -*- coding: utf-8 -*-

class LLMError(RuntimeError):
    """LLM 일반 오류(파싱/HTTP 등)."""
    pass

class LLMTimeout(LLMError):
    """LLM 응답 시간 초과."""
    pass

class LLMConnectionError(LLMError):
    """LLM 연결/네트워크 오류."""
    pass

class LLMModelNotFound(LLMError):
    """Ollama에 해당 모델이 로드되지 않았거나 이름 오타."""
    pass
