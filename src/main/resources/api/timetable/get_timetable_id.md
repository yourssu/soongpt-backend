# get_timetable (GET /api/timetables/{timetableId})

## Path Variable

| Name          | Type    | Required | Description                |
|---------------|---------|----------|----------------------------|
| timetableId   | integer | true     | ID of the timetable to get |

---

## Response Body

| name        | type     | description                               |
|-------------|----------|-------------------------------------------|
| timetableId | integer  | Unique ID of the timetable                |
| tag         | string   | Timetable tag (e.g., NO_MORNING_CLASSES)  |
| score       | integer  | Timetable preference score                |
| courses     | Course[] | List of courses included in the timetable |

### Course

| Name           | Type                 | Nullable | Description                                        |
|----------------|----------------------|----------|----------------------------------------------------| 
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
| `courseTimes`   | CourseTimeResponse[] | No       | Array of course schedule and classroom information |

#### CourseTimeResponse

| Name        | Type   | Nullable | Description                                     |
|-------------|--------|----------|-------------------------------------------------|
| `week`      | string | No       | Day of the week (in Korean, e.g., 월 for Monday) |
| `start`     | string | No       | Start time of the class (in HH:mm format)       |
| `end`       | string | No       | End time of the class (in HH:mm format)         |
| `classroom` | string | No       | Classroom location                              |

---

## 200 OK

```json
{
  "timestamp": "2025-01-24 01:23:45",
  "result": {
    "timetableId": 1,
    "tag": "NO_MORNING_CLASSES",
    "score": 155,
    "courses": [
      {
        "category": "전필-컴퓨터",
        "subCategory": null,
        "field": null,
        "code": 12345,
        "name": "컴퓨터구조",
        "professor": "홍길동",
        "department": "컴퓨터학부",
        "division": "(가)분반",
        "time": "3.0",
        "point": "3.0",
        "personeel": 40,
        "scheduleRoom": "월 19:00-21:00 (공학관 1004호-홍길동)",
        "target": "컴퓨터공학과 2학년",
        "courseTimes": [
          {
            "week": "월",
            "start": "19:00",
            "end": "21:00",
            "classroom": "공학관 1004호"
          }
        ]
      },
      {
        "category": "전선-컴퓨터",
        "subCategory": "복선-컴퓨터",
        "field": null,
        "code": 123456,
        "name": "자료구조",
        "professor": "이몽룡",
        "department": "컴퓨터학부",
        "division": "(나)분반",
        "time": "3.0",
        "point": "3.0",
        "personeel": 35,
        "scheduleRoom": "화 15:00-17:00 (공학관 1003호-이몽룡)",
        "target": "컴퓨터공학과 2학년",
        "courseTimes": [
          {
            "week": "화",
            "start": "15:00",
            "end": "17:00",
            "classroom": "공학관 1003호"
          }
        ]
      },
      {
        "category": "전필-교양",
        "subCategory": null,
        "field": "과학·기술",
        "code": 1234567,
        "name": "영어회화",
        "professor": "성춘향",
        "department": "교육과정혁신팀",
        "division": null,
        "time": "3.0",
        "point": "3.0",
        "personeel": 25,
        "scheduleRoom": "수 10:00-12:00 (인문관 201호-성춘향)",
        "target": "전체",
        "courseTimes": [
          {
            "week": "수",
            "start": "10:00",
            "end": "12:00",
            "classroom": "인문관 201호"
          }
        ]
      }
    ]
  }
}
```
