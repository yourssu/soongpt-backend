# 관리자 과목 API (prefix: /api/admin/courses)

모든 요청은 `X-Admin-Password` 헤더가 필요할 수 있습니다.

## 1) 전체 과목 조회
GET `/api/admin/courses`
- Query: `q`(optional), `page`(default 0), `size`(default 20), `sort`(ASC|DESC)

## 2) 과목 수강 대상 조회
GET `/api/admin/courses/{code}/target`

## 3) 과목 정보 수정
PUT `/api/admin/courses/{code}`
- Header: `X-Admin-Password`

## 4) 과목 수강대상 전체 수정
PUT `/api/admin/courses/{code}/target`
- Header: `X-Admin-Password`

## 5) 과목 추가
POST `/api/admin/courses`
- Header: `X-Admin-Password`

## 6) 과목 삭제
DELETE `/api/admin/courses/{code}`
- Header: `X-Admin-Password`
