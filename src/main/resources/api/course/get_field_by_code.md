# 과목 코드로 필드 조회 (GET /api/courses/field-by-code)

과목 코드 리스트와 학번(schoolId)을 받아 각 과목의 field를 반환합니다.

## Request
### Query Parameters
- `code` (number[], required)
- `schoolId` (number, required)

## Example
```http
GET /api/courses/field-by-code?schoolId=26&code=2150118601&code=5022706701
```

## Response
- `Response<Map<Long, String?>>`
