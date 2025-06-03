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

| Name            | Type             | Description                    |
|-----------------|------------------|--------------------------------|
| `content`       | CourseResponse[] | Array of course response data  |
| `totalElements` | long             | Total number of elements       |
| `totalPages`    | integer          | Total number of pages          |
| `size`          | integer          | Number of elements per page    |
| `page`          | integer          | Current page number            |

### CourseResponse

| Name         | Type                 | Description                                        |
|--------------|----------------------|----------------------------------------------------|
| `course_id`  | integer              | Course Id for Databases                            |
| `name`       | string               | Course name                                        |
| `professor`  | string               | Name of the professor in charge                    |
| `code`       | string               | Unique 10-digit course code                        |
| `category`   | string               | Course category (e.g., GENERAL_ELECTIVE)           |
| `credit`     | string               | Number of credits                                  |
| `target`     | string               | Target students for the course                     |
| `courseTime` | CourseTimeResponse[] | Array of course schedule and classroom information |
| `field`      | string               | Curriculum field by admission year                 |

#### CourseTimeResponse

| Name        | Type   | Description                                     |
|-------------|--------|-------------------------------------------------|
| `week`      | string | Day of the week (in Korean, e.g., 월 for Monday) |
| `start`     | string | Start time of the class (HH:mm format)          |
| `end`       | string | End time of the class (HH:mm format)            |
| `classroom` | string | Classroom location                              |

---

### 200 OK

```json
{
  "timestamp": "2025-06-04 08:46:52",
  "result": {
    "content": [
      {
        "name": "(공통)물리1및실험",
        "professor": "김남미\n이재구",
        "code": 1,
        "category": "MAJOR_ELECTIVE",
        "credit": 0,
        "target": "전체학년 기계,화공,전기,건축학부,신소재,정통전,전자정보공학부-IT융합,전자정보공학부-전자공학,AI융합,물리,화학,의생명,소프트,컴퓨터",
        "field": null,
        "courseTime": []
      },
      {
        "name": "(공통)물리1및실험",
        "professor": "김남미\n이동재",
        "code": 2,
        "category": "MAJOR_ELECTIVE",
        "credit": 0,
        "target": "전체학년 기계,화공,전기,건축학부,신소재,정통전,전자정보공학부-IT융합,전자정보공학부-전자공학,AI융합,물리,화학,의생명,소프트,컴퓨터",
        "field": null,
        "courseTime": []
      },
      {
        "name": "4차산업혁명시대의정보보안",
        "professor": "장의진",
        "code": 34,
        "category": "GENERAL_ELECTIVE",
        "credit": 0,
        "target": "전체(IT융합전공 ,컴퓨터 ,소프트 ,AI융합학부 ,글로벌미디어, 정보보호학과, 학점교류생 제한)",
        "field": "[‘23이후]과학·기술\n['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술\n['19]균형교양-자연/공학(자연/과학/기술)\n['16-'18]기초역량(과학정보기술-정보기술)\n['15이전]정보와기술(융합-자연)",
        "courseTime": []
      },
      {
        "name": "[기초]Co-op SAP 트랙",
        "professor": "홍지만\n최종석",
        "code": 102,
        "category": "GENERAL_ELECTIVE",
        "credit": 0,
        "target": "전체학년 IT융합전공 ,컴퓨터 ,소프트 ,AI융합학부 ,글로벌미디어, 경영학부, 산업정보시스템공학과 (대상외수강제한)",
        "field": "[‘23이후]과학·기술\n['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술\n['19]균형교양-자연/공학(자연/과학/기술)\n['16-'18]기초역량(과학정보기술-정보기술)\n['15이전]정보와기술(융합-자연)",
        "courseTime": []
      },
      {
        "name": "[실전]Co-op SAP 트랙",
        "professor": "홍지만\n최종석",
        "code": 105,
        "category": "GENERAL_ELECTIVE",
        "credit": 0,
        "target": "전체학년 IT융합전공 ,컴퓨터 ,소프트 ,AI융합학부 ,글로벌미디어, 경영학부, 산업정보시스템공학과 (대상외수강제한)",
        "field": "[‘23이후]과학·기술\n['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술\n['19]균형교양-자연/공학(자연/과학/기술)\n['16-'18]기초역량(과학정보기술-정보기술)\n['15이전]정보와기술(융합-자연)",
        "courseTime": []
      },
      {
        "name": "[심화]Co-op SAP 트랙",
        "professor": "홍지만\n최종석",
        "code": 106,
        "category": "MAJOR_ELECTIVE",
        "credit": 0,
        "target": "전체학년 IT융합전공 ,컴퓨터 ,소프트 ,AI융합학부 ,글로벌미디어, 경영학부, 산업정보시스템공학과 (대상외수강제한)",
        "field": null,
        "courseTime": []
      },
      {
        "name": "객체지향프로그래밍",
        "professor": "최지웅",
        "code": 146,
        "category": "MAJOR_ELECTIVE",
        "credit": 0,
        "target": "전체학년 컴퓨터 (컴퓨터학부 2학년은 수강 불가)",
        "field": null,
        "courseTime": []
      },
      {
        "name": "경영과컴퓨터",
        "professor": "김광용",
        "code": 162,
        "category": "MAJOR_ELECTIVE",
        "credit": 0,
        "target": "전체학년 혁신경영학과(계약학과) (대상외수강제한)",
        "field": null,
        "courseTime": []
      },
      {
        "name": "고급컴퓨터구조",
        "professor": "공영호",
        "code": 209,
        "category": "OTHER",
        "credit": 0,
        "target": "전체",
        "field": null,
        "courseTime": []
      },
      {
        "name": "논리회로설계및실험",
        "professor": "천명준",
        "code": 364,
        "category": "MAJOR_ELECTIVE",
        "credit": 0,
        "target": "전체학년 컴퓨터학부,빅데이터컴퓨팅융합",
        "field": null,
        "courseTime": []
      },
      {
        "name": "대학원논문연구:컴퓨터비전",
        "professor": "김희원",
        "code": 388,
        "category": "OTHER",
        "credit": 0,
        "target": "전체",
        "field": null,
        "courseTime": []
      },
      {
        "name": "선형대수",
        "professor": "박중석",
        "code": 621,
        "category": "MAJOR_ELECTIVE",
        "credit": 0,
        "target": "전체학년 컴퓨터",
        "field": null,
        "courseTime": []
      },
      {
        "name": "차세대컴퓨터시스템",
        "professor": "이영서",
        "code": 970,
        "category": "OTHER",
        "credit": 0,
        "target": "전체",
        "field": null,
        "courseTime": []
      },
      {
        "name": "컴퓨터구조",
        "professor": "하석재",
        "code": 998,
        "category": "MAJOR_ELECTIVE",
        "credit": 0,
        "target": "전체학년 소프트",
        "field": null,
        "courseTime": []
      },
      {
        "name": "컴퓨터구조특론",
        "professor": "이영서",
        "code": 999,
        "category": "OTHER",
        "credit": 0,
        "target": "전체",
        "field": null,
        "courseTime": []
      },
      {
        "name": "컴퓨터그래픽",
        "professor": "박정아",
        "code": 1000,
        "category": "GENERAL_ELECTIVE",
        "credit": 0,
        "target": "전체",
        "field": "[‘23이후]과학·기술\n['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술\n['19]균형교양-자연/공학(자연/과학/기술)\n['16-'18]기초역량(과학정보기술-정보기술)\n['15이전]정보와기술(융합-자연)",
        "courseTime": []
      },
      {
        "name": "컴퓨터그래픽",
        "professor": "박정아",
        "code": 1001,
        "category": "GENERAL_ELECTIVE",
        "credit": 0,
        "target": "전체",
        "field": "[‘23이후]과학·기술\n['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술\n['19]균형교양-자연/공학(자연/과학/기술)\n['16-'18]기초역량(과학정보기술-정보기술)\n['15이전]정보와기술(융합-자연)",
        "courseTime": []
      },
      {
        "name": "컴퓨터그래픽",
        "professor": "박정아",
        "code": 1002,
        "category": "GENERAL_ELECTIVE",
        "credit": 0,
        "target": "전체",
        "field": "[‘23이후]과학·기술\n['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술\n['19]균형교양-자연/공학(자연/과학/기술)\n['16-'18]기초역량(과학정보기술-정보기술)\n['15이전]정보와기술(융합-자연)",
        "courseTime": []
      },
      {
        "name": "컴퓨터그래픽",
        "professor": "박정아",
        "code": 1003,
        "category": "GENERAL_ELECTIVE",
        "credit": 0,
        "target": "전체",
        "field": "[‘23이후]과학·기술\n['20,'21~'22]창의/융합,균형교양-자연과학·공학·기술\n['19]균형교양-자연/공학(자연/과학/기술)\n['16-'18]기초역량(과학정보기술-정보기술)\n['15이전]정보와기술(융합-자연)",
        "courseTime": []
      },
      {
        "name": "컴퓨터그래픽스",
        "professor": "이정진",
        "code": 1004,
        "category": "OTHER",
        "credit": 0,
        "target": "전체",
        "field": null,
        "courseTime": []
      }
    ],
    "totalElements": 120,
    "totalPages": 6,
    "size": 20,
    "page": 0
  }
}
```
