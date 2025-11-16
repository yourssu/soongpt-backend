# sync_usaint_data (POST /api/usaint/sync)

> **Internal API**
> 이 엔드포인트는 **WAS(Kotlin) ↔ rusaint-service(Python)** 간 통신에만 사용됩니다.
> 외부 클라이언트에는 노출되지 않습니다.

---

## Request

### Headers

| Name              | Required | Description                                    |
| ----------------- | -------- | ---------------------------------------------- |
| `Authorization` | true     | `Bearer {internal-jwt}` (WAS 발급 내부 토큰) |
| `Content-Type`  | true     | `application/json`                           |

### Request Body

| Name          | Type   | Required | Constraint                             |
| ------------- | ------ | -------- | -------------------------------------- |
| `studentId` | string | true     | @Range(min = 20150000, max = 20259999) |
| `sToken`    | string | true     | @NotBlank                              |

WAS는 클라이언트로부터 받은 `studentId`, `sToken`을 그대로 rusaint-service에 전달합니다.
`pseudonym`은 WAS 내부에서만 사용하는 식별자이며, 외부에는 노출되지 않습니다.

---

## Reply

### Status

- **200 OK**: u-saint 데이터 스냅샷을 정상적으로 조회하여 반환합니다.

### Response Body

```json
{
  "takenCourses": [
    {
      "year": 2024,
      "semester": "1",
      "subjectCode": "2150545501"
    }
  ],
  "flags": {
    "doubleMajor": true,
    "minor": false,
    "teaching": false
  },
  "availableCredits": {
    "previousGpa": 4.3,
    "carriedOverCredits": 3,
    "maxAvailableCredits": 21
  },
  "basicInfo": {
    "year": 2025,
    "semester": "1",
    "grade": 3,
    "department": "컴퓨터학부"
  },
  "remainingCredits": {
    "majorRequired": 12,
    "majorElective": 18,
    "generalRequired": 6,
    "generalElective": 10
  }
}
```

---

## Notes

- rusaint-service는 `studentId` + `sToken`을 사용하여 u-saint에 로그인/토큰 발급 후,
  위와 같은 형태로 u-saint 데이터를 수집하여 **동일 HTTP 응답 바디로 반환**합니다.
- WAS는 이 응답을 받아 pseudonym과 함께 DB/Redis에 저장하고,
  이후 `POST /api/timetables/usaint` 요청 시 해당 정보를 기반으로 시간표를 생성합니다.
