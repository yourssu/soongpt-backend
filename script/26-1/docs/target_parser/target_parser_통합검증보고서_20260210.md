# target_parser 통합 검증 보고서 (2026-02-10)

## 1) 목적
- 복수전공/부전공 매핑(`course_secondary_major_classification`) 정합성 검증
- 복수전공/부전공 분류 과목의 실제 수강대상(`target`) 정합성 검증 및 DB 보정

---

## 2) 매핑 검증 결과 (DB ↔ ssu26-1)

### 2-1. 복수전공 (DOUBLE_MAJOR)
- DB 레코드: **1,764건**
- ssu26-1 기대치(복필/복선): **1,764건**
- 누락/초과: **0 / 0**
- raw 토큰 매핑 불일치: **0건**
- 판정: **정상**

### 2-2. 부전공 (MINOR)
- DB 레코드: **1,591건**
- ssu26-1 기대치(부필/부선): **1,591건**
- 누락/초과: **0 / 0**
- raw 토큰 매핑 불일치: **0건**
- 판정: **정상**

---

## 3) 수강대상(target) 검증 및 보정

### 3-1. 보정 전 의심 집계
- 분류 총 레코드: **3,355건** (복수 1,764 / 부전공 1,591)
- NO_TARGET: 0
- NO_GENERAL_TARGET: 178
- NO_SCOPE_MATCH_ALLOW: 6
- HAS_SCOPE_MATCH_DENY: 1

### 3-2. 보정 작업
- 적용 SQL: `script/26-1/target_parser/output/sql/26-1-fix-secondary-major-target-20260210.sql`
- 실제 반영: **106건 INSERT**
  - NO_GENERAL_TARGET 보완: 103건
  - NO_SCOPE_MATCH_ALLOW 보완: 3건

### 3-3. 보정 후 결과
- 잔존 의심: **HAS_SCOPE_MATCH_DENY 1건**
  - `2150534105 객체지향프로그래밍 / 컴퓨터학부`
  - 원문 정책: **전체학년 컴퓨터(2학년 제외)**
  - 판정: **정상 정책 예외(수정 불필요)**

---

## 4) 최종 판정
- 복수전공/부전공 매핑: **정상**
- 수강대상(target): 보정 완료 후 운영 가능 상태
- 정책 예외 1건은 의도된 제한으로 유지

---

## 5) 최종 유지 파일
- `target_parser_통합검증보고서_20260210.md` (본 문서)
- `../target_parser/output/sql/26-1-fix-secondary-major-target-20260210.sql`
