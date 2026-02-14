-- PT-167: 검색 API 강의코드·개설학과 검색 확장
-- 기존 (name, professor) 인덱스 DROP → (name, professor, department) 인덱스 생성

DROP INDEX idx_course_name_professor ON course;

CREATE FULLTEXT INDEX idx_course_name_professor_department
    ON course (name, professor, department)
    WITH PARSER ngram;

-- idx_course_fulltext(5컬럼)은 그대로 유지 (다른 용도에서 쓸 수 있음)
