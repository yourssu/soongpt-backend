# 수강 가능한 교양 과목 목록 조회 (GET /api/timetables/{id}/available-general-electives)

특정 시간표를 기준으로 시간이 겹치지 않는 교양 과목을 field별로 그룹화하여 반환합니다.

## Request
- Path: `id` (number)

## Example
```http
GET /api/timetables/123/available-general-electives
```

## Response
- `Response<List<GeneralElectiveDto>>`
