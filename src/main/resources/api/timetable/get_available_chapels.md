# 수강 가능한 채플 과목 목록 조회 (GET /api/timetables/{id}/available-chapels)

특정 시간표를 기준으로 시간이 겹치지 않는 채플 과목 목록을 반환합니다.

## Request
- Path: `id` (number)

## Example
```http
GET /api/timetables/123/available-chapels
```

## Response
- `Response<List<TimetableCourseResponse>>`
