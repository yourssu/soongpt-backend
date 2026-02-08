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
    "status": "PROCESSING | COMPLETED | REQUIRES_REAUTH | FAILED",
    "reason": "string (에러 시에만)",
    "studentInfo": { ... } // COMPLETED 시에만
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

프론트: 폴링 중단, `studentInfo`를 화면에 표시한다.

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
    }
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

### 200 OK — FAILED (동기화 실패)

프론트: 에러 안내 및 재시도 유도.

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "FAILED",
    "reason": "sync_failed",
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
