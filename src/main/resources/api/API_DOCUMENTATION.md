# API Documentation

## Course

- [강의 필터링 조회 (GET /api/courses/by-category)](course/get_courses_by_category.md)
- [과목 단일 조회 (GET /api/courses)](course/get_courses_by_code.md)
- [과목 검색 (GET /api/courses/search)](course/search_course.md)
- [전공 분야/영역 조회 (GET /api/courses/fields)](course/get_fields.md)
- [과목 코드로 필드 조회 (GET /api/courses/field-by-code)](course/get_field_by_code.md)
- [다전공/부전공 트랙 조회 (GET /api/courses/by-track)](course/get_courses_by_track.md)
- [교직 과목 조회 (GET /api/courses/teaching)](course/get_teaching_courses.md)
- [통합 과목 추천 조회 (GET /api/courses/recommend/all)](course/get_recommend_all.md)

## Admin

- [관리자 과목 API (prefix: /api/admin/courses)](admin/admin_courses.md)

## SSO

- [SSO 콜백 (GET /api/sso/callback)](sso/sso_callback.md)
- [동기화 상태 조회 (GET /api/sync/status)](sso/sync_status.md)
- [학적정보 수정 (PUT /api/sync/student-info)](sso/update_student_info.md)

## Timetable

- [시간표 추천 생성 (POST /api/timetables)](timetable/create_recommendation.md)
- [최종 시간표 확정 (POST /api/timetables/finalize)](timetable/finalize_timetable.md)
- [시간표 조회 (GET /api/timetables/{timetableId})](timetable/get_timetable_id.md)
- [수강 가능한 교양 과목 목록 조회 (GET /api/timetables/{id}/available-general-electives)](timetable/get_available_general_electives.md)
- [수강 가능한 채플 과목 목록 조회 (GET /api/timetables/{id}/available-chapels)](timetable/get_available_chapels.md)

## Contact

- [연락처 생성 (POST /api/contacts)](contact/create_contact.md)
