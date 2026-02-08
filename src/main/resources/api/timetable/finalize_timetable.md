# finalize_timetable (POST /api/timetables/finalize)

사용자가 제안받은 시간표 중 하나를 기반으로, 교양 및 채플 과목을 추가 선택하여 최종 시간표를 확정하고 새로 저장합니다.

## Request

### Request Body

| Name | Type | Required | Description |
|---|---|---|---|
| `timetableId` | integer | true | 기반이 되는 시간표의 ID |
| `generalElectiveCourseCodes` | integer[] | false | 최종적으로 추가할 교양 과목 코드 목록 |
| `chapelCourseCode` | integer | false | 최종적으로 추가할 채플 과목 코드 |

---

## Reply

### Response Body

성공 시, 최종적으로 확정되어 새로 생성된 시간표의 전체 정보를 반환합니다.

| name | type | description |
|---|---|---|
| (root) | TimetableResponse | 최종 확정된 시간표 정보 |

<br/>

**`TimetableResponse`** 의 상세 구조는 `get_timetable_id.md` 문서를 참고하세요.

---

### 200 OK

```json
{
  "timestamp": "2026-02-05T14:35:00.456Z",
  "result": {
    "timetableId": 152,
    "tag": "점심시간 보장",
    "score": 85,
    "totalPoint": 21.0,
    "courses": [
      { ... },
      { ... },
      {
        "id": 1025,
        "category": "GENERAL_ELECTIVE",
        "name": "환경과기후(숭실사이버대)",
        ...
      }
    ]
  }
}
```

### 400 Bad Request

요청에 포함된 교양/채플 과목이 기반 시간표의 과목들과 시간이 겹칠 경우 발생합니다.

```json
{
    "timestamp": "2026-02-05T14:36:00.789Z",
    "result": null,
    "error": {
        "status": 400,
        "message": "시간이 겹치는 과목(환경과기후(숭실사이버대))이 있어 시간표를 완성할 수 없습니다."
    }
}
```
