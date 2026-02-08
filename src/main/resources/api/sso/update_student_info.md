# 학적정보 수정 (PUT /api/sync/student-info)

## 개요

- **목적**: 동기화된 학적정보가 틀린 경우 사용자가 직접 수정한다.
- **전제**: [동기화 상태 조회](sync_status.md)에서 `COMPLETED`를 받은 뒤 사용.
- **인증**: `soongpt_auth` 쿠키 (JWT) 필요.
- **효과**: 캐시의 basicInfo/flags가 업데이트되어 이후 화면별 API에서 수정된 정보가 사용된다.

---

## Request

### Headers / Cookie

| Name            | Type   | Required | Description                      |
|-----------------|--------|----------|----------------------------------|
| `soongpt_auth`  | cookie | Yes      | SSO 콜백에서 발급된 JWT 쿠키     |

### Request Body

| Name                     | Type    | Nullable | Description          |
|--------------------------|---------|----------|----------------------|
| `grade`                  | integer | No       | 학년                 |
| `semester`               | integer | No       | 학기 차수            |
| `year`                   | integer | No       | 입학년도             |
| `department`             | string  | No       | 소속 학과            |
| `doubleMajorDepartment`  | string  | Yes      | 복수전공 학과        |
| `minorDepartment`        | string  | Yes      | 부전공 학과          |
| `teaching`               | boolean | No       | 교직 이수 여부       |

### 예시

```
PUT /api/sync/student-info
Cookie: soongpt_auth={JWT}
Content-Type: application/json

{
  "grade": 4,
  "semester": 7,
  "year": 2023,
  "department": "컴퓨터학부",
  "doubleMajorDepartment": null,
  "minorDepartment": null,
  "teaching": false
}
```

---

## Response

### 200 OK — 수정 성공

수정된 학적정보와 함께 `COMPLETED` 상태를 반환한다.

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": {
    "status": "COMPLETED",
    "reason": null,
    "studentInfo": {
      "grade": 4,
      "semester": 7,
      "year": 2023,
      "department": "컴퓨터학부",
      "doubleMajorDepartment": null,
      "minorDepartment": null,
      "teaching": false
    }
  }
}
```

### 401 Unauthorized — 인증 오류

| reason            | 의미                          |
|-------------------|-------------------------------|
| `invalid_session` | 쿠키 없음 또는 JWT 검증 실패  |
| `session_expired` | 동기화 세션 만료              |

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
