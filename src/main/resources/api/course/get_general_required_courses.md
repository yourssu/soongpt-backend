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
| `timePoints`   | string  | No       | time / point information           |
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
      "field": "['23 이후] 과학·기술\n['20,'21~'22] 창의/융합, 균형교양-자연과학·공학·기술\n['19] 균형교양-자연/공학(자연/과학/기술)\n['16-'18] 기초역량(과학정보기술-정보기술)\n['15 이전] 정보와기술(융합-자연)",
      "code": 1001234567,
      "name": "일반물리학및실험1",
      "professor": "김물리",
      "department": "교육과정혁신팀",
      "timePoints": "4.0/3.0",
      "personeel": 50,
      "scheduleRoom": "월 09:00-10:15 (정보과학관 21101-김물리)\n수 09:00-10:15 (정보과학관 21101-김물리)\n금 13:00-15:50 (정보과학관 21201-김물리)",
      "target": "이공계열 전체학년"
    },
    {
      "category": "전필-교양",
      "subCategory": null,
      "field": "['23 이후] 과학·기술\n['20,'21~'22] 창의/융합, 균형교양-자연과학·공학·기술\n['19] 균형교양-자연/공학(자연/과학/기술)\n['16-'18] 기초역량(과학정보기술-정보기술)\n['15 이전] 정보와기술(융합-자연)",
      "code": 1001234568,
      "name": "일반물리학및실험1",
      "professor": "박실험",
      "department": "교육과정혁신팀",
      "timePoints": "4.0/3.0",
      "personeel": 45,
      "scheduleRoom": "화 09:00-10:15 (정보과학관 21102-박실험)\n목 09:00-10:15 (정보과학관 21102-박실험)\n화 13:00-15:50 (정보과학관 21202-박실험)",
      "target": "이공계열 전체학년"
    },
    {
      "category": "전필-교양",
      "subCategory": null,
      "field": "['23 이후] 언어·문학\n['20,'21~'22] 창의/융합, 균형교양-언어·문학\n['19] 균형교양-언어/문학\n['16-'18] 기초역량(언어소통-한국어)\n['15 이전] 언어와문학",
      "code": 2001234567,
      "name": "대학글쓰기",
      "professor": null,
      "department": "교육과정혁신팀",
      "timePoints": "2.0/2.0",
      "personeel": 25,
      "scheduleRoom": "",
      "target": "전체학년"
    }
  ]
}
```
