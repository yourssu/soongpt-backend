-- [PT-162] course_time.room '' -> NULL 적용 확인

SELECT
  SUM(room IS NULL) AS room_is_null,
  SUM(room = '') AS room_is_empty,
  COUNT(*) AS total
FROM course_time;

-- 예시: 강의실이 없는 과목(2150046001)
SELECT course_code, day_of_week, start_minute, end_minute, room
FROM course_time
WHERE course_code = 2150046001
ORDER BY day_of_week, start_minute;
