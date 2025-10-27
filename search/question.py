# search/question.py
from typing import Dict, List
from .db import get_conn
from .normalize import normalize_text, collapse_spaces

def _grep_alias_hits(conn, table: str, qnorm: str, qcoll: str, min_len: int = 2) -> List[str]:
    with conn.cursor() as cur:
        cur.execute(f"SELECT DISTINCT canonical, alias FROM {table} WHERE length(alias) >= %s", (min_len,))
        rows = cur.fetchall()
    hits = set()
    for canon, alias in rows:
        a = normalize_text(alias)
        if not a:
            continue
        if a in qnorm or collapse_spaces(a) in qcoll:
            hits.add(canon)  # canonical로 승격
    return list(hits)

def parse_query(text: str) -> Dict[str, List[str]]:
    qnorm = normalize_text(text)
    qcoll = collapse_spaces(qnorm)
    with get_conn() as conn:
        must_ingredients = _grep_alias_hits(conn, "ingredient_aliases", qnorm, qcoll, min_len=2)
        must_brands      = _grep_alias_hits(conn, "brand_aliases",      qnorm, qcoll, min_len=2)
        must_products    = _grep_alias_hits(conn, "product_aliases",    qnorm, qcoll, min_len=2)
        soft_efficacies  = _grep_alias_hits(conn, "efficacy_aliases",   qnorm, qcoll, min_len=2)
    return dict(
        must_ingredients=must_ingredients,
        must_brands=must_brands,
        must_products=must_products,
        soft_efficacies=soft_efficacies,
    )
