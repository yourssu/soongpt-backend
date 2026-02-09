# PT-60 데이터 정제 계획 (`ssu26-1.csv`)

## 1. 목표
- 입력값: `복수전공/부전공/타전공인정 + 학과 + 학년`
- 출력값: 해당 이수구분 과목 목록(분반 묶음), 전선/부선은 학년 그룹 포함
- 데이터 소스:
  - `이수구분(다전공)` 컬럼(복필/복선/부필/부선/타전공인정)
  - `수강대상` 기반 `target` 테이블(학년/허용·제외)

## 2. 프로파일링 요약 (26-1 CSV 실측)
- 총 row: `3,053`
- `이수구분(다전공)` 비어있지 않은 row: `1,527` (실제 파싱 대상은 prefix 매칭 기준)
- PT-60 관련 prefix 포함 row: `1,491`
- prefix 토큰 수:
  - `복선`: `1,468`
  - `부선`: `1,582`
  - `복필`: `298`
  - `부필`: `10`
  - `타전공*`: `0` (26-1 기준 미출현, 스키마/파서는 선반영)

## 3. 테이블 반영 전략
### 변경
- `course.multi_major_category` 추가
  - 원본 `이수구분(다전공)` 감사용 저장

### 추가
- `course_secondary_major_classification` 신설
  - `course_code`
  - `track_type`: `DOUBLE_MAJOR | MINOR | CROSS_MAJOR`
  - `completion_type`: `REQUIRED | ELECTIVE | RECOGNIZED`
  - `department_id`
  - `raw_classification`, `raw_department_token`

## 4. 정제 규칙
### 4.1 토큰 분해
- 대상 컬럼: `이수구분(다전공)`
- 구분자: `/`
- 정규식:
  - `^(복필|복선|부필|부선|타전공(?:인정(?:과목)?)?)\-(.+)$`

### 4.2 이수구분 정규화
- `복필 -> (DOUBLE_MAJOR, REQUIRED)`
- `복선 -> (DOUBLE_MAJOR, ELECTIVE)`
- `부필 -> (MINOR, REQUIRED)`
- `부선 -> (MINOR, ELECTIVE)`
- `타전공* -> (CROSS_MAJOR, RECOGNIZED)`

### 4.3 학과 토큰 정규화
- 우선순위:
  1. `department.name` 정확 매칭
  2. Alias 매핑 후 재매칭
  3. 미매핑 목록으로 격리
- 26-1 기준 주요 alias:
  - `AI소프트 -> AI소프트웨어학부`
  - `소프트 -> 소프트웨어학부`
  - `전자공학 -> 전자정보공학부 전자공학전공`
  - `IT융합 -> 전자정보공학부 IT융합전공`
  - `산업·정보 -> 산업정보시스템공학과`
  - `통계·보험 -> 정보통계보험수리학과`
  - `문예창작 -> 예술창작학부 문예창작전공`
  - `디지털미디어 -> 미디어경영학과`

### 4.4 중복 제거
- 유니크 키 기준 dedupe:
  - `(course_code, track_type, completion_type, department_id)`

## 5. 적재 파이프라인
1. `course` insert 시 `multi_major_category` 포함 저장  
   파일: `script/26-1/target_parser/generate_course_inserts.py`
2. `course_secondary_major_classification` insert 생성  
   파일: `script/26-1/target_parser/generate_secondary_major_inserts.py`
3. SQL 실행 순서
   1. `script/26-1/pt60/01_pt60_schema_changes.sql`
   2. `output/26-1-course-inserts.sql`
   3. `output/26-1-target-inserts.sql`
   4. `output/26-1-course-secondary-major-inserts.sql`

## 6. 검증 항목
- 무결성
  - `department_id` NULL 금지
  - FK 위반 0건
  - 유니크 위반 0건
- 커버리지
  - PT-60 prefix 토큰 대비 적재율 100% 목표
  - 미매핑 학과 토큰 0건 목표
- API 결과
  - `복필/부필`: 현재학년 이하 필수 과목만 반환
  - `복선/부선`: 전학년 대상 반환 + 학년 그룹 응답

## 7. 예외/운영 규칙
- `타전공인정` 토큰 미출현이어도 스키마/파서는 유지(후속 학기 대비)
- 미매핑 토큰 발생 시:
  - 즉시 배포 차단
  - alias 사전 업데이트 후 재생성
