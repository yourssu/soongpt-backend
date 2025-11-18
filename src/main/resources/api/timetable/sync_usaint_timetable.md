# sync_usaint_timetable (POST /api/timetables/usaint/sync)

## Request Body

이 엔드포인트는 별도의 Request Body를 요구하지 않습니다.

---

## Reply

### Response Body

| name          | type   | description                                   |
| ------------- | ------ | --------------------------------------------- |
| `timestamp` | string | 응답 생성 시각 (`yyyy-MM-dd HH:mm:ss` 형식) |
| `result`    | object | 유세인트 동기화 결과ㅇ;                       |

#### Result

| name              | type   | description                                                                 |
| ----------------- | ------ | --------------------------------------------------------------------------- |
| `lastSyncedAt`  | string | 이번 유세인트 동기화 기준 시각 (`yyyy-MM-dd HH:mm:ss` 형식)               |
| `rusaintStatus` | string | 유세인트 연동/세션 상태 (`"LINKED"` \| `"EXPIRED"` \| `"NOT_LINKED"`) |
| `note`          | string | 간단한 안내 메시지                                                          |

### 200 OK

유세인트 세션이 유효하여, 최신 유세인트 데이터를 정상적으로 동기화한 경우입니다.

```json
{
  "timestamp": "2025-11-18 14:21:03",
  "result": {
    "lastSyncedAt": "2025-11-18 14:21:03",
    "rusaintStatus": "LINKED",
    "note": "유세인트 정보가 성공적으로 업데이트되었습니다."
  }
}
```

---

### 409 Conflict - RUSAINT_SESSION_EXPIRED

서버 내부의 rusaint 세션(sToken)이 만료되었거나 존재하지 않아
유세인트 재인증이 필요한 경우입니다.

클라이언트는 이 응답을 기준으로 유저에게 “유세인트 재인증 필요” 안내를 띄우고,
유세인트 로그인 페이지로 이동시킵니다.

```json
{
  "timestamp": "2025-11-18 14:30:00",
  "code": "RUSAINT_SESSION_EXPIRED",
  "message": "유세인트 세션이 만료되었습니다. 다시 유세인트 인증이 필요합니다.",
  "result": {
    "rusaintStatus": "EXPIRED",
    "lastSyncedAt": "2025-09-01 10:12:00"
  }
}
```

---
