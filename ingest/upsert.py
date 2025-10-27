# ingest/upsert.py
import os
import hashlib
import json
import psycopg2
from typing import Dict, List, Tuple
from psycopg2.extras import execute_values
from dotenv import load_dotenv

load_dotenv()

def _connect():
    return psycopg2.connect(
        host=os.getenv("PG_HOST"),
        port=os.getenv("PG_PORT"),
        dbname=os.getenv("PG_DATABASE"),
        user=os.getenv("PG_USER"),
        password=os.getenv("PG_PASSWORD"),
    )

def hash_source_id(category: str, key_fields: Dict[str, str]) -> str:
    """뷰 로우를 대표하는 필드들의 정규화 텍스트를 이어붙여 SHA256"""
    joined = "|".join(f"{k}={key_fields.get(k,'')}" for k in sorted(key_fields.keys()))
    return hashlib.sha256(f"{category}|{joined}".encode("utf-8")).hexdigest()

def upsert_chunks(rows: List[Tuple[str, int, str, Dict, List[float]]]):
    """
    rows: [(category, chunk_id, content, metadata, embedding), ...]
          단, source_id는 metadata['source_id']에 포함되어 있다고 가정
    """
    if not rows:
        return 0
    with _connect() as conn, conn.cursor() as cur:
        # INSERT ... ON CONFLICT
        sql = """
        INSERT INTO rag_documents_food
            (source_id, chunk_id, category, content, metadata, embedding)
        VALUES %s
        ON CONFLICT (source_id, chunk_id) DO UPDATE SET
            category = EXCLUDED.category,
            content  = EXCLUDED.content,
            metadata = EXCLUDED.metadata,
            embedding= EXCLUDED.embedding;
        """
        values = [
            (
                md["source_id"],       # source_id
                chunk_id,              # chunk_id
                vtype,                 # category (BPMF or MF)
                content,               # content
                json.dumps(md, ensure_ascii=False),  # metadata as JSON
                embedding              # vector
            )
            for (vtype, chunk_id, content, md, embedding) in rows
        ]
        execute_values(cur, sql, values)
    return len(rows)
