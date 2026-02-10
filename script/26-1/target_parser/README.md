# target_parser 구조

`target_parser` 내부를 목적별로 정리했습니다.

## 폴더 구성
- `*.py` : 실행 스크립트(파싱/검증/SQL 생성)
- `data/`
  - `data.yml`
  - `unique_targets.json`
  - `parsed_unique_targets.json`
  - `unmapped_targets.json`
- `output/sql/`
  - 생성된 SQL 파일들 (`26-1-*.sql`)
- `qa/checklists/`
  - `TEST_CHECKLIST.md`
- `qa/tests/`
  - 파싱 검증 보조 스크립트/결과 폴더

## 참고
- 주요 스크립트는 새 경로를 기준으로 이미 경로 상수를 반영했습니다.
- 기존 작업 흐름은 그대로 유지됩니다.
