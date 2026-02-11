# API Documentation

## Course

[전공 필수 과목 조회 (GET /api/courses/major/required)](course/get_major_required_courses.md)
[전공 선택 과목 조회 (GET /api/courses/major/elective)](course/get_major_elective_courses.md)
[교양 필수 과목 조회 (GET /api/courses/general/required)](course/get_general_required_courses.md)
[복수/부전공 과목 추천 (GET /api/courses/secondary-major/recommend)](course/get_secondary_major_courses.md)
[교직 과목 조회 (GET /api/courses/teaching)](course/get_teaching_courses.md)
[과목 단일 조회 (GET /api/courses)](course/get_courses_by_code.md)
[과목 검색 (GET /api/courses/search)](course/search_course.md)
[학번별 필드 조회 (GET /api/courses/fields/schoolId/{schoolId})](course/get_fields_by_school_id.md)

## SSO

[SSO 콜백 (GET /api/sso/callback)](sso/sso_callback.md)
[동기화 상태 조회 (GET /api/sync/status)](sso/sync_status.md)
[학적정보 수정 (PUT /api/sync/student-info)](sso/update_student_info.md)

## TimeTable

[시간표 추천 생성 (POST /api/timetables)](timetable/create_recommendation.md)
[최종 시간표 확정 (POST /api/timetables/finalize)](timetable/finalize_timetable.md)
[시간표 조회 (GET /api/timetables/{timetableId})](timetable/get_timetable_id.md)
[재수강 가능 과목 조회 (GET /api/usaint/retake)](usaint/get_retake_courses.md)

## Internal

[유세인트 데이터 스냅샷 조회 (POST /api/usaint/snapshot)](internal/fetch_usaint_snapshot.md)

## Contact

[연락처 생성 (POST /api/contacts)](contact/create_contact.md)
