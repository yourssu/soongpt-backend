-- PT-207: Update chapel target policy (2026) via DB targets
--
-- Goal
-- - Keep API/business logic unchanged (target allow/deny 기반)
-- - Fix chapel recommendation eligibility by adjusting `target` rows
--
-- Policy (요청 사항)
-- - 10채플(…10)을 제외하고, 01~09 분반은 **2~4학년 전체 수강 가능**하도록 target 정책을 수정한다.
--   (즉, 단과대/학과별 allow/deny 대신 전교생(UNIVERSITY) allow를 추가해 열어준다)
--
-- Notes
-- - scope_type / student_type 는 코드(ordinal)로 저장됨
--   - scope_type: UNIVERSITY=0
--   - student_type: GENERAL=0

START TRANSACTION;

-- 1) 01~09 분반을 2~4학년 전교생(GENERAL) 수강 가능으로 오픈
--
-- 실DB(soongpt_dev) 확인 결과, 해당 분반(course_code=2150101501~1510)은 division 컬럼이 null인 경우가 있어
-- division 기반 필터 대신 "코드 범위"로 정책을 적용한다.
--
-- 적용 대상: 2150101501 ~ 2150101509
-- 제외:      2150101510 (10채플)
--
-- 기존 UNIVERSITY scope GENERAL allow가 있으면 중복을 피하기 위해 삭제 후 재삽입
DELETE FROM target
WHERE course_code BETWEEN 2150101501 AND 2150101509
  AND scope_type = 0
  AND student_type = 0
  AND is_denied = 0;

-- 2~4학년 GENERAL 전교생 allow 추가
INSERT INTO target (
  course_code, scope_type, college_id, department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, student_type, is_strict
)
SELECT
  c.code,
  0 AS scope_type,
  NULL AS college_id,
  NULL AS department_id,
  0 AS grade1,
  1 AS grade2,
  1 AS grade3,
  1 AS grade4,
  0 AS grade5,
  0 AS is_denied,
  0 AS student_type,
  0 AS is_strict
FROM course c
WHERE c.code BETWEEN 2150101501 AND 2150101509;

-- 2) 10채플(…10)은 제외: target 변경 없음 (기존 외국인 2~3 허용 등 유지)

COMMIT;
