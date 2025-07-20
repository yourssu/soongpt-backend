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
The sum of points from selected major required, general required courses, and desired major/general elective points must be less than 23.

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
| tag         | string    | Timetable tag (see Available Tags below)  |
| score       | integer   | Timetable preference score                |
| totalCredit | double    | Sum of all course credits in timetable    |
| courses     | Courses[] | List of courses included in the timetable |

#### Courses

| Name           | Type                 | Nullable | Description                                        |
|----------------|----------------------|----------|----------------------------------------------------| 
| `category`     | string               | No       | Course category                                    |
| `subCategory`  | string               | Yes      | Course sub category                                |
| `field`        | string               | Yes      | Curriculum field by admission year                 |
| `code`         | integer              | No       | Unique course code identifier                      |
| `name`         | string               | No       | Course name                                        |
| `professor`    | string               | Yes      | Name of the professor in charge                    |
| `department`   | string               | No       | Department                                         |
| `timePoints`   | string               | No       | Time points information                            |
| `personeel`    | integer              | No       | Personnel information                              |
| `scheduleRoom` | string               | No       | Schedule and room information                      |
| `target`       | string               | No       | Target students for the course                     |
| `courseTime`   | CourseTimeResponse[] | No       | Array of course schedule and classroom information |

#### CourseTimeResponse

| Name        | Type   | Nullable | Description                                     |
|-------------|--------|----------|-------------------------------------------------|
| `week`      | string | No       | Day of the week (in Korean, e.g., 월 for Monday) |
| `start`     | string | No       | Start time of the class (in HH:mm format)       |
| `end`       | string | No       | End time of the class (in HH:mm format)         |
| `classroom` | string | No       | Classroom location                              |

### Available Tags

Each timetable will have one of the following tags:

| Tag                      | Description                    |
|--------------------------|--------------------------------|
| `DEFAULT`                | 기본 태그                       |
| `NO_MORNING_CLASSES`     | 아침 수업이 없는 시간표          |
| `HAS_FREE_DAY`           | 공강 날이 있는 시간표            |
| `NO_LONG_BREAKS`         | 우주 공강이 없는 시간표          |
| `GUARANTEED_LUNCH_TIME`  | 점심시간 보장되는 시간표         |
| `NO_EVENING_CLASSES`     | 저녁수업이 없는 시간표           |

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
            "category": "전필-컴퓨터",
            "subCategory": null,
            "field": null,
            "code": 12345,
            "name": "자료구조",
            "professor": "김교수",
            "department": "컴퓨터학부",
            "timePoints": "3.0/3.0",
            "personeel": 40,
            "scheduleRoom": "월 10:00-12:00 (공학관 1004호-김교수)",
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
            "category": "전선-컴퓨터",
            "subCategory": "복선-컴퓨터",
            "field": null,
            "code": 123456,
            "name": "알고리즘",
            "professor": "이교수",
            "department": "컴퓨터학부",
            "timePoints": "3.0/3.0",
            "personeel": 35,
            "scheduleRoom": "수 14:00-16:00 (공학관 2002호-이교수)",
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
            "category": "전필-교양",
            "subCategory": null,
            "field": "['23 이후] 언어·문학\n['20,'21~'22] 창의/융합, 균형교양-언어·문학\n['19] 균형교양-언어/문학\n['16-'18] 기초역량(언어소통-한국어)\n['15 이전] 언어와문학",
            "code": 1234567,
            "name": "글쓰기",
            "professor": "박교수",
            "department": "교육과정혁신팀",
            "timePoints": "3.0/3.0",
            "personeel": 30,
            "scheduleRoom": "금 11:00-13:00 (인문관 301호-박교수)",
            "target": "전체",
            "courseTime": [
              {
                "week": "금",
                "start": "11:00",
                "end": "13:00",
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

