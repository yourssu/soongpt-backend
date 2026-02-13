-- [PT-162] 강의실이 없는 경우 course_time.room 을 빈 문자열('') 대신 NULL 로 저장
--
-- 배경:
-- - schedule_room 이 "(-교수)" 형태로 내려오는 과목이 존재하며(강의실 정보 없음)
-- - 일부 환경에서는 room 을 '' 로 저장하고 있어, 강의실 누락을 NULL 로 통일하려 함

UPDATE course_time
SET room = NULL
WHERE room IS NOT NULL AND TRIM(room) = '';
