# ollama.Dockerfile
FROM python:3.11-slim

WORKDIR /app

# requirements.txt만 먼저 복사 → pip install (캐시 활용)
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

# 필요한 ollama 관련 코드만 복사 (필요시 경로 수정)
COPY main.py /app/

# 환경변수 및 포트 설정
ENV PYTHONUNBUFFERED=1
EXPOSE 11434

# 앱 실행
CMD ["python", "main.py"]
