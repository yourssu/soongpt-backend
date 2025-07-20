# getCoursesByCode (GET /api/courses})

## Request

### Query Parameters

| Name   | Type      | Required | Constraint |
|--------|-----------|----------|------------|
| `code` | integer[] | true     | @NotNull   |

## Reply

### Response Body

| Name           | Type                 | Nullable | Description                                        |
|----------------|----------------------|----------|----------------------------------------------------| 
| `category`     | string               | No       | Course category                                    |
| `subCategory`  | string               | Yes      | Course sub category                                |
| `field`        | string               | Yes      | Curriculum field by admission year                 |
| `code`         | integer              | No       | Unique course code identifier                      |
| `name`         | string               | No       | Course name                                        |
| `professor`    | string               | Yes      | Name of the professor in charge                    |
| `department`   | string               | No       | Department                                         |
| `timePoints`   | string               | No       | time / point information                           |
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

### 200 OK

```json
{
  "timestamp": "2025-05-18 15:14:00",
  "result": [
    {
      "category": "전선-컴퓨터",
      "subCategory": "복선-컴퓨터",
      "field": null,
      "code": 5678901234,
      "name": "컴퓨터미적분활용",
      "professor": "김민수",
      "department": "컴퓨터학부",
      "timePoints": "3.0/3.0",
      "personeel": "30",
      "scheduleRoom": "월 09:00-10:15 (정보과학관 21001-김민수)\n수 09:00-10:15 (정보과학관 21001-김민수)",
      "target": "컴퓨터학부 1,2학년",
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
      ]
    },
    {
      "category": "전선-컴퓨터",
      "subCategory": "복선-컴퓨터",
      "field": null,
      "code": 6789012345,
      "name": "컴퓨터학개론",
      "professor": "이수정",
      "department": "컴퓨터학부",
      "timePoints": "3.0/3.0",
      "personeel": "35",
      "scheduleRoom": "화 13:30-14:45 (정보과학관 21002-이수정)\n목 13:30-14:45 (정보과학관 21002-이수정)",
      "target": "컴퓨터학부 전체",
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
      ]
    }
  ]
}

```
