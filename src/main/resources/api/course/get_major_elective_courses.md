# getMajorElectiveCourse (GET /api/courses/major/elective)

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
| `timePoints`   | string  | No       | time / point information           |
| `personeel`    | integer | No       | Personnel information              |
| `scheduleRoom` | string  | No       | Schedule and room information      |
| `target`       | string  | No       | Target students for the course     |

### 200 OK

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": [
    {
      "category": "전선-컴퓨터",
      "subCategory": "복선-컴퓨터",
      "field": null,
      "code": 3001234567,
      "name": "데이터베이스",
      "professor": "김데이터",
      "department": "컴퓨터학부",
      "timePoints": "3.0/3.0",
      "personeel": 40,
      "scheduleRoom": "월 10:30-11:45 (정보과학관 21301-김데이터)\n수 10:30-11:45 (정보과학관 21301-김데이터)",
      "target": "컴퓨터학부 2,3학년"
    },
    {
      "category": "전선-컴퓨터",
      "subCategory": "복선-컴퓨터",
      "field": null,
      "code": 3001234568,
      "name": "웹프로그래밍",
      "professor": "이웹개발",
      "department": "컴퓨터학부",
      "timePoints": "3.0/3.0",
      "personeel": 35,
      "scheduleRoom": "화 14:00-15:15 (정보과학관 21302-이웹개발)\n목 14:00-15:15 (정보과학관 21302-이웹개발)",
      "target": "컴퓨터학부 2,3학년"
    },
    {
      "category": "전선-소프트",
      "subCategory": null,
      "field": null,
      "code": 3001234569,
      "name": "모바일앱개발",
      "professor": null,
      "department": "소프트웨어학부",
      "timePoints": "4.0/3.0",
      "personeel": 30,
      "scheduleRoom": "",
      "target": "소프트웨어학부 3,4학년"
    }
  ]
}
```
