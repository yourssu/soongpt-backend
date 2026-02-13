# PT-160 AI소프트웨어학부 타전공인정(CROSS_MAJOR) 분류 동치화 보고서

## 1) 배경
- `/api/courses/by-track`에서 `trackType=CROSS_MAJOR&completionType=RECOGNIZED` 조회는
  `course_secondary_major_classification` 원본 분류 기준으로 동작한다.
- dev DB 기준 `AI소프트웨어학부`의 CROSS_MAJOR/RECOGNIZED 분류가 비어 있어 API가 빈 목록을 반환했다.
- 정책적으로 `AI소프트웨어학부`의 타전공인정 과목 목록은 `소프트웨어학부`와 동일해야 한다.

## 2) 조치 내용
- `course_secondary_major_classification`에서
  `소프트웨어학부` → `AI소프트웨어학부`로 CROSS_MAJOR/RECOGNIZED 분류를 복사하여 동치화.
- 중복 삽입 방지를 위해 `NOT EXISTS` 조건으로 **idempotent** 하게 작성.

### 실행 SQL
- `script/26-1/sql/pt-160_sync_ai-software_cross_major_from_software.sql`

### 검증 SQL
- `script/26-1/sql/pt-160_sync_ai-software_cross_major_from_software_verify.sql`

## 3) 실행 결과 (dev DB)
- `소프트웨어학부` CROSS_MAJOR/RECOGNIZED: 122
- `AI소프트웨어학부` CROSS_MAJOR/RECOGNIZED: 0 → 122
- INSERT row count: 122

## 4) API 검증 (dev)
다음 요청에서 두 학과의 결과 count가 동일(122)함을 확인.

- `https://api.dev.soongpt.yourssu.com/api/courses/by-track?schoolId=26&department=소프트웨어학부&trackType=CROSS_MAJOR&completionType=RECOGNIZED`
- `https://api.dev.soongpt.yourssu.com/api/courses/by-track?schoolId=26&department=AI소프트웨어학부&trackType=CROSS_MAJOR&completionType=RECOGNIZED`

추가로, department 58개 전체에 대해
`expected(DB distinct course_code) == api count` 전수 대조를 수행했고 불일치 0건을 확인했다.

## 5) 주의사항
- `raw_classification`, `raw_department_token`은 소프트웨어학부의 원본 값을 그대로 복사한다.
  (API 동작에는 영향 없으나, provenance 관점에서 추후 재적재/감사 시 참고 필요)
