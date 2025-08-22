## 🗄️ DB 초기 데이터 세팅 가이드

이 프로젝트는 로컬 개발 환경에서 PostgreSQL DB를 사용합니다.  
각자 로컬 환경에서 PostgreSQL을 설치하고, 아래 절차를 따라 초기 데이터를 복원하세요.

---

### 1. PostgreSQL 설치
1. [PostgreSQL 공식 다운로드](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)에서 **Windows x86-64** 버전 설치
2. 설치 시 **Command Line Tools** 체크
3. 설치 후 `C:\Program Files\PostgreSQL\<버전>\bin` 경로를 **Windows 환경 변수 Path**에 추가
   - 시작 메뉴 → "시스템 환경 변수 편집"
   - **환경 변수(N)...** → **Path** 선택 → **편집**
   - 새 항목으로 `C:\Program Files\PostgreSQL\<버전>\bin` 추가
4. 터미널 재시작 후 확인:
   ```bash
   pg_dump --version
   psql --version

### 2. 데이터베이스 생성
  createdb -h 127.0.0.1 -p 5432 -U postgres TestDb
postgres 계정 비밀번호는 각자 로컬환경에서 설정한 값을 사용

### 3. 초기 데이터 복원
(선택) 비밀번호를 환경변수에 저장
export PGPASSWORD='본인_PostgreSQL_비밀번호'

# 복원 실행
psql -h 127.0.0.1 -p 5432 -U postgres -d TestDb -f db/seed/initial.sql

(선택) 환경변수 제거
unset PGPASSWORD

### 복원 확인
psql -h 127.0.0.1 -p 5432 -U postgres -d TestDb -c "\dt"
  - 테이블 목록이 정상적으로 보이면 성공


## 로컬에 브랜치 생성 후 원격 브랜치와 연결하는 방법
 - git checkout -b (브랜치명) origin(원격 브랜치명)
