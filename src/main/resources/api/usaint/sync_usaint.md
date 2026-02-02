# 유세인트 동기화 (POST /api/usaint/sync)

## 개요

- **목적**: 클라이언트가 학번·sToken으로 유세인트(u-saint) 정보를 동기화한다. 동기화 후 재수강 과목 조회 등 pseudonym 기반 API를 사용할 수 있다.
- **식별자 보안**: 응답의 **pseudonym**은 URL/쿼리에 넣지 않고, 이후 [GET /api/usaint/retake](get_retake_courses.md) 등에서는 **헤더**(`X-Pseudonym`)로만 전달한다.

---

## Request

### Body (JSON)

| Name          | Type   | Required | Description                                                                |
| ------------- | ------ | -------- | -------------------------------------------------------------------------- |
| `studentId` | string | Yes      | 학번(2015~2025) + 4자리 숫자 (예: 20161234).                               |
| `sToken`    | string | Yes      | 유세인트 인증 토큰. sync 시에만 사용하며, 이후 API에서는 pseudonym만 사용. |

### 예시

```
POST /api/usaint/sync
Content-Type: application/json

{
  "studentId": "20161234",
  "sToken": "{sToken}"
}
```

### Validation

- `studentId`: 비어 있으면 안 되며, 패턴 `^20(1[5-9]|2[0-5])\d{4}$` (예: 20150000 ~ 20259999).
- `sToken`: 비어 있으면 안 됨.

---

## Response

### 공통 구조

- 응답 본문: `result` = `{ summary: string, pseudonym: string }`
  - `summary`: 동기화 결과 요약(예: "usaint data synced").
  - `pseudonym`: 학번 기반으로 생성된 익명 식별자. **저장 후 재수강 조회 등에서 `X-Pseudonym` 헤더로만 전달할 것.** URL/쿼리/로그에 노출 금지.

| Name          | Type   | Description                                               |
| ------------- | ------ | --------------------------------------------------------- |
| `summary`   | string | 동기화 결과 요약.                                         |
| `pseudonym` | string | 이후 pseudonym 기반 API 호출 시 사용할 값. 헤더로만 전달. |

### 예시

```json
{
  "timestamp": "2025-02-03 12:00:00",
  "result": {
    "summary": "usaint data synced",
    "pseudonym": "{pseudonym}"
  }
}
```

---

## 이후 플로우

1. 클라이언트는 응답의 `pseudonym`을 저장한다.
2. [GET /api/usaint/retake](get_retake_courses.md) 등 pseudonym 기반 API 호출 시, **쿼리/바디가 아닌 요청 헤더 `X-Pseudonym`** 에만 넣어 전달한다.
