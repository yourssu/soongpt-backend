# PT-155 SQL 계획: 전공과목 `전체` 수강대상 범위 보정 (UNIVERSITY -> DEPARTMENT)

## 배경
- 일부 전공과목(전선/전필/전기)의 원문 수강대상은 `개설학과 N학년 전체` 의미로 보이지만,
  현재 `target.scope_type=UNIVERSITY(0)`로 저장되어 전교생 해당 학년으로 해석될 수 있음.
- 목표: 지정 과목들에 대해 `UNIVERSITY` 허용 row를 `DEPARTMENT` 허용 row로 보정.

---

## 1) 대상 과목
요청된 15개 과목코드:
- 2150078601
- 2150083201
- 2150084001
- 2150084002
- 2150084003
- 2150084501
- 2150084601
- 2150085901
- 2150128501
- 2150128601
- 2150129501
- 2150155001
- 2150156401
- 2150386604
- 2150465601

---

## 2) 보정 원칙
1. **허용(allow) row만 변환**
   - 조건: `is_denied=0 AND student_type=GENERAL(0)`
2. **UNIVERSITY(0) -> DEPARTMENT(2)**
   - `department_id`는 `course.department`와 일치하는 `department.id`로 설정
   - `college_id`는 `NULL` 유지
3. 학년 조건(`grade1~grade5`)과 `is_strict`는 기존 값 유지
4. 기존 deny row(`is_denied=1`)는 유지
5. 중복 방지
   - 동일 key(row signature) 존재 시 insert 생략

---

## 3) 사전 검증 SQL
- 대상 과목의 현재 target 상태 확인
- `course.department` -> `department.id` 매핑 가능 여부 확인
- 매핑 실패 과목(학과명 불일치) 목록 확인

핵심 체크:
- `scope_type=0` allow row 개수
- 매핑 불가 row 개수
- 보정 후 예상 insert/delete 개수

---

## 4) 실행 SQL 전략
### Step A. 백업 테이블 생성
- `target`에서 대상 과목 row를 백업 테이블(`target_backup_pt155_yyyymmddhhmm`)로 저장

### Step B. 변환용 임시 집합 생성
- 대상 과목 + allow + GENERAL + UNIVERSITY row를 기준으로
  `department_id` 매핑된 변환 후보 생성

### Step C. DEPARTMENT row 삽입
- `NOT EXISTS`로 중복 체크 후 insert
  - key: `course_code, scope_type(=2), department_id, grade1~5, student_type, is_denied, is_strict`

### Step D. 기존 UNIVERSITY allow row 삭제
- Step C가 성공한 건에 한해 원본 `scope_type=0` allow row 삭제

### Step E. 트랜잭션 커밋
- 전 과정 1 트랜잭션으로 실행

---

## 5) 사후 검증 SQL
1. 대상 과목의 allow row가 모두 `scope_type=2`인지 확인
2. 과목별 학년 조건/strict 유지 여부 확인
3. deny row 변경 없음 확인
4. 샘플 API 검증
   - `/api/courses/by-category`에서 개설학과 소속 학년은 조회되고, 타학과 동일 학년은 미조회되는지 확인

---

## 6) 롤백 계획
- 백업 테이블로 즉시 복구:
  1) 대상 과목 `target` row 삭제
  2) 백업 테이블 row 재삽입

---

## 7) 산출물
- 실행 SQL: `script/26-1/sql/pt-155_department_scope_correction.sql`
- 검증 SQL: `script/26-1/sql/pt-155_department_scope_correction_verify.sql`
- 실행 결과 보고서: `script/26-1/docs/target_parser/pt-155_department-scope-correction-report.md`
