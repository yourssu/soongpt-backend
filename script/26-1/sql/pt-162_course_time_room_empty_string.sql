-- [PT-162] 강의실이 없는 경우 course_time.room 을 NULL 대신 빈 문자열('')로 저장
--
-- 배경:
-- - schedule_room 이 "(-교수)" 형태로 내려오는 과목이 존재하며(강의실 정보 없음)
-- - 기존 파싱/적재 로직에서는 room 을 NULL 로 저장하고 있었음
-- - FE/분석 편의를 위해 room 을 항상 문자열로 유지하도록('')로 통일

UPDATE course_time
SET room = ''
WHERE room IS NULL;
