-- Create course_field table
CREATE TABLE IF NOT EXISTS course_field (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code BIGINT NOT NULL UNIQUE,
    course_name VARCHAR(255) NOT NULL,
    field VARCHAR(255) NOT NULL,
    INDEX idx_course_field_code (course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
