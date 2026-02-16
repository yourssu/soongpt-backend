# 동기화 상태 조회 (GET /api/sync/status)

## 개요

- **목적**: 쿠키 기반 인증으로 rusaint 동기화 진행 상태를 조회한다.
- **인증**: [SSO 콜백](sso_callback.md)에서 발급된 `soongpt_auth` 쿠키 필요.
- **사용 방식**: 프론트엔드에서 fetch 폴링 (모든 응답은 JSON, 302 리다이렉트 없음).
- **분기 기준**: 프론트는 HTTP 상태코드 + `result.status` 값으로 분기한다.

---

## Request

### Headers / Cookie

| Name            | Type   | Required | Description                      |
|-----------------|--------|----------|----------------------------------|
| `soongpt_auth`  | cookie | Yes      | SSO 콜백에서 발급된 JWT 쿠키     |

### 예시

```
GET /api/sync/status
Cookie: soongpt_auth={JWT}
```

---

## Response

### 공통 구조

```json
{
  "timestamp": "...",
  "result": {
    "status": "PROCESSING | COMPLETED | REQUIRES_REAUTH | REQUIRES_USER_INPUT | FAILED",
    "reason": "string (에러/실패 시에만)",
    "studentInfo": { ... },  // COMPLETED 시에만
    "warnings": ["..."]      // COMPLETED 시에만, 빈 데이터 경고 (nullable)
  }
}
```

---

### 200 OK — PROCESSING (동기화 진행 중)

프론트: 계속 폴링한다.

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "PROCESSING",
    "reason": null,
    "studentInfo": null
  }
}
```

### 200 OK — COMPLETED (동기화 완료)

프론트: 폴링 중단, `studentInfo`를 화면에 표시한다. `warnings`가 있으면 일부 데이터가 비어있음을 의미한다.

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "COMPLETED",
    "reason": null,
    "studentInfo": {
      "grade": 3,
      "semester": 5,
      "year": 2022,
      "department": "컴퓨터학부",
      "doubleMajorDepartment": null,
      "minorDepartment": "경영학부",
      "teaching": false
    },
    "warnings": null
  }
}
```

#### 새내기(수강 이력 없음) 예시

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "COMPLETED",
    "reason": null,
    "studentInfo": {
      "grade": 1,
      "semester": 1,
      "year": 2025,
      "department": "컴퓨터학부",
      "doubleMajorDepartment": null,
      "minorDepartment": null,
      "teaching": false
    },
    "warnings": ["NO_SEMESTER_INFO", "NO_COURSE_HISTORY", "NO_GRADUATION_DATA"]
  }
}
```

#### StudentInfo 필드

| Name                     | Type    | Nullable | Description          |
|--------------------------|---------|----------|----------------------|
| `grade`                  | integer | No       | 학년                 |
| `semester`               | integer | No       | 학기 차수            |
| `year`                   | integer | No       | 입학년도             |
| `department`             | string  | No       | 소속 학과            |
| `doubleMajorDepartment`  | string  | Yes      | 복수전공 학과        |
| `minorDepartment`        | string  | Yes      | 부전공 학과          |
| `teaching`               | boolean | No       | 교직 이수 여부       |

#### warnings 필드

| Code                 | 의미                                    | 비고 |
|----------------------|-----------------------------------------|------|
| `NO_COURSE_HISTORY`  | 수강 이력 없음 (빈 학기 목록)           | - |
| `NO_SEMESTER_INFO`   | 학기 정보 없어 기본값(1학기) 사용       | - |
| `NO_GRADUATION_DATA` | 졸업사정표 조회 불가 (1-1·미제공 등)   | 동기화 단계에서 세션에 저장됨. 과목 추천 API 호출 시 `NO_GRADUATION_REPORT`도 함께 올 수 있음 → [졸업사정표 경고 가이드](../requirements/졸업사정표_경고_가이드.md) 참고 |

`warnings`는 COMPLETED 시에만 포함되며, 경고가 없으면 `null`이다. 동기화 자체는 정상 완료이므로 기존 COMPLETED 플로우 그대로 진행하면 된다.

### 200 OK — REQUIRES_REAUTH (재인증 필요)

프론트: SSO 재로그인으로 유도한다.

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "REQUIRES_REAUTH",
    "reason": "token_expired",
    "studentInfo": null
  }
}
```

### 200 OK — REQUIRES_USER_INPUT (학적 정보 없음/매칭 실패, 사용자 입력 필요)

유세인트에서 기본 학적 정보를 조회하지 못했거나, 학년/학과/입학년도 등이 DB와 매칭되지 않은 경우. 프론트: 학적 정보 직접 입력 화면으로 유도한다. `studentInfo`는 null이다.

| reason | 의미 |
|--------|------|
| `student_info_mapping_failed` | 학년/학과/입학년도 매칭 실패 (데이터는 왔으나 DB 매칭 실패) |
| `student_info_mapping_failed: basic_info_unavailable` | 유세인트에서 기본 학적 정보 조회 실패(데이터 없음·파싱 실패 등) |

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "REQUIRES_USER_INPUT",
    "reason": "student_info_mapping_failed: basic_info_unavailable",
    "studentInfo": null,
    "warnings": null
  }
}
```

### 200 OK — FAILED (동기화 실패)

프론트: 에러 안내 및 재시도 유도.

| reason | 의미 |
|--------|------|
| `server_unreachable` | 유세인트 서버 접속 불가 |
| `server_timeout` | 유세인트 서버 응답 시간 초과 |
| `internal_error` | 내부 서버 오류 |

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "FAILED",
    "reason": "server_timeout",
    "studentInfo": null
  }
}
```

### 401 Unauthorized — ERROR (인증 오류)

프론트: SSO 재로그인으로 유도한다.

| reason            | 의미                     |
|-------------------|--------------------------|
| `invalid_session` | 쿠키 없음 또는 JWT 검증 실패 |
| `session_expired` | 동기화 세션 만료 (캐시 TTL 초과) |

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "ERROR",
    "reason": "invalid_session",
    "studentInfo": null
  }
}
```

---

## 코드 기준: rusaint에서 안 가져왔을 때

아래는 **현재 코드**(`RusaintServiceClient`, `SsoService.startAsyncRusaintFetch`, `mapFailReason`) 기준 동작이다.

| rusaint 상황 | HTTP status 처리 | 비동기 결과 (GET /api/sync/status) | studentInfo |
|--------------|------------------|-------------------------------------|-------------|
| **academic 500/503** (학적 조회 실패·파싱 실패) | 500/503 → `getAcademicSnapshot`에서 **null 반환** (throw 안 함) | **200 REQUIRES_USER_INPUT**<br>`reason=student_info_mapping_failed: basic_info_unavailable` | null |
| **academic 502** | 502 → **RusaintServiceException** throw | **200 FAILED** `reason=server_unreachable` | null |
| **academic 504** | 504 → **RusaintServiceException** throw | **200 FAILED** `reason=server_timeout` | null |
| **graduation 500/503** (졸업사정만 없음) | 500/503 → `getGraduationSnapshot`에서 **null 반환** | **200 COMPLETED** (merge 성공, academic 있음)<br>`warnings`에 `NO_GRADUATION_DATA` | 있음 |
| **graduation 502/504** | 502/504 → **RusaintServiceException** throw | **200 FAILED** `reason=server_unreachable` / `server_timeout` | null |
| **401** (비동기 fetch 중 sToken 만료) | 401 → **RusaintServiceException** throw, `isUnauthorized` | **200 REQUIRES_REAUTH** `reason=token_expired` | null |
| **연결 실패** (네트워크·연결 거부 등) | `RestClientException` → **RusaintServiceException**(statusCode=null) throw | **200 FAILED** `reason=internal_error` | null |
| **merge 검증 실패** (학과 미매칭 등) | `mergeWithValidation`에서 `validationError` 설정 → **StudentInfoMappingException** | **200 REQUIRES_USER_INPUT**<br>`reason=student_info_mapping_failed: {검증사유}` | null 또는 partialUsaintData |

요약:

- **학적(academic)을 못 가져오면** (500/503 등): **REQUIRES_USER_INPUT**, `basic_info_unavailable`. FAILED가 아님.
- **졸업사정(graduation)만 못 가져오면** (500/503): **COMPLETED** + `NO_GRADUATION_DATA`. 실패 아님.
- **502/504**만 **FAILED** (server_unreachable / server_timeout).
- **internal_error**는 **502/504/401이 아닌** rusaint 예외가 아닐 때: 연결 실패(statusCode 없음) 또는 WAS 내부 예외(merge 등).
