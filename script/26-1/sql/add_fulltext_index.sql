-- Adds FULLTEXT index required by backend queries.
--
-- MySQL requires the MATCH() column list to match a FULLTEXT index definition exactly.
-- Example: MATCH(name, professor) AGAINST (...) needs a FULLTEXT(name, professor) index.
--
-- This script is idempotent: it creates the needed index only if it doesn't already exist.

SET @has_fulltext_name_professor := (
  SELECT COUNT(*)
  FROM (
    SELECT index_name
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'course'
      AND index_type = 'FULLTEXT'
    GROUP BY index_name
    HAVING GROUP_CONCAT(column_name ORDER BY seq_in_index) = 'name,professor'
  ) t
);

SET @sql := IF(
  @has_fulltext_name_professor = 0,
  'ALTER TABLE course ADD FULLTEXT INDEX idx_course_name_professor (name, professor) WITH PARSER ngram',
  'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- NOTE:
-- If you also want a broader FULLTEXT index (e.g., department/target/schedule_room),
-- add it separately. For example:
--   ALTER TABLE course
--     ADD FULLTEXT INDEX idx_course_fulltext (name, professor, department, target, schedule_room)
--     WITH PARSER ngram;
