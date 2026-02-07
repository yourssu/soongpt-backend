CREATE FULLTEXT INDEX idx_course_fulltext
ON course (name, professor, department, target, schedule_room)
WITH PARSER ngram;
