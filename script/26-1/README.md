# 26-1 폴더 구조 가이드

요청하신 대로 26-1 디렉터리를 카테고리별로 정리했습니다.

## 📁 주요 분류

- `reviews/`
  - `reviews/pt115/` : PT-115 QA 리뷰 결과 파일 모음 (`PT115_REVIEW_*`)
  - `reviews/checklists/` : QA 체크리스트/계획 문서

- `docs/`
  - `docs/target_parser/` : target parser 관련 정책/가이드/참고 문서

- `assets/`
  - `assets/target_parser/` : HTML 등 보조 자산 파일

- `sql/`
  - 공통 SQL 파일 (`add_fulltext_index.sql`)

- `ops/`
  - 운영/배포 스크립트 (`docker-deploy.sh`)

- `python/`
  - 기타 파이썬 파일 (`main.py`)

- `target_parser/`
  - Python 실행 스크립트(루트)
  - `data/` (json/yml 파서 데이터)
  - `output/sql/` (생성 SQL)
  - `qa/checklists/`, `qa/tests/` (검증용 체크리스트/테스트)

- `field_parser/`
  - Python 실행 스크립트(루트)
  - `data/` (입력 엑셀/CSV)
  - `sql/`, `output/sql/`, `assets/`로 분리

- `course/`
  - 원본 CSV/XLSX 데이터

- `pt60/`
  - PT60 관련 SQL/문서

- `misc/`
  - 기타 파일 (`test.json`)

## ✅ 정리 원칙

1. **리뷰 파일은 리뷰 폴더로 분리**
2. **문서/가이드는 docs로 분리**
3. **실행 스크립트(target_parser, field_parser)는 동작 안정성 위해 기존 실행 경로 유지**
