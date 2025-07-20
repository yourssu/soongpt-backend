# searchCourse (GET /api/v2/courses/search)

## Request

### Query Parameters

| Name         | Type    | Required | Constraint      | Description               |
|--------------|---------|----------|-----------------|---------------------------|
| `schoolId`   | integer | true     | 14 < value < 26 | Year of admission         |
| `department` | string  | true     | -               | Name of the department    |
| `grade`      | integer | true     | 0 < value < 6   | Year (grade)              |
| `q`          | string  | false    | -               | Keyword for search        |
| `page`       | integer | false    | 0 ≤ value       | Page number (default: 0)  |
| `size`       | integer | false    | 0 < value       | Page size (default: 20)   |
| `sort`       | string  | false    | -               | Sort order (default: ASC) |

## Reply

### Response Body

| Name            | Type             | Nullable | Description                   |
|-----------------|------------------|----------|-------------------------------|
| `content`       | CourseResponse[] | No       | Array of course response data |
| `totalElements` | long             | No       | Total number of elements      |
| `totalPages`    | integer          | No       | Total number of pages         |
| `size`          | integer          | No       | Number of elements per page   |
| `page`          | integer          | No       | Current page number           |

### CourseResponse

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

---

### 200 OK

```json
{
  "timestamp": "2025-06-04 08:46:52",
  "result": {
    "content": [
      {
        "category": "전선-물리",
        "subCategory": null,
        "field": null,
        "code": 1001,
        "name": "(공통)물리1및실험",
        "professor": "김남미\n이재구",
        "department": "물리학과",
        "timePoints": "4.0/3.0",
        "personeel": 25,
        "scheduleRoom": "목 13:00-13:50 (숭덕경상관 02317-김남미)\n목 14:00-14:50 (숭덕경상관 02317-김남미)",
        "target": "전체학년 기계,화공,전기,건축학부,신소재,정통전,전자정보공학부-IT융합,전자정보공학부-전자공학,AI융합,물리,화학,의생명,소프트,컴퓨터"
      },
      {
        "category": "전선-물리",
        "subCategory": null,
        "field": null,
        "code": 1002,
        "name": "(공통)물리1및실험",
        "professor": "김남미\n이동재",
        "department": "물리학과",
        "timePoints": "4.0/3.0",
        "personeel": 25,
        "scheduleRoom": "목 15:00-15:50 (조만식기념관 12123-이재구)\n목 16:00-16:50 (조만식기념관 12123-이재구)",
        "target": "전체학년 기계,화공,전기,건축학부,신소재,정통전,전자정보공학부-IT융합,전자정보공학부-전자공학,AI융합,물리,화학,의생명,소프트,컴퓨터"
      },
      {
        "category": "전선-교양",
        "subCategory": null,
        "field": "['23이후]과학·기술\n['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술\n['19]균형교양-자연/공학(자연/과학/기술)\n['16-'18]기초역량(과학정보기술-정보기술)\n['15이전]정보와기술(융합-자연)",
        "code": 2034,
        "name": "4차산업혁명시대의정보보안",
        "professor": "장의진",
        "department": "교육과정혁신팀물리",
        "timePoints": "3.0/3.0",
        "personeel": 40,
        "scheduleRoom": "화 10:30-11:45 (정보과학관 21401-장의진)\n목 10:30-11:45 (정보과학관 21401-장의진)",
        "target": "전체(IT융합전공 ,컴퓨터 ,소프트 ,AI융합학부 ,글로벌미디어, 정보보호학과, 학점교류생 제한)"
      }
    ],
    "totalElements": 120,
    "totalPages": 6,
    "size": 20,
    "page": 0
  }
}
```
