# PT-160 벤처경영학과 타전공인정(CROSS_MAJOR) 분류 동치화 보고서

## 1) 배경
- `/api/courses/by-track`에서 `trackType=CROSS_MAJOR&completionType=RECOGNIZED` 조회는
  `course_secondary_major_classification` 원본 분류 기준으로 동작한다.
- dev DB 기준 `벤처경영학과`의 CROSS_MAJOR/RECOGNIZED 분류가 비어 있어 API가 빈 목록을 반환했다.
- 제공된 엑셀(이수구분(주전공)=`전선-벤처중소`)에는 122개 과목코드가 존재했고,
  이는 `벤처중소기업학과`의 인정 목록과 동일했다.
- 정책적으로 `벤처경영학과`도 동일한 타전공인정 목록을 노출하도록 동치화한다.

## 2) 조치 내용
- `course_secondary_major_classification`에서
  `벤처중소기업학과` → `벤처경영학과`로 CROSS_MAJOR/RECOGNIZED 분류를 복사하여 동치화.
- 중복 삽입 방지를 위해 `NOT EXISTS` 조건으로 **idempotent** 하게 작성.

### 실행 SQL
- `script/26-1/sql/pt-160_sync_venture-management_cross_major_from_venture-sme.sql`

### 검증 SQL
- `script/26-1/sql/pt-160_sync_venture-management_cross_major_from_venture-sme_verify.sql`

## 3) 실행 결과 (dev DB)
- `벤처중소기업학과` CROSS_MAJOR/RECOGNIZED: 122
- `벤처경영학과` CROSS_MAJOR/RECOGNIZED: 0 → 122
- INSERT row count: 122

## 4) API 검증 (dev)
다음 요청에서 두 학과의 결과 count가 동일(122)함을 확인.

- `https://api.dev.soongpt.yourssu.com/api/courses/by-track?schoolId=26&department=벤처중소기업학과&trackType=CROSS_MAJOR&completionType=RECOGNIZED`
- `https://api.dev.soongpt.yourssu.com/api/courses/by-track?schoolId=26&department=벤처경영학과&trackType=CROSS_MAJOR&completionType=RECOGNIZED`

또한 전체 58개 학과에 대해
`expected(DB distinct course_code) == api count` 전수 대조를 재수행했고 불일치 0건을 확인했다.

## 5) 주의사항
- `raw_classification`, `raw_department_token`은 벤처중소기업학과의 원본 값을 그대로 복사한다.
  (API 동작에는 영향 없음)
