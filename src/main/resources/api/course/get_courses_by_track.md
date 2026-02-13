# 다전공/부전공 트랙 조회 (GET /api/courses/by-track)

특정 학과의 다전공/부전공 과목을 트랙별로 조회합니다. (1~5학년)

> 조회 기준
> - `DOUBLE_MAJOR`, `MINOR`: `target`의 Allow-Deny / scope / grade 조건을 적용한 실수강 가능 기준
> - `CROSS_MAJOR`: `course_secondary_major_classification` 원본 분류 기준(타전공 인정 목록). `target` 필터는 적용하지 않음

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
