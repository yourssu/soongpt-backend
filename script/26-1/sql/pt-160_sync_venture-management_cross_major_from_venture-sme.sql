-- PT-160: 벤처경영학과 타전공인정(CROSS_MAJOR/RECOGNIZED) 분류를 벤처중소기업학과와 동일하게 맞춤
--
-- 배경
-- - course_secondary_major_classification 에서 벤처경영학과(13)의 CROSS_MAJOR/RECOGNIZED가 비어 있어
--   /api/courses/by-track?trackType=CROSS_MAJOR 조회가 빈 목록으로 내려옴
-- - 제공된 엑셀(이수구분(주전공)=전선-벤처중소) 기준으로는 벤처중소기업학과의 인정 목록(122개)과 동일
-- - 따라서 벤처경영학과도 동일 목록을 노출하도록 분류를 복사
--
-- 주의
-- - idempotent: NOT EXISTS 조건으로 중복 삽입 방지
-- - raw_classification/raw_department_token은 원본(벤처중소기업학과) 값을 그대로 복사

INSERT INTO course_secondary_major_classification
    (course_code, track_type, completion_type, department_id, raw_classification, raw_department_token)
SELECT
    s.course_code,
    s.track_type,
    s.completion_type,
    vm.id AS department_id,
    s.raw_classification,
    s.raw_department_token
FROM course_secondary_major_classification s
JOIN department src ON src.id = s.department_id AND src.name = '벤처중소기업학과'
JOIN department vm  ON vm.name = '벤처경영학과'
WHERE s.track_type = 'CROSS_MAJOR'
  AND s.completion_type = 'RECOGNIZED'
  AND NOT EXISTS (
      SELECT 1
      FROM course_secondary_major_classification t
      WHERE t.department_id = vm.id
        AND t.track_type = s.track_type
        AND t.completion_type = s.completion_type
        AND t.course_code = s.course_code
  );
