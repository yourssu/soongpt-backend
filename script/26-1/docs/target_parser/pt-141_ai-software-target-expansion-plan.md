# PT-141 실행 계획: AI소프트웨어학부 대상 과목의 수강대상 확장

## 배경
- AI소프트웨어학부는 **AI융합학부 + 소프트웨어학부** 성격으로 신설됨.
- 기존 학과(소프트웨어학부, AI융합학부)는 유지되고 있음.
- 목표: 현재 `target` 테이블에서 **AI소프트웨어학부가 수강 가능한 과목**을 기준으로,
  - 동일 조건의 `소프트웨어학부`, `AI융합학부` target이 존재하는지 점검
  - 없으면 동일 조건으로 추가

---

## 1) 작업 범위 정의
기준 데이터:
- `target.department_id = AI소프트웨어학부 id` 인 rows
- 포함 조건: `scope_type=DEPARTMENT` + `is_denied`/`is_strict`/학년 조건/`student_type`/`course_code`

확장 대상 학과:
- `소프트웨어학부`
- `AI융합학부`

제외/주의:
- 이미 동일 조건 row가 있으면 중복 삽입 금지
- `id`는 신규 생성(auto increment)
- `college_id/department_id` 일관성 확인

---

## 2) 사전 점검
1. 학과 id 확인
   - AI소프트웨어학부 / 소프트웨어학부 / AI융합학부
2. 기준 row 수 확인
   - AI소프트웨어학부 대상 target 총 건수
3. 중복 기준 정의
   - 동일성 키:
     - `course_code`
     - `scope_type`
     - `student_type`
     - `grade1~grade5`
     - `is_denied`
     - `is_strict`
     - `college_id` (필요 시)
     - `department_id`(확장 대상 학과 id)

---

## 3) 데이터 생성 전략
### 3-1. 후보 생성
- AI소프트웨어학부 rows를 source로 사용해,
  - `department_id`만 각각 소프트웨어학부 / AI융합학부로 치환한 2세트 생성

### 3-2. 존재 여부 검사
- target 테이블과 left join 하여
  - 동일성 키 기준 미존재 row만 추출

### 3-3. 삽입
- `INSERT INTO target (...) SELECT ... WHERE NOT EXISTS (...)`
- 트랜잭션으로 실행

---

## 4) 검증 계획
1. 삽입 전/후 건수 비교
   - 학과별(`AI소프트웨어학부`, `소프트웨어학부`, `AI융합학부`) 과목별 target 건수
2. 샘플 과목 수동 검증
   - 임의 10개 과목에서 학년/deny/strict/student_type 동일 여부 체크
3. 중복 검증
   - 동일성 키 기준 중복 row가 생기지 않았는지 확인
4. API 영향 확인(선택)
   - `/api/courses/by-category` 또는 관련 조회에서 대상 학과 수강 가능 여부 확인

---

## 5) 롤백 계획
- 실행 전 백업(덤프 또는 대상 rows 백업 테이블)
- 이번 작업으로 삽입한 row 식별 조건을 남겨 삭제 가능하게 준비
  - 예: 실행 시각/임시 태그 컬럼이 없으므로, 동일성 키 + 대상 학과 + source 과목집합으로 역삭제 쿼리 작성

---

## 6) 산출물
- 실행 SQL 파일
  - `script/26-1/sql/pt-141_expand_ai-software_targets.sql`
- 검증 SQL 파일
  - `script/26-1/sql/pt-141_expand_ai-software_targets_verify.sql`
- 결과 리포트(건수/샘플 검증)
  - `script/26-1/docs/target_parser/pt-141_ai-software-target-expansion-report.md`

---

## 7) 실행 순서 요약
1. department id 확인
2. 사전 카운트/중복 체크
3. dry-run SELECT로 삽입 대상 확인
4. 트랜잭션으로 INSERT
5. 사후 검증
6. 결과 문서화
