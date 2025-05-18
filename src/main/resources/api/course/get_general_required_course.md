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

| Name         | Type                 | Description                                        |
|--------------|----------------------|----------------------------------------------------|
| `course_id`  | integer              | Course Id for Databases                            |
| `name`       | string               | Course name                                        |
| `professor`  | string               | Name of the professor in charge                    |
| `code`       | string               | Unique 10-digit course code                        |
| `category`   | string               | Course category (e.g., GENERAL_REQUIRED)           |
| `credit`     | string               | Number of credits                                  |
| `target`     | string               | Target students for the course                     |
| `courseTime` | CourseTimeResponse[] | Array of course schedule and classroom information |
| `field`      | string               | Curriculum field by admission year                 |

### CourseTimeResponse

| Name        | Type   | Description                                     |
|-------------|--------|-------------------------------------------------|
| `week`      | string | Day of the week (in Korean, e.g., 월 for Monday) |
| `start`     | string | Start time of the class (in HH:mm format)       |
| `end`       | string | End time of the class (in HH:mm format)         |
| `classroom` | string | Classroom location                              |

### 200 OK

```json
{
  "timestamp": "2025-05-18 15:18:00",
  "result": [
    {
      "name": "컴퓨터미적분활용",
      "professor": "김민수",
      "code": "5678901234",
      "category": "GENERAL_REQUIRED",
      "credit": "3",
      "target": "전체학년",
      "courseTime": [
        {
          "week": "월",
          "start": "09:00",
          "end": "10:15",
          "classroom": "정보과학관 21001"
        },
        {
          "week": "수",
          "start": "09:00",
          "end": "10:15",
          "classroom": "정보과학관 21001"
        }
      ],
      "field": "[‘23 이후] 과학·기술\n['20,'21~'22] 창의/융합, 균형교양-자연과학·공학·기술\n['19] 균형교양-자연/공학(자연/과학/기술)\n['16-'18] 기초역량(과학정보기술-정보기술)\n['15 이전] 정보와기술(융합-자연)"
    },
    {
      "name": "컴퓨터학개론",
      "professor": "이수정",
      "code": "6789012345",
      "category": "GENERAL_REQUIRED",
      "credit": "3",
      "target": "1학년",
      "courseTime": [
        {
          "week": "화",
          "start": "13:30",
          "end": "14:45",
          "classroom": "정보과학관 21002"
        },
        {
          "week": "목",
          "start": "13:30",
          "end": "14:45",
          "classroom": "정보과학관 21002"
        }
      ],
      "field": "[‘23 이후] 과학·기술\n['20,'21~'22] 창의/융합, 균형교양-자연과학·공학·기술\n['19] 균형교양-자연/공학(자연/과학/기술)\n['16-'18] 기초역량(과학정보기술-정보기술)\n['15 이전] 정보와기술(융합-자연)"
    }
  ]
}
```
