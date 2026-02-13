-- [PT-176] "한반도평화와통일" field 누락 보정 검증

SET NAMES utf8mb4;

SELECT code, name, category, sub_category, field
FROM course
WHERE name = '한반도평화와통일'
ORDER BY code;
