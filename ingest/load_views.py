# ingest/load_views.py
import os
import pandas as pd
import psycopg2
from psycopg2.extras import RealDictCursor
from dotenv import load_dotenv, find_dotenv


load_dotenv()

def _connect():
    return psycopg2.connect(
     host=os.getenv("PG_HOST"),
     port=os.getenv("PG_PORT"),
     dbname=os.getenv("PG_DATABASE"),
     user=os.getenv("PG_USER"),
     password=os.getenv("PG_PASSWORD"),
)



def load_bpmf():
    view_name = os.getenv("BPMF_VIEW", "brand_product_materials_functionalities")
    csv_path = os.getenv("BPMF_CSV")
    try:
        with _connect() as conn, conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(f'SELECT * FROM "{view_name}"')  # 소문자/대문자 섞이면 따옴표가 안전
            rows = cur.fetchall()
        return pd.DataFrame(rows)
    except Exception:
        if csv_path and os.path.exists(csv_path):
            return pd.read_csv(csv_path)
        raise

def load_mf():
    view_name = os.getenv("MF_VIEW", "material_functionality")
    csv_path = os.getenv("MF_CSV")
    try:
        with _connect() as conn, conn.cursor(cursor_factory=RealDictCursor) as cur:
            cur.execute(f'SELECT * FROM "{view_name}"')
            rows = cur.fetchall()
        return pd.DataFrame(rows)
    except Exception:
        if csv_path and os.path.exists(csv_path):
            return pd.read_csv(csv_path)
        raise
