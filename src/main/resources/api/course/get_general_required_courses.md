# getGeneralRequiredCourse (GET /api/courses/general/required)

## Request

### Query Parameters

| Name            | Type    | Required | Constraint                                      |
|-----------------|---------|----------|-------------------------------------------------|
| `schoolId`      | integer | true     | @Range(min = 15, max = 25)                      |
| `department`    | string  | true     | @NotBlank                                       |
| `subDepartment` | string  | false    | @NotBlank                                       |
| `grade`         | integer | true     | @Range(min = 1, max = 5)                        |
| `field`         | string  | false    | Curriculum field by admission year(e.g., 과학·기술) |

## Reply

### Response Body

| Name           | Type    | Nullable | Description                        |
|----------------|---------|----------|------------------------------------|
| `category`     | string  | No       | Course category                    |
| `subCategory`  | string  | Yes      | Course sub category                |
| `field`        | string  | Yes      | Curriculum field by admission year |
| `code`         | integer | No       | Unique course code identifier      |
| `name`         | string  | No       | Course name                        |
| `professor`    | string  | Yes      | Name of the professor in charge    |
| `department`   | string  | No       | Department                         |
| `division`     | string  | Yes      | Course division                    |
| `time`         | string  | No       | Course time information            |
| `point`        | string  | No       | Course point information           |
| `personeel`    | integer | No       | Personnel information              |
| `scheduleRoom` | string  | No       | Schedule and room information      |
| `target`       | string  | No       | Target students for the course     |

### 200 OK

```json
{
  "timestamp": "2025-05-18 15:18:00",
  "result": [
    {
      "category": "전필-교양",
      "subCategory": null,
      "field": "과학·기술",
      "code": 1001234567,
      "name": "일반물리학및실험1",
      "professor": "김물리",
      "department": "교육과정혁신팀",
      "division": "01분반",
      "time": "4.0",
      "point": "3.0",
      "personeel": 50,
      "scheduleRoom": "월 09:00-10:15 (정보과학관 21101-김물리)\n수 09:00-10:15 (정보과학관 21101-김물리)\n금 13:00-15:50 (정보과학관 21201-김물리)",
      "target": "이공계열 전체학년"
    },
    {
      "category": "전필-교양",
      "subCategory": null,
      "field": "과학·기술",
      "code": 1001234568,
      "name": "일반물리학및실험1",
      "professor": "박실험",
      "department": "교육과정혁신팀",
      "division": "01분반",
      "time": "4.0",
      "point": "3.0",
      "personeel": 45,
      "scheduleRoom": "화 09:00-10:15 (정보과학관 21102-박실험)\n목 09:00-10:15 (정보과학관 21102-박실험)\n화 13:00-15:50 (정보과학관 21202-박실험)",
      "target": "이공계열 전체학년"
    },
    {
      "category": "전필-교양",
      "subCategory": null,
      "field": "언어·문학",
      "code": 2001234567,
      "name": "대학글쓰기",
      "professor": null,
      "department": "교육과정혁신팀",
      "division": "01분반",
      "time": "2.0",
      "point": "2.0",
      "personeel": 25,
      "scheduleRoom": "",
      "target": "전체학년"
    }
  ]
}
```
