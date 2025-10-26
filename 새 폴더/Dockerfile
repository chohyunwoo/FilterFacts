FROM postgres:17

# pgvector 설치에 필요한 패키지 설치
RUN apt-get update && \
    apt-get install -y postgresql-server-dev-17 build-essential git && \
    git clone https://github.com/pgvector/pgvector.git && \
    cd pgvector && \
    make && make install && \
    rm -rf pgvector && \
    apt-get clean && rm -rf /var/lib/apt/lists/*
