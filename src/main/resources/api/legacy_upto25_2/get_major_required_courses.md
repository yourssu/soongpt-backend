# getMajorRequiredCourse (GET /api/courses/major/required)

## Request

### Query Parameters

| Name            | Type    | Required | Constraint                 |
|-----------------|---------|----------|----------------------------|
| `schoolId`      | integer | true     | @Range(min = 15, max = 25) |
| `department`    | string  | true     | @NotBlank                  |
| `subDepartment` | string  | false    | @NotBlank                  |
| `grade`         | integer | true     | @Range(min = 1, max = 5)   |

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
  "timestamp": "2025-05-18 15:09:00",
  "result": [
    {
      "category": "전필-컴퓨터",
      "subCategory": null,
      "field": null,
      "code": 4001234567,
      "name": "자료구조",
      "professor": "신용태",
      "department": "컴퓨터학부",
      "division": "01분반",
      "time": "3.0",
      "point": "3.0",
      "personeel": 45,
      "scheduleRoom": "월 13:30-14:45 (정보과학관 21201-신용태)\n화 13:30-14:45 (정보과학관 21201-신용태)",
      "target": "소프트 1학년, 컴퓨터학부 전체"
    },
    {
      "category": "전필-컴퓨터",
      "subCategory": null,
      "field": null,
      "code": 4001234568,
      "name": "자료구조",
      "professor": "송현주",
      "department": "컴퓨터학부",
      "division": "01분반",
      "time": "3.0",
      "point": "3.0",
      "personeel": 40,
      "scheduleRoom": "화 10:30-11:45 (정보과학관 21201-송현주)\n목 10:30-11:45 (정보과학관 21204-송현주)",
      "target": "컴퓨터학부 전체"
    },
    {
      "category": "전필-소프트",
      "subCategory": null,
      "field": null,
      "code": 4001234569,
      "name": "프로그래밍기초",
      "professor": null,
      "department": "소프트웨어학부",
      "division": null,
      "time": "3.0",
      "point": "3.0",
      "personeel": 50,
      "scheduleRoom": "",
      "target": "소프트웨어학부 1학년"
    }
  ]
}
```
