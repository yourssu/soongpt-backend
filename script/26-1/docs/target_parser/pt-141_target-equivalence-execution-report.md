# PT-141 필요충분관계(동치) 정렬 실행 리포트

## 목적
`AI소프트웨어학부(9)`, `소프트웨어학부(5)`, `AI융합학부(2)`의 `target`을 서로 필요충분(동치) 관계로 맞춤.

## 방법
- 3개 학과 target의 union signature 생성
  - signature: `course_code, scope_type, student_type, college_id, grade1~5, is_denied, is_strict`
- union에 없는 항목만 각 학과로 `NOT EXISTS` 조건 INSERT

## 실행 결과
### Before
- 9: 127
- 5: 146
- 2: 147

### Inserted
- into 9: 25
- into 5: 6
- into 2: 5

### After
- 9: 152
- 5: 152
- 2: 152

## 동치 검증 (row-level)
- 9 \ 5 = 0
- 5 \ 9 = 0
- 9 \ 2 = 0
- 2 \ 9 = 0
- 5 \ 2 = 0
- 2 \ 5 = 0

=> 세 학과 target은 현재 동일 집합(필요충분관계)으로 정렬 완료.

## 관련 SQL
- `script/26-1/sql/pt-141_make_equivalent_targets.sql`
- `script/26-1/sql/pt-141_make_equivalent_targets_verify.sql`
