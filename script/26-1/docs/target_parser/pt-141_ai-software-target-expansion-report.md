# PT-141 실행 결과 리포트

## 실행 일시
- 2026-02-11 (Asia/Seoul)

## 작업 내용
- 기준: `target.department_id = 9` (AI소프트웨어학부)
- 확장 대상:
  - `department_id = 5` (소프트웨어학부)
  - `department_id = 2` (AI융합학부)
- 동일성 키(`course_code`, `scope_type`, `student_type`, `college_id`, `grade1~5`, `is_denied`, `is_strict`, `department_id`) 기준으로
  **미존재 row만 INSERT** 수행

## 실행 SQL
- `script/26-1/sql/pt-141_expand_ai-software_targets.sql`
- `script/26-1/sql/pt-141_expand_ai-software_targets_verify.sql`

## 사전/사후 건수
### Before
- AI소프트웨어학부(9): 127
- 소프트웨어학부(5): 132
- AI융합학부(2): 133

### Inserted
- 소프트웨어학부(5): 14
- AI융합학부(2): 14

### After
- AI소프트웨어학부(9): 127 (변동 없음)
- 소프트웨어학부(5): 146
- AI융합학부(2): 147

## 검증 결과
- AI소프트웨어학부 기준 누락 row 수:
  - 소프트웨어학부(5): 0
  - AI융합학부(2): 0

즉, 기준 집합 대비 두 학과 모두 동일 조건 수강대상이 충족되도록 반영 완료.
