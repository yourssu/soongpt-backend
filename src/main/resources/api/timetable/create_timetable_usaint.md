# create_timetable (POST /api/timetables/usaint)

## Request

### Request Body

| Name          | Type   | Required | Constraint                             |
| ------------- | ------ | -------- | -------------------------------------- |
| `studentId` | string | true     | @Range(min = 20150000, max = 20259999) |
| `sToken`    | string | true     | @NotBlank                              |

---

## Reply

### Response Body

| name       | type                | description                  |
| ---------- | ------------------- | ---------------------------- |
| timetables | TimetableResponse[] | List of generated timetables |

#### TimetableResponse

| name        | type      | description                               |
| ----------- | --------- | ----------------------------------------- |
| timetableId | integer   | Unique ID of the timetable                |
| tag         | string    | Timetable tag (see Available Tags below)  |
| score       | integer   | Timetable preference score                |
| totalPoint  | double    | Sum of all course points in timetable     |
| courses     | Courses[] | List of courses included in the timetable |

#### Courses

| Name             | Type                 | Nullable | Description                                        |
| ---------------- | -------------------- | -------- | -------------------------------------------------- |
| `category`     | string               | No       | Course category                                    |
| `subCategory`  | string               | Yes      | Course sub category                                |
| `field`        | string               | Yes      | Curriculum field by admission year                 |
| `code`         | integer              | No       | Unique course code identifier                      |
| `name`         | string               | No       | Course name                                        |
| `professor`    | string               | Yes      | Name of the professor in charge                    |
| `department`   | string               | No       | Department                                         |
| `division`     | string               | Yes      | Course division                                    |
| `time`         | string               | No       | Course time information                            |
| `point`        | string               | No       | Course point information                           |
| `personeel`    | integer              | No       | Personnel information                              |
| `scheduleRoom` | string               | No       | Schedule and room information                      |
| `target`       | string               | No       | Target students for the course                     |
| `courseTimes`  | CourseTimeResponse[] | No       | Array of course schedule and classroom information |

#### CourseTimeResponse

| Name          | Type   | Nullable | Description                                      |
| ------------- | ------ | -------- | ------------------------------------------------ |
| `week`      | string | No       | Day of the week (in Korean, e.g., 월 for Monday) |
| `start`     | string | No       | Start time of the class (in HH:mm format)        |
| `end`       | string | No       | End time of the class (in HH:mm format)          |
| `classroom` | string | No       | Classroom location                               |

### Available Tags

Each timetable will have one of the following tags:

| Tag                       | Description              |
| ------------------------- | ------------------------ |
| `DEFAULT`               | 기본 태그                |
| `NO_MORNING_CLASSES`    | 아침 수업이 없는 시간표  |
| `HAS_FREE_DAY`          | 공강 날이 있는 시간표    |
| `NO_LONG_BREAKS`        | 우주 공강이 없는 시간표  |
| `GUARANTEED_LUNCH_TIME` | 점심시간 보장되는 시간표 |
| `NO_EVENING_CLASSES`    | 저녁수업이 없는 시간표   |

---

### 200 OK

```json
{
  "timestamp": "2025-07-29 01:35:17",
  "result": {
    "timetables": [
      {
        "timetableId": 17,
        "tag": "기본 태그",
        "score": 10,
        "totalPoint": 21.0,
        "courses": [
          {
            "id": 2084,
            "category": "MAJOR_REQUIRED",
            "subCategory": "복선-법학/부선-법학",
            "field": "법학과목",
            "code": 2150545501,
            "name": "행정법1",
            "professor": "홍길동",
            "department": "법학과",
            "division": null,
            "time": "3.0",
            "point": "3.0",
            "personeel": 0,
            "scheduleRoom": "월 13:30-14:45 (진리관 11522 (이효계강의실)-홍길동)\n수 10:30-11:45 (진리관 11407-홍길동)",
            "target": "2학년 법학",
            "courseTimes": [
              {
                "week": "월",
                "start": "13:30",
                "end": "14:45",
                "classroom": "진리관 11522 (이효계강의실)-홍길동"
              },
              {
                "week": "수",
                "start": "10:30",
                "end": "11:45",
                "classroom": "진리관 11407-홍길동"
              }
            ]
          },
          {
            ...
          }
        ]
      },
      {
        "timetableId": 18,
        "tag": "기본 태그",
        "score": 10,
        "totalPoint": 21.0,
        "courses": [...]
      },
      {
        "timetableId": 19,
        "tag": "공강 날이 있는 시간표",
        "score": 10,
        "totalPoint": 21.0,
        "courses": [...]
      },
      {
        "timetableId": 20,
        "tag": "공강 날이 있는 시간표",
        "score": 10,
        "totalPoint": 21.0,
        "courses": [...]
      },
      {
        "timetableId": 21,
        "tag": "우주 공강이 없는 시간표",
        "score": 10,
        "totalPoint": 21.0,
        "courses": [...]
      },
      {
        "timetableId": 22,
        "tag": "점심시간 보장되는 시간표",
        "score": 10,
        "totalPoint": 21.0,
        "courses": [...]
      }
    ]
  }
}
```
