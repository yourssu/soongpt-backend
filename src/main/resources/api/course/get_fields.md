# 전공 분야/영역 조회 (GET /api/courses/fields)

학교 ID(schoolId)에 해당하는 field(영역) 목록을 조회합니다.

## Request
### Query Parameters
- `schoolId` (number, required)

## Example
```http
GET /api/courses/fields?schoolId=26
```

## Response
- `Response<Any>`
