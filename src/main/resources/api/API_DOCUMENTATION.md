# API Documentation

## Course

[전공 필수 과목 조회 (GET /api/courses/major/required)](course/get_major_required_courses.md)
[전공 선택 과목 조회 (GET /api/courses/major/elective)](course/get_major_elective_courses.md)
[교양 필수 과목 조회 (GET /api/courses/general/required)](course/get_general_required_courses.md)
[과목 단일 조회 (GET /api/courses)](course/get_courses_by_code.md)
[과목 검색 (GET /api/courses/search)](course/search_course.md)
[학번별 필드 조회 (GET /api/courses/fields/schoolId/{schoolId})](course/get_fields_by_school_id.md)

## TimeTable

[유세인트기반 시간표 생성 (POST /api/timetables/usaint)](timetable/create_timetable_usaint.md)
[유세인트 정보 업데이트 (POST /api/usaint/snapshot/refresh)](timetable/refresh_usaint_snapshot.md)
[수동 시간표 생성 (POST /api/timetables/manual)](timetable/create_timetable_manual.md)
[시간표 조회 (GET /api/timetables/{timetableId})](timetable/get_timetable_id.md)

## Internal

[유세인트 데이터 스냅샷 조회 (POST /api/usaint/snapshot)](internal/fetch_usaint_snapshot.md)

## Contact

[연락처 생성 (POST /api/contacts)](contact/create_contact.md)
