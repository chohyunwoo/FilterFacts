ai_server/
  ingest/
    run_ingest.py            # 엔트리(오케스트레이션) + build_content_* 포함
    load_views.py            # DB View(또는 CSV) → DataFrame 로드
    normalize.py             # 텍스트/메타데이터 정규화
    chunk.py                 # 문장 경계 기준 400~800자 청크 분할
    embed.py                 # Ollama 임베딩 호출 (nomic-embed-text 등)
    upsert.py                # rag_documents UPSERT + 인덱스 생성 유틸
    sql/
      schema.sql             # 테이블/인덱스 DDL
  search/
    config.py                  # .env 로딩 + 가중치/임계값
    db.py                      # PG 연결 + 임베딩 호출
    normalize.py               # 질의/문서 정규화 유틸
    aliases.py                 # 성분/별칭 사전 
    service.py                 # hybrid_search() 핵심 로직
    question.py                # 질문 파싱 parse_query() 핵심로직
    context_builder            # 컨텍스트 셀렉
    prompt_template            # 프롬포트 작성
    llm_client                 #
    controller                 #
    api_http                   #
    quick_hybrid_test_v5.py    # 모듈 호출형 테스트 스크립트 
  .env                       # DB, OLLAMA 등 환경 변수
  requirements.txt
  README.md
