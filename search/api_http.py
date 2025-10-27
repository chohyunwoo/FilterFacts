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
    except LLMTimeout:
        # 내부 상세는 노출하지 않음
        raise HTTPException(status_code=504, detail="LLM gateway timeout")
    except LLMConnectionError:
        raise HTTPException(status_code=502, detail="LLM upstream unavailable")
    except LLMModelNotFound:
        raise HTTPException(status_code=500, detail="LLM model not available")
    except LLMError:
        raise HTTPException(status_code=500, detail="LLM internal error")

    # ====== 그 외 예기치 못한 오류 → 500 ======
    except Exception:
        raise HTTPException(status_code=500, detail="Internal server error")
