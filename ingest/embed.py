# ingest/embed.py
import os
import requests
from typing import List
from dotenv import load_dotenv

load_dotenv()

_OLLAMA = os.getenv("OLLAMA_BASE_URL", "http://127.0.0.1:11434")
_EMBED_MODEL = os.getenv("EMBEDDING_MODEL", "nomic-embed-text")

def embed_texts(texts: List[str]) -> List[List[float]]:
    url = f"{_OLLAMA}/api/embeddings"
    out = []
    for t in texts:
        resp = requests.post(url, json={"model": _EMBED_MODEL, "prompt": t}, timeout=60)
        resp.raise_for_status()
        data = resp.json()
        out.append(data["embedding"])
    return out
