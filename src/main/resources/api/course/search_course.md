# searchCourse (GET /api/courses/search)

## Request

### Query Parameters

| Name   | Type    | Required | Constraint                          | Description               |
|--------|---------|----------|-------------------------------------|---------------------------|
| `q`    | string  | true     | 2-100자, 한글/영문/숫자/공백/점/언더스코어/하이픈만 허용 | Keyword for search        |
| `page` | integer | false    | 0 ≤ value                           | Page number (default: 0)  |
| `size` | integer | false    | 1 ≤ value ≤ 100                     | Page size (default: 20)   |
| `sort` | string  | false    | ASC \| DESC (대소문자 구분 없음)     | Sort order (default: ASC) |

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
| `division`     | string  | Yes      | Course division                    |
| `time`         | string  | No       | Course time information            |
| `point`        | string  | No       | Course point information           |
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
        "code": 1001100101,
        "name": "(공통)물리1및실험",
        "professor": "김남미\n이재구",
        "department": "물리학과",
        "division": "01분반",
        "time": "4.0",
        "point": "3.0",
        "personeel": 25,
        "scheduleRoom": "목 13:00-13:50 (숭덕경상관 02317-김남미)\n목 14:00-14:50 (숭덕경상관 02317-김남미)",
        "target": "전체학년 기계,화공,전기,건축학부,신소재,정통전,전자정보공학부-IT융합,전자정보공학부-전자공학,AI융합,물리,화학,의생명,소프트,컴퓨터"
      },
      {
        "category": "전선-물리",
        "subCategory": null,
        "field": null,
        "code": 1001100102,
        "name": "(공통)물리1및실험",
        "professor": "김남미\n이동재",
        "department": "물리학과",
        "division": "01분반",
        "time": "4.0",
        "point": "3.0",
        "personeel": 25,
        "scheduleRoom": "목 15:00-15:50 (조만식기념관 12123-이재구)\n목 16:00-16:50 (조만식기념관 12123-이재구)",
        "target": "전체학년 기계,화공,전기,건축학부,신소재,정통전,전자정보공학부-IT융합,전자정보공학부-전자공학,AI융합,물리,화학,의생명,소프트,컴퓨터"
      },
      {
        "category": "전선-교양",
        "subCategory": null,
        "field": "과학·기술",
        "code": 1001100103,
        "name": "4차산업혁명시대의정보보안",
        "professor": "장의진",
        "department": "교육과정혁신팀물리",
        "division": null,
        "time": "3.0",
        "point": "3.0",
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
