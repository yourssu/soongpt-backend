-- PT-60 schema changes
-- 1) course 테이블에 다전공 원본 이수구분 컬럼 추가
-- 2) 복수전공/부전공/타전공인정 분류 정규화 테이블 추가

ALTER TABLE course
    ADD COLUMN IF NOT EXISTS multi_major_category TEXT NULL;

CREATE TABLE IF NOT EXISTS course_secondary_major_classification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code BIGINT NOT NULL,
    track_type VARCHAR(32) NOT NULL,
    completion_type VARCHAR(32) NOT NULL,
    department_id BIGINT NOT NULL,
    raw_classification VARCHAR(64) NOT NULL,
    raw_department_token VARCHAR(100) NOT NULL,

    CONSTRAINT fk_csmc_course_code
        FOREIGN KEY (course_code) REFERENCES course(code),
    CONSTRAINT fk_csmc_department
        FOREIGN KEY (department_id) REFERENCES department(id),

    CONSTRAINT uk_csmc_course_track_completion_department
        UNIQUE (course_code, track_type, completion_type, department_id),

    CONSTRAINT chk_csmc_track_type
        CHECK (track_type IN ('DOUBLE_MAJOR', 'MINOR', 'CROSS_MAJOR')),
    CONSTRAINT chk_csmc_completion_type
        CHECK (completion_type IN ('REQUIRED', 'ELECTIVE', 'RECOGNIZED'))
);

CREATE INDEX idx_csmc_lookup
    ON course_secondary_major_classification(track_type, completion_type, department_id, course_code);

CREATE INDEX idx_csmc_course_code
    ON course_secondary_major_classification(course_code);
