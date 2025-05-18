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

| name           | type         | description                                                                          |
|----------------|--------------|--------------------------------------------------------------------------------------|
| courseName     | string       | Course name                                                                          |
| professorName  | string       | Professor name                                                                       |
| classification | string       | Course classification (e.g., MAJOR_REQUIRED, MAJOR_ELECTIVE, GENERAL_REQUIRED, etc.) |
| courseCode     | integer      | Course code                                                                          |
| credit         | integer      | Number of credits                                                                    |
| target         | string       | Target grade/department                                                              |
| courseTime     | CourseTime[] | Course schedule information                                                          |

### CourseTime

| name      | type   | description                          |
|-----------|--------|--------------------------------------|
| week      | string | Day of the week (e.g., 월 for Monday) |
| start     | string | Start time (HH:mm)                   |
| end       | string | End time (HH:mm)                     |
| classroom | string | Classroom location                   |

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
        "courseName": "컴퓨터구조",
        "professorName": "홍길동",
        "classification": "MAJOR_REQUIRED",
        "courseCode": 12345,
        "credit": 3,
        "target": "컴퓨터공학과 2학년",
        "courseTime": [
          {
            "week": "월",
            "start": "19:00",
            "end": "21:00",
            "classroom": "공학관 1004호"
          }
        ]
      },
      {
        "courseName": "자료구조",
        "professorName": "이몽룡",
        "classification": "MAJOR_ELECTIVE",
        "courseCode": 123456,
        "credit": 3,
        "target": "컴퓨터공학과 2학년",
        "courseTime": [
          {
            "week": "화",
            "start": "15:00",
            "end": "17:00",
            "classroom": "공학관 1003호"
          }
        ]
      },
      {
        "courseName": "영어회화",
        "professorName": "성춘향",
        "classification": "GENERAL_REQUIRED",
        "courseCode": 1234567,
        "credit": 3,
        "target": "전체",
        "courseTime": [
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
