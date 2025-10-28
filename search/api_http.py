# api_http.py
# -*- coding: utf-8 -*-
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from search.controller import generate_answer
from search.errors import (
    LLMError, LLMTimeout, LLMConnectionError, LLMModelNotFound
)

app = FastAPI(title="aiTest API", version="1.0")

class AskReq(BaseModel):
    question: str
    category: str | None = None

class AskRes(BaseModel):
    result: str  # controller가 반환한 최종 문자열(헤더 + 본문)

@app.post("/ask", response_model=AskRes)
def ask(req: AskReq):
    try:
        out = generate_answer(
            question_text=req.question,
            category=req.category,
            k=12,
        )
        # 여기까지 왔으면 "업무로직 성공" → HTTP 200
        return AskRes(result=out)

# ====== 인프라/연결/모델 오류 → 5xx ======
except LLMTimeout as e:
    print("⏱ Timeout Error:", e)
    raise HTTPException(status_code=504, detail=f"Timeout: {str(e)[:200]}")

except LLMConnectionError as e:
    print("🔌 Connection Error:", e)
    raise HTTPException(status_code=502, detail=f"ConnectionError: {str(e)[:200]}")

except LLMModelNotFound as e:
    print("❌ Model Not Found Error:", e)
    raise HTTPException(status_code=500, detail=f"ModelNotFound: {str(e)[:200]}")

except LLMError as e:
    print("🤖 LLM Error:", e)
    raise HTTPException(status_code=500, detail=f"LLMError: {str(e)[:200]}")


except Exception as e:
    import traceback
    print("🔥 [UNEXPECTED ERROR] in /ask:", e)
    traceback.print_exc()
    raise HTTPException(
        status_code=500,
        detail=f"Unexpected: {type(e).__name__}: {str(e)[:200]}"
    )
