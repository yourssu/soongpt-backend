-- [PT-176] 교양필수(GENERAL_REQUIRED) "한반도평화와통일" field 누락 보정
--
-- 배경
-- - 일부 분반(예: 2150663401, 2150663402)의 course.field가 빈 문자열로 적재되어
--   교양필수 추천/분야(field) 기반 처리에서 제외되는 문제가 발생.
-- - 숭실대 공식 교양필수 안내 페이지(2023학년도 이후 입학자 기준)에 "한반도평화와통일"이 교양필수로 명시됨.
--   https://ssu.ac.kr/학사/교육·교과과정/교과과정/교양필수/
-- - 기존 교필 field 포맷(예: "교필-['23이후]품격(글로벌시민의식)")과 동일하게 품격 영역으로 매핑.

UPDATE course
SET field = '교필-[''23이후]품격(한반도평화와통일)'
WHERE category = 'GENERAL_REQUIRED'
  AND name = '한반도평화와통일'
  AND (field IS NULL OR TRIM(field) = '');
