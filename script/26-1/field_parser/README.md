# field_parser 구조

`field_parser` 내부를 목적별로 정리했습니다.

## 폴더 구성
- `*.py` : 실행 스크립트
- `data/`
  - `general_elective/` (원본 엑셀 + merged CSV)
  - `merged_courses.csv`
- `sql/`
  - `create_course_field_table.sql`
- `output/sql/`
  - `26-1-course-field-inserts.sql`
- `assets/`
  - `교직과정이수표.png`

## 참고
- `generate_field_inserts.py`는 `../target_parser/output/sql`로 결과를 생성하도록 유지했습니다.
- `execute_field_inserts.py`는 `field_parser/output/sql`를 읽도록 경로를 반영했습니다.
