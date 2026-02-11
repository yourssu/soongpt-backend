# 다전공/부전공 트랙 조회 (GET /api/courses/by-track)

특정 학과의 다전공/부전공 과목을 트랙별로 조회합니다. (1~5학년)

## Request
### Query Parameters
- `schoolId` (number, required)
- `department` (string, required)
- `trackType` (string, required)
  - `DOUBLE_MAJOR` | `MINOR` | `CROSS_MAJOR`
  - 또는 `복수전공` | `부전공` | `타전공인정`
- `completionType` (string, optional)
  - `REQUIRED` | `ELECTIVE` | `RECOGNIZED`
  - 또는 `필수` | `선택` | `타전공인정`

## Example
```http
GET /api/courses/by-track?schoolId=26&department=컴퓨터학부&trackType=DOUBLE_MAJOR&completionType=REQUIRED
```

## Response
- `Response<List<CourseResponse>>`
