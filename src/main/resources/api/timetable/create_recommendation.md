# create_recommendation (POST /api/timetables)

사용자가 선택한 과목 목록을 기반으로 시간표 조합을 생성하고, 가능한 제안들을 함께 반환합니다.

## Request

### Request Body

| Name | Type | Required | Description |
|---|---|---|---|
| `maxCredit` | integer | true | 수강 희망 최대 학점 |
| `retakeCourses` | SelectedCourseDto[] | false | 재수강할 과목 목록 |
| `majorRequiredCourses` | SelectedCourseDto[] | false | 전공 필수 과목 목록 |
| `majorElectiveCourses` | SelectedCourseDto[] | false | 전공 선택 과목 목록 |
| `otherMajorCourses` | SelectedCourseDto[] | false | 타전공 과목 목록 |
| `generalRequiredCourses` | SelectedCourseDto[] | false | 교양 필수 과목 목록 |
| `addedCourses` | SelectedCourseDto[] | false | 직접 추가한 과목 목록 |

#### SelectedCourseDto

| Name | Type | Required | Description |
|---|---|---|---|
| `courseCode` | integer | true | 과목의 대표 코드 (분반 제외, 예: 12345678) |
| `selectedCourseIds` | integer[] | true | 사용자가 직접 선택한 특정 분반 코드 목록. 비어있을 경우, 해당 과목의 모든 분반을 후보로 간주. |

---

## Reply

### Response Body

응답은 `status` 필드의 값에 따라 세 가지 다른 구조를 가집니다.

| Name | Type | Description |
|---|---|---|
| `status` | string | 응답의 상태. `SUCCESS`, `SINGLE_CONFLICT`, `FAILURE` 중 하나. |
| `successResponse` | FullTimetableRecommendationResponse | `status`가 `SUCCESS`일 때만 존재. |
| `singleConflictCourses` | DeletableCourseDto[] | `status`가 `SINGLE_CONFLICT`일 때만 존재. |

---

### Case 1: `status: SUCCESS`

시간표 조합 생성에 성공한 경우입니다.

#### FullTimetableRecommendationResponse

| Name | Type | Description |
|---|---|---|
| `primaryTimetable` | TimetableResponse | 생성된 시간표 중 가장 추천하는 최적 시간표 1개 |
| `alternativeSuggestions` | RecommendationDto[] | 차선책으로 선택 가능한 제안 시간표 목록 |

<br/>

**`TimetableResponse`** 와 **`RecommendationDto`** 의 상세 구조는 `get_timetable_id.md` 문서를 참고하세요.

**201 CREATED Example:**
```json
{
  "timestamp": "2026-02-05T14:30:00.123Z",
  "result": {
    "status": "SUCCESS",
    "successResponse": {
      "primaryTimetable": {
        "timetableId": 101,
        "tag": "점심시간 보장",
        "score": 85,
        "totalPoint": 18.0,
        "courses": [
          { ... }
        ]
      },
      "alternativeSuggestions": [
        {
          "description": "'컴퓨터구조'를 '가반'에서 '나반'으로 변경하여 '공강 날이 있는 시간표' 효과를 얻을 수 있습니다.",
          "timetable": {
            "timetableId": 102,
            "tag": "공강 날이 있는 시간표",
            "score": 80,
            "totalPoint": 18.0,
            "courses": [ { ... } ]
          }
        }
      ]
    }
  }
}
```

---

### Case 2: `status: SINGLE_CONFLICT`

시간표 조합은 불가능하지만, 특정 과목 1개를 제거하면 조합이 가능해지는 경우입니다.

#### DeletableCourseDto

| Name | Type | Description |
|---|---|---|
| `courseCode` | integer | 삭제를 추천하는 과목의 대표 코드 |
| `category` | string | 해당 과목의 이수 구분 (예: `MAJOR_REQUIRED`) |

<br/>

**201 CREATED Example:**
```json
{
  "timestamp": "2026-02-05T14:30:00.123Z",
  "result": {
    "status": "SINGLE_CONFLICT",
    "singleConflictCourses": [
      {
        "courseCode": 2150143401,
        "category": "MAJOR_ELECTIVE"
      },
      {
        "courseCode": 2150225101,
        "category": "MAJOR_ELECTIVE"
      }
    ]
  }
}
```

---

### Case 3: `status: FAILURE`

1개 과목을 제거해도 시간표 조합이 불가능한 경우입니다. 이 경우 **HTTP 상태 코드가 400 Bad Request**로 반환됩니다.

**400 Bad Request Example:**
```json
{
  "timestamp": "2026-02-05T14:30:00.123Z",
  "result": {
    "status": "FAILURE"
  }
}
```
