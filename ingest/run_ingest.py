# ingest/run_ingest.py
import os
import argparse
from dotenv import load_dotenv
from tqdm import tqdm

from load_views import load_bpmf, load_mf
from normalize import normalize_text, normalize_metadata
from text_chunker import chunk_text
from embed import embed_texts
from upsert import hash_source_id, upsert_chunks

load_dotenv()

def g(row, *keys):
    for k in keys:
        v = row.get(k)
        if v not in (None, ""):
            return v
    return ""

def build_content_bpmf(row) -> str:
    # brand / product / ingredient / functionality 매핑
    brand      = normalize_text(g(row, "ltd"))                 # 업체명
    product    = normalize_text(g(row, "product"))             # 제품명
    ingredient = normalize_text(g(row, "raw_materials"))       # 성분(복수 가능, 콤마 구분)
    func       = normalize_text(g(row, "functionalities"))     # 효능(복수 가능, 콤마 구분)
    return f"{brand}\n{product}\n{ingredient}\n{func}".strip()

def build_content_mf(row) -> str:
    ingredient = normalize_text(g(row, "raw_material"))        # 성분(단수/복수 가능)
    guide_func = normalize_text(g(row, "functionality"))       # 효능/표시가이드
    return f"{ingredient}\n{guide_func}".strip()


def process_view(df, view_type: str, key_fields: list,
                 chunk_min: int, chunk_max: int):
    """
    key_fields: source_id를 만들 때 사용할 필드 리스트 (예: ['brand','product','ingredient','functionality'])
    """
    total_upserts = 0

    for _, row in tqdm(df.iterrows(), total=len(df), desc=f"{view_type} rows"):
        # 1) content
        if view_type == "BPMF":
            content = build_content_bpmf(row)
        else:
            content = build_content_mf(row)

        # 2) normalize content / metadata
        content = normalize_text(content)
        raw_md = {k: row.get(k) for k in row.keys()}
        md = normalize_metadata(raw_md)

        # 3) source_id
        keymap = {k: normalize_text(row.get(k, "")) for k in key_fields}
        sid = hash_source_id(view_type, keymap)
        md["source_id"] = sid
        md["view_type"] = view_type

        # 4) chunk
        chunks = chunk_text(content, min_size=chunk_min, max_size=chunk_max)
        if not chunks:
            continue

        # 5) embed
        vectors = embed_texts(chunks)

        # 6) upsert
        up_rows = []
        for i, (c, v) in enumerate(zip(chunks, vectors)):
            up_rows.append( (view_type, i, c, md, v) )
        total_upserts += upsert_chunks(up_rows)

    return total_upserts

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--only", choices=["BPMF","MF","ALL"], default="ALL")
    args = parser.parse_args()

    chunk_min = int(os.getenv("CHUNK_MIN", "400"))
    chunk_max = int(os.getenv("CHUNK_MAX", "800"))

    total = 0
    if args.only in ("BPMF","ALL"):
        df_bpmf = load_bpmf()
        # 키 결정: 브랜드/제품/성분/효능 (실제 view 컬럼명에 맞춰 조정)
        total += process_view(df_bpmf, "BPMF",
                              key_fields=["ltd", "product", "raw_materials", "functionalities"],
                              chunk_min=chunk_min, chunk_max=chunk_max)

    if args.only in ("MF","ALL"):
        df_mf = load_mf()
        total += process_view(df_mf, "MF",
                              key_fields=["raw_material", "functionality", "company_list"],
                              chunk_min=chunk_min, chunk_max=chunk_max)

    print(f"Ingest upserts: {total}")

if __name__ == "__main__":
    main()
