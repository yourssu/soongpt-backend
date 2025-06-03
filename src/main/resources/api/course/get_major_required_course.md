# getMajorRequiredCourse (GET /api/v2/courses/major/required)

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

### 200 OK

```json
{
  "timestamp": "2025-05-18 15:09:00",
  "result": [
    {
      "name": "자료구조",
      "professor": "신용태",
      "code": "1234567890",
      "category": "MAJOR_REQUIRED",
      "credit": "3",
      "target": "소프트 1학년, 컴퓨터학부 전체",
      "courseTime": [
        {
          "week": "월",
          "start": "13:30",
          "end": "14:45",
          "classroom": "정보과학관 21201"
        },
        {
          "week": "화",
          "start": "13:30",
          "end": "14:45",
          "classroom": "정보과학관 21201"
        }
      ]
    },
    {
      "name": "자료구조",
      "professor": "신용태",
      "code": "2345678901",
      "category": "MAJOR_REQUIRED",
      "credit": "3",
      "target": "컴퓨터학부 전체",
      "courseTime": [
        {
          "week": "월",
          "start": "16:30",
          "end": "17:45",
          "classroom": "정보과학관 21201"
        },
        {
          "week": "화",
          "start": "16:30",
          "end": "17:45",
          "classroom": "정보과학관 21201"
        }
      ]
    },
    {
      "name": "자료구조",
      "professor": "송현주",
      "code": "3456789012",
      "category": "MAJOR_REQUIRED",
      "credit": "3",
      "target": "컴퓨터학부 전체",
      "courseTime": [
        {
          "week": "화",
          "start": "10:30",
          "end": "11:45",
          "classroom": "정보과학관 21201"
        },
        {
          "week": "목",
          "start": "10:30",
          "end": "11:45",
          "classroom": "정보과학관 21204"
        }
      ]
    },
    {
      "name": "자료구조",
      "professor": "최중규",
      "code": "4567890123",
      "category": "MAJOR_REQUIRED",
      "credit": "3",
      "target": "컴퓨터학부 전체",
      "courseTime": [
        {
          "week": "월",
          "start": "15:00",
          "end": "16:15",
          "classroom": "정보과학관 21203"
        },
        {
          "week": "수",
          "start": "15:00",
          "end": "16:15",
          "classroom": "정보과학관 21501"
        }
      ]
    }
  ]
}
```
