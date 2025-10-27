import psycopg2, requests
from psycopg2.extras import RealDictCursor
from .config import PG, EMBED_EP, EMBED_MODEL

def get_conn():
    return psycopg2.connect(
        host=PG["host"], port=PG["port"], dbname=PG["dbname"],
        user=PG["user"], password=PG["password"]
    )

def embed_text(text: str):
    r = requests.post(
        f"{EMBED_EP}/api/embeddings",
        json={"model": EMBED_MODEL, "prompt": text},
        timeout=30
    )
    r.raise_for_status()
    return r.json()["embedding"]
