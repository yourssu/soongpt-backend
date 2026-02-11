# 강의 필터링 조회 (GET /api/courses/by-category)

카테고리/학과/학년 등의 조건으로 수강 가능한 과목을 필터링 조회합니다.

## Request

### Query Parameters
- `schoolId` (number, required)
- `department` (string, required)
- `grade` (number, required, 1~5)
- `category` (string, optional)
  - `MAJOR_REQUIRED` | `MAJOR_ELECTIVE` | `MAJOR_BASIC` | `GENERAL_REQUIRED` | `GENERAL_ELECTIVE` | `CHAPEL` | `TEACHING` | `OTHER`
- `field` (string, optional)
- `subDepartment` (string, optional)

## Example
```http
GET /api/courses/by-category?schoolId=26&department=컴퓨터학부&grade=3&category=MAJOR_REQUIRED
```

## Response
- `Response<List<CourseResponse>>`
