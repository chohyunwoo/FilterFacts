# -*- coding: utf-8 -*-
import os
from dotenv import load_dotenv, find_dotenv

load_dotenv(find_dotenv())

def _envf(key: str, default: str = "") -> str:
    v = os.getenv(key, default)
    return v.strip() if isinstance(v, str) else v

def _envb(key: str, default: bool = False) -> bool:
    v = os.getenv(key, None)
    if v is None:
        return default
    return str(v).lower() in ("1", "true", "yes", "y")

def _enfi(key: str, default: int) -> int:
    try:
        return int(_envf(key, str(default)))
    except Exception:
        return default

def _envf_float(key: str, default: float) -> float:
    try:
        return float(_envf(key, str(default)))
    except Exception:
        return default

# 카테고리 → (schema, table) 매핑 (service.py에서 사용)
CATEGORY_TABLE = {
    "functional_food":     ("public", "rag_documents_food"),
    "functional_cosmetic": ("public", "rag_documents_cosmetic"),
}

# --- PG / Ollama Embeddings ---
PG = dict(
    host=_envf("PG_HOST", "127.0.0.1"),
    port=_enfi("PG_PORT", 5432),
    dbname=_envf("PG_DATABASE", "TestDb"),
    user=_envf("PG_USER", "postgres"),
    password=_envf("PG_PASSWORD", ""),
)

EMBED_EP    = _envf("EMBED_ENDPOINT", "http://ollama-service:11434")
EMBED_MODEL = _envf("EMBED_MODEL", "nomic-embed-text")
EMBED_DIM   = _enfi("EMBED_DIM", 768)

# Views (optional/legacy)
BPMF_VIEW = _envf("BPMF_VIEW", "brand_product_materials_functionalities")
MF_VIEW   = _envf("MF_VIEW", "material_functionality")

# --- Defaults ---
DEFAULT_CATEGORY = _envf("DEFAULT_CATEGORY", "functional_food")

# --- Hybrid Weights / Thresholds ---
SEARCH_ALPHA = _envf_float("SEARCH_ALPHA", 0.45)
SEARCH_BETA  = _envf_float("SEARCH_BETA",  0.40)
SEARCH_GAMMA = _envf_float("SEARCH_GAMMA", 0.10)
SEARCH_DELTA = _envf_float("SEARCH_DELTA", 0.20)
TRGM_MIN_SIM = _envf_float("TRGM_MIN_SIM", 0.28)

# --- Context / Selection Limits ---
EVIDENCE_MAX       = _enfi("EVIDENCE_MAX", 8)
MAX_PER_SOURCE     = _enfi("MAX_PER_SOURCE", 2)
MAX_CONTEXT_CHARS  = _enfi("MAX_CONTEXT_CHARS", 3800)
TRUNCATION_MODE    = _envf("TRUNCATION_MODE", "sentence_boundary")
MUST_BACKOFF_ORDER = [s.strip() for s in _envf("MUST_BACKOFF_ORDER", "both,either,none").split(",")]
CITATION_STYLE     = _envf("CITATION_STYLE", "[{n}]")

# ---- Prompt Budget (LLM에 넘길 전체 프롬프트 길이 상한; 컨트롤러에서 사용) ----
MAX_PROMPT_CHARS = _enfi("MAX_PROMPT_CHARS", 12000)

# --- LLM ---
LLM_MODEL       = _envf("LLM_MODEL", "qwen2.5:1.5b-instruct")
LLM_BASE_URL    = _envf("LLM_BASE_URL", "http://ollama-service:11434")
LLM_TEMPERATURE = _envf_float("LLM_TEMPERATURE", 0.2)
LLM_TOP_P       = _envf_float("LLM_TOP_P", 0.9)
LLM_MAX_TOKENS  = _enfi("LLM_MAX_TOKENS", 1024)   # ← Ollama options.num_predict로 매핑
LLM_TIMEOUT     = _enfi("LLM_TIMEOUT", 180)        # 초 단위
LLM_NUM_CTX     = _enfi("LLM_NUM_CTX", 2048)      # ← Ollama options.num_ctx로 매핑
LLM_MAX_RETRIES = _enfi("LLM_MAX_RETRIES", 2)     # 재시도 횟수

# --- Logging / Tracing ---
ENABLE_STRUCTURED_LOG = _envb("ENABLE_STRUCTURED_LOG", True)
LOG_SAMPLE_RATE       = float(_envf("LOG_SAMPLE_RATE", "1.0"))
LOG_LEVEL             = _envf("LOG_LEVEL", "INFO")
REDACT_PII            = _envb("REDACT_PII", True)

# Debug toggles (optional)
DEBUG_LLM   = _envb("DEBUG_LLM", True)
DEBUG_CTRL  = _envb("DEBUG_CTRL", True)
DEBUG_PRMPT = _envb("DEBUG_PRMPT", True)
