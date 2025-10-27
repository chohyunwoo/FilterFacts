# search/aliases.py
from functools import lru_cache
from typing import List, Dict, Iterable, Tuple
from .db import get_conn
from .normalize import normalize_text, collapse_spaces

def _derive_basic_aliases(term: str) -> List[str]:
    t = normalize_text(term)
    return list({t, t.replace(" ", ""), t.replace("-", "")})

def _fetch_alias_map(conn, table: str, canonicals: Iterable[str]) -> Dict[str, List[str]]:
    if not canonicals:
        return {}
    with conn.cursor() as cur:
        cur.execute(f"SELECT canonical, alias FROM {table} WHERE canonical = ANY(%s)", (list(canonicals),))
        rows = cur.fetchall()
    m: Dict[str, List[str]] = {}
    for c, a in rows:
        m.setdefault(c, []).append(a)
    return m

def _upsert_aliases(conn, table: str, pairs: Iterable[Tuple[str, str]]) -> None:
    pairs = list(pairs)
    if not pairs:
        return
    with conn.cursor() as cur:
        cur.executemany(
            f"""
            INSERT INTO {table} (canonical, alias)
            VALUES (%s, %s)
            ON CONFLICT (canonical, alias) DO NOTHING
            """,
            pairs
        )
    conn.commit()

@lru_cache(maxsize=2048)
def expand_ingredient_aliases(canonical: str) -> List[str]:
    c = normalize_text(canonical)
    table = "ingredient_aliases"
    with get_conn() as conn:
        found = _fetch_alias_map(conn, table, [c]).get(c, [])
        existing = set(found) | {c, collapse_spaces(c)}
        derived = set(_derive_basic_aliases(c))
        to_add = [(c, a) for a in (derived | {c}) if a not in existing]
        if to_add:
            _upsert_aliases(conn, table, to_add)
            existing |= {a for _, a in to_add}
        return sorted(existing)

@lru_cache(maxsize=2048)
def expand_brand_aliases(canonical: str) -> List[str]:
    c = normalize_text(canonical)
    table = "brand_aliases"
    with get_conn() as conn:
        found = _fetch_alias_map(conn, table, [c]).get(c, [])
        existing = set(found) | {c, collapse_spaces(c)}
        derived = set(_derive_basic_aliases(c))
        to_add = [(c, a) for a in (derived | {c}) if a not in existing]
        if to_add:
            _upsert_aliases(conn, table, to_add)
            existing |= {a for _, a in to_add}
        return sorted(existing)

@lru_cache(maxsize=2048)
def expand_product_aliases(canonical: str) -> List[str]:
    c = normalize_text(canonical)
    table = "product_aliases"
    with get_conn() as conn:
        found = _fetch_alias_map(conn, table, [c]).get(c, [])
        existing = set(found) | {c, collapse_spaces(c)}
        derived = set(_derive_basic_aliases(c))
        to_add = [(c, a) for a in (derived | {c}) if a not in existing]
        if to_add:
            _upsert_aliases(conn, table, to_add)
            existing |= {a for _, a in to_add}
        return sorted(existing)

def expand_aliases(term: str, kind: str = "ingredient") -> List[str]:
    if kind == "ingredient":
        return expand_ingredient_aliases(term)
    if kind == "brand":
        return expand_brand_aliases(term)
    if kind == "product":
        return expand_product_aliases(term)
    t = normalize_text(term)
    return list({t, collapse_spaces(t)})

def invalidate_alias_cache():
    expand_ingredient_aliases.cache_clear()
    expand_brand_aliases.cache_clear()
    expand_product_aliases.cache_clear()
