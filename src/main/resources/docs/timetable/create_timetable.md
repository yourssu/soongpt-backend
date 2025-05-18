# create_timetable (POST /api/timetables)

## Request

### Request Body

| Name                     | Type     | Required | Constraint                 |
|--------------------------|----------|----------|----------------------------|
| `schoolId`               | integer  | true     | @Range(min = 15, max = 25) |
| `department`             | string   | true     | @NotBlank                  |
| `subDepartment`          | string   | false    |                            |
| `grade`                  | integer  | true     | @Range(min = 1, max = 5)   |
| `isChapel`               | boolean  | false    | default: false             |
| `majorRequiredCourses`   | string[] | true     | @NotNull                   |
| `majorElectiveCourses`   | string[] | true     | @NotNull                   |
| `generalRequiredCourses` | string[] | true     | @NotNull                   |
| `majorElectiveCredit`    | integer  | true     | @Range(min = 0, max = 22)  |
| `generalElectiveCredit`  | integer  | true     | @Range(min = 0, max = 22)  |

**Global Constraint:**  
The sum of credits from selected major required, general required courses, and desired major/general elective credits must be less than 23.

---

## Reply

### Response Body

| name       | type                | description                  |
|------------|---------------------|------------------------------|
| timetables | TimetableResponse[] | List of generated timetables |

#### TimetableResponse

| name        | type      | description                               |
|-------------|-----------|-------------------------------------------|
| timetableId | integer   | Unique ID of the timetable                |
| tag         | string    | Timetable tag (e.g., NO_MORNING_CLASSES)  |
| score       | integer   | Timetable preference score                |
| totalCredit | double    | Sum of all course credits in timetable    |
| courses     | Courses[] | List of courses included in the timetable |

#### Courses

| name           | type         | description                                                                          |
|----------------|--------------|--------------------------------------------------------------------------------------|
| courseName     | string       | Course name                                                                          |
| professorName  | string       | Professor name                                                                       |
| classification | string       | Course classification (e.g., MAJOR_REQUIRED, MAJOR_ELECTIVE, GENERAL_REQUIRED, etc.) |
| courseCode     | integer      | Course code                                                                          |
| credit         | integer      | Number of credits                                                                    |
| target         | string       | Target grade/department                                                              |
| courseTime     | CourseTime[] | Course schedule information                                                          |

#### CourseTime

| name      | type   | description                          |
|-----------|--------|--------------------------------------|
| week      | string | Day of the week (e.g., 월 for Monday) |
| start     | string | Start time (HH:mm)                   |
| end       | string | End time (HH:mm)                     |
| classroom | string | Classroom location                   |

---

### 200 OK

```json
{
  "timestamp": "2025-01-24 01:23:45",
  "result": {
    "timetables": [
      {
        "timetableId": 1,
        "tag": "NO_MORNING_CLASSES",
        "score": 155,
        "totalCredit": 9,
        "courses": [
          {
            "courseName": "자료구조",
            "professorName": "김교수",
            "classification": "MAJOR_REQUIRED",
            "courseCode": 12345,
            "credit": 3,
            "target": "컴퓨터 2학년",
            "courseTime": [
              {
                "week": "월",
                "start": "10:00",
                "end": "12:00",
                "classroom": "공학관 1004호"
              }
            ]
          },
          {
            "courseName": "알고리즘",
            "professorName": "이교수",
            "classification": "MAJOR_ELECTIVE",
            "courseCode": 123456,
            "credit": 3,
            "target": "컴퓨터 2학년",
            "courseTime": [
              {
                "week": "수",
                "start": "14:00",
                "end": "16:00",
                "classroom": "공학관 2002호"
              }
            ]
          },
          {
            "courseName": "글쓰기",
            "professorName": "박교수",
            "classification": "GENERAL_REQUIRED",
            "courseCode": 1234567,
            "credit": 3,
            "target": "전체",
            "courseTime": [
              {
                "week": "금",
                "start": "09:00",
                "end": "11:00",
                "classroom": "인문관 301호"
              }
            ]
          }
        ]
      }
    ]
  }
}
```

