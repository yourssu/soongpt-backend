# PT-115 주전공 수강대상 데이터 QA 계획 & 체크리스트

## 0) 문서 목적
- **목적**: `주전공` 과목의 `수강대상(target)` 데이터 품질을 점검하고, DB 직접 수정 시 안전하게 반영하기 위한 실행 기준을 정의한다.
- **배경**: QA 브랜치(`feature/pt-115`)에서 주전공 대상 데이터 이상치를 식별/수정해야 함.

---

## 1) 범위 정의

### In Scope
- `course.category IN ('MAJOR_REQUIRED', 'MAJOR_ELECTIVE', 'MAJOR_BASIC')` 과목
- 위 과목에 연결된 `target` 레코드
- 관련 참조 테이블: `department`, `college`

### Out of Scope (이번 단계 제외)
- 복수전공/부전공 분류 로직 검증
- 교양/채플/교직 과목 전체 QA
- 파서 코드 자체 리팩터링

---

## 2) 품질 기준 (합격 조건)

### Hard Rule (반드시 0건)
1. `target` 무결성 오류
   - 잘못된 `scope_type` / `college_id` / `department_id` 조합
   - 학년 플래그(`grade1~grade5`) 전부 false
   - 참조 무결성 위반(없는 course/department/college 참조)
2. 완전 중복 레코드
   - 의미적으로 동일한 target 레코드가 2개 이상

### Soft Rule (리뷰 후 확정)
1. 동일 조건에서 Allow/Deny 동시 존재
2. 주전공 과목인데 일반학생(`student_type=0`) 허용 타겟이 없는 케이스
3. 개설학과 대비 target scope가 비정상적으로 넓거나 협소한 케이스

---

## 3) 실행 계획 (Phase)

### Phase A. 사전 준비
1. 변경 대상 브랜치 확인: `feature/pt-115`
2. DB 백업(필수)
   - 최소: `target` + 주전공 `course` subset 백업
3. 작업 원칙
   - **읽기 쿼리로 이상치 식별 → 수정 SQL 작성 → 트랜잭션 단위 반영**
   - 한 번에 대량 수정 금지(작은 배치로 적용)
   - 수정 전/후 결과를 증적(쿼리 결과)로 남김

### Phase B. 베이스라인 수집
- 주전공 과목 수, 주전공 대상 target 수, student_type 분포, scope_type 분포를 먼저 확보

### Phase C. 이상치 탐지
- Hard Rule 쿼리부터 실행하여 무조건 제거
- Soft Rule은 `검토 목록`으로 분리

### Phase D. 수정안 작성/검토
- 이상치 유형별로 `UPDATE/DELETE/INSERT` SQL 작성
- 가능하면 1차 검토(동료 리뷰) 후 실행

### Phase E. 반영/검증
- 트랜잭션 내 반영 후 즉시 재검증 쿼리 실행
- 이상 없으면 커밋, 이상 시 롤백

### Phase F. 결과 정리
- 변경 건수/영향 범위/남은 이슈 문서화
- 후속 작업(파서 보정 필요 여부) 분리 등록

---

## 4) SQL 점검 쿼리 체크리스트

> 아래 쿼리는 MySQL 기준. 필요 시 LIMIT/조건을 조정한다.

### 4-1. 기준 집합 확인 (주전공 과목)
```sql
SELECT COUNT(*) AS major_course_cnt
FROM course
WHERE category IN ('MAJOR_REQUIRED', 'MAJOR_ELECTIVE', 'MAJOR_BASIC');
```

```sql
SELECT COUNT(*) AS major_target_cnt
FROM target t
JOIN course c ON c.code = t.course_code
WHERE c.category IN ('MAJOR_REQUIRED', 'MAJOR_ELECTIVE', 'MAJOR_BASIC');
```

### 4-2. Hard Rule - scope 무결성
```sql
SELECT t.id, t.course_code, t.scope_type, t.college_id, t.department_id
FROM target t
JOIN course c ON c.code = t.course_code
WHERE c.category IN ('MAJOR_REQUIRED', 'MAJOR_ELECTIVE', 'MAJOR_BASIC')
  AND (
    (t.scope_type = 0 AND (t.college_id IS NOT NULL OR t.department_id IS NOT NULL))
    OR (t.scope_type = 1 AND (t.college_id IS NULL OR t.department_id IS NOT NULL))
    OR (t.scope_type = 2 AND t.department_id IS NULL)
  );
```

### 4-3. Hard Rule - 학년 플래그 이상
```sql
SELECT t.id, t.course_code, t.grade1, t.grade2, t.grade3, t.grade4, t.grade5
FROM target t
JOIN course c ON c.code = t.course_code
WHERE c.category IN ('MAJOR_REQUIRED', 'MAJOR_ELECTIVE', 'MAJOR_BASIC')
  AND NOT (t.grade1 OR t.grade2 OR t.grade3 OR t.grade4 OR t.grade5);
```

### 4-4. Hard Rule - 참조 무결성
```sql
-- target -> course 누락
SELECT t.id, t.course_code
FROM target t
LEFT JOIN course c ON c.code = t.course_code
WHERE c.code IS NULL;
```

```sql
-- target -> department 누락 (scope_type=2)
SELECT t.id, t.course_code, t.department_id
FROM target t
LEFT JOIN department d ON d.id = t.department_id
WHERE t.scope_type = 2
  AND d.id IS NULL;
```

```sql
-- target -> college 누락 (scope_type=1)
SELECT t.id, t.course_code, t.college_id
FROM target t
LEFT JOIN college col ON col.id = t.college_id
WHERE t.scope_type = 1
  AND col.id IS NULL;
```

### 4-5. Hard Rule - 완전 중복 레코드
```sql
SELECT
  t.course_code, t.scope_type, t.college_id, t.department_id,
  t.grade1, t.grade2, t.grade3, t.grade4, t.grade5,
  t.is_denied, t.student_type, t.is_strict,
  COUNT(*) AS dup_cnt
FROM target t
JOIN course c ON c.code = t.course_code
WHERE c.category IN ('MAJOR_REQUIRED', 'MAJOR_ELECTIVE', 'MAJOR_BASIC')
GROUP BY
  t.course_code, t.scope_type, t.college_id, t.department_id,
  t.grade1, t.grade2, t.grade3, t.grade4, t.grade5,
  t.is_denied, t.student_type, t.is_strict
HAVING COUNT(*) > 1;
```

### 4-6. Soft Rule - Allow/Deny 충돌 후보
```sql
SELECT
  t.course_code, t.scope_type, t.college_id, t.department_id,
  t.grade1, t.grade2, t.grade3, t.grade4, t.grade5,
  t.student_type, t.is_strict,
  SUM(CASE WHEN t.is_denied = 0 THEN 1 ELSE 0 END) AS allow_cnt,
  SUM(CASE WHEN t.is_denied = 1 THEN 1 ELSE 0 END) AS deny_cnt
FROM target t
JOIN course c ON c.code = t.course_code
WHERE c.category IN ('MAJOR_REQUIRED', 'MAJOR_ELECTIVE', 'MAJOR_BASIC')
GROUP BY
  t.course_code, t.scope_type, t.college_id, t.department_id,
  t.grade1, t.grade2, t.grade3, t.grade4, t.grade5,
  t.student_type, t.is_strict
HAVING allow_cnt > 0 AND deny_cnt > 0;
```

### 4-7. Soft Rule - 일반학생 허용 target 없음
```sql
SELECT c.code, c.name, c.department, c.sub_category
FROM course c
LEFT JOIN target t
  ON t.course_code = c.code
 AND t.student_type = 0
 AND t.is_denied = 0
WHERE c.category IN ('MAJOR_REQUIRED', 'MAJOR_ELECTIVE', 'MAJOR_BASIC')
GROUP BY c.code, c.name, c.department, c.sub_category
HAVING COUNT(t.id) = 0;
```

---

## 5) DB 직접 수정 절차 (표준)

### 5-1. 작업 전 백업
```sql
-- 작업 세션 식별용 suffix는 날짜/시간으로 변경
CREATE TABLE target_backup_pt115_yyyymmdd_hhmm AS
SELECT t.*
FROM target t
JOIN course c ON c.code = t.course_code
WHERE c.category IN ('MAJOR_REQUIRED', 'MAJOR_ELECTIVE', 'MAJOR_BASIC');
```

### 5-2. 트랜잭션 템플릿
```sql
START TRANSACTION;

-- 1) 수정 전 확인
SELECT *
FROM target
WHERE id IN (/* 대상 id */)
FOR UPDATE;

-- 2) 수정 실행
-- UPDATE target SET ... WHERE id IN (...);
-- DELETE FROM target WHERE id IN (...);
-- INSERT INTO target (...) VALUES (...);

-- 3) 수정 후 검증
SELECT *
FROM target
WHERE id IN (/* 대상 id */);

-- 이상 없으면
COMMIT;
-- 문제 있으면
-- ROLLBACK;
```

### 5-3. 롤백 템플릿
```sql
-- 특정 course_code 범위를 백업본으로 복구
DELETE FROM target
WHERE course_code IN (/* 복구 대상 course_code */);

INSERT INTO target
SELECT *
FROM target_backup_pt115_yyyymmdd_hhmm
WHERE course_code IN (/* 복구 대상 course_code */);
```

---

## 6) 실행 체크리스트 (작업용)

### A. 시작 전
- [ ] 작업 브랜치가 `feature/pt-115`인지 확인
- [ ] DB 백업 생성 완료
- [ ] 읽기 전용 점검 쿼리 먼저 실행

### B. 점검
- [ ] Hard Rule 4종 결과 0건 확인
- [ ] 완전 중복 레코드 0건 확인
- [ ] Soft Rule 후보 목록 추출
- [ ] Soft Rule 후보 중 실제 오류/정상 케이스 분류

### C. 수정
- [ ] 수정 SQL를 이슈 단위(작게)로 작성
- [ ] 수정 전 대상 row `SELECT ... FOR UPDATE` 확인
- [ ] 트랜잭션으로 반영
- [ ] 반영 후 즉시 재검증 쿼리 실행

### D. 마감
- [ ] Hard Rule 재검증 0건
- [ ] 수정 건수/대상 과목 코드 목록 기록
- [ ] 남은 이슈(파서 보정 필요) 별도 TODO 기록

---

## 7) 산출물
1. QA 결과 요약
   - 총 점검 건수, 오류 유형별 건수, 최종 잔여 이슈
2. DB 수정 이력
   - 변경 SQL, 변경 row 수, 담당자, 시각
3. 후속 과제
   - 반복 발생 패턴(파서/매핑 보정 대상) 정리

---

## 8) 참고
- `script/26-1/target_parser/PLAN.md`
- `script/26-1/target_parser/TEST_CHECKLIST.md`
- `src/main/resources/api/ERD_COURSE_ELIGIBILITY.md`
- `src/main/kotlin/com/yourssu/soongpt/domain/target/storage/TargetEntity.kt`
