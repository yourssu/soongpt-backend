# Course Parsing Refactoring Plan

## TL;DR
> **Quick Summary**: Redesigning the course data structure to support "Dept + Grade" eligibility queries.
> **Deliverables**:
> - Design docs: `script/26-1/{ARCHITECTURE.md, Agents.md, PLAN.md}`
> - Backend: New Entities (`Department`, `Course`, `Section`, `Eligibility`), Parser Logic.
> **Estimated Effort**: Medium (3-4 days)

---

## Context
The current system lacks a structured way to filter courses by "Target Audience" (e.g., Computer Science 1st Year). The raw data has 153 courses with section-specific targets. We need a normalized schema to query this efficiently.

---

## Work Objectives
1.  **Define Schema**: Create a normalized DB schema for Courses and Eligibility.
2.  **Document Design**: Create `script/26-1/` design files.
3.  **Implement Parser**: Port parsing logic to Kotlin with a robust "Department Normalizer".

---

## Execution Strategy

### Wave 1: Design Documentation (The Requested Task)
- **Task 1**: Create `script/26-1/ARCHITECTURE.md`
  - Defines the ERD and "CourseEligibility" concept.
- **Task 2**: Create `script/26-1/Agents.md`
  - Defines logical components (Parser, Normalizer, Loader).
- **Task 3**: Create `script/26-1/PLAN.md`
  - Detailed implementation steps for the backend.

### Wave 2: Backend Implementation (Kotlin)
- **Task 4**: Add dependencies (`fastexcel`).
- **Task 5**: Create JPA Entities (`Department`, `Course`, `Section`, `Eligibility`).
- **Task 6**: Implement `DepartmentNormalizer` (String -> Dept ID).
- **Task 7**: Implement `CourseParser` (Excel -> Entities).

---

## TODOs

- [ ] 1. Create Design Documents in `script/26-1/`

  **What to do**:
  - Create `script/26-1/ARCHITECTURE.md` with the ERD and Schema.
  - Create `script/26-1/Agents.md` with the component definitions.
  - Create `script/26-1/PLAN.md` with the implementation checklist.

  **Recommended Agent**:
  - **Category**: `writing`
  - **Skills**: `git-master` (to commit the docs)

  **Content for ARCHITECTURE.md**:
  ```markdown
  # Data Structure Design & Schema

  ## 1. Core Concept: Separation of Concerns
  To solve the "9% discrepancy" and "Dept+Grade" query requirement, we decouple **Course Metadata**, **Execution Details (Section)**, and **Eligibility Rules**.

  ## 2. Entity Relationship Diagram (ERD)

  \`\`\`mermaid
  erDiagram
      Department ||--o{ CourseEligibility : "is eligible for"
      Course ||--|{ CourseSection : "has"
      Course ||--o{ CourseEligibility : "has rules"
      CourseSection ||--o{ SectionSchedule : "has time"

      Department {
          Long id PK
          String name "컴퓨터학부"
          String college "IT대학"
      }

      Course {
          Long id PK
          String code UK "21501050"
          String name "컴퓨터학개론"
          Double credit "3.0"
          String category "전선"
      }

      CourseSection {
          Long id PK
          Long course_id FK
          String section_no "01"
          String professor "김철수"
          Int max_capacity
      }

      CourseEligibility {
          Long id PK
          Long course_id FK
          Long department_id FK
          Int grade_mask "Bitmask: 1=1st, 2=2nd... (e.g., 30 = 2nd|3rd|4th)"
          String restriction_type "MAJOR_REQUIRED / MAJOR_ELECTIVE / GENERAL"
          Boolean is_excluded "If true, this dept CANNOT take it"
      }
  \`\`\`

  ## 3. Key Data Structures

  ### A. CourseEligibility (The Solver)
  This is the critical table for your "Dept + Grade" query.

  - **Purpose**: Fast filtering of courses available to a student.
  - **Structure**:
    - `course_id`: Link to the abstract course.
    - `department_id`: Link to the student's department.
    - `grade_mask`: Integer bitmask.
      - 1st year = 1 (`00001`)
      - 2nd year = 2 (`00010`)
      - 3rd year = 4 (`000100`)
      - ...
      - Example: "2, 3학년 수강 가능" = `2 | 4` = `6` (`00110`)
  - **Query Logic**:
    \`\`\`sql
    -- Find courses for Computer (ID=10) 1st year (Mask=1)
    SELECT c.*
    FROM course c
    JOIN course_eligibility ce ON c.id = ce.course_id
    WHERE ce.department_id = 10
      AND (ce.grade_mask & 1) > 0
    \`\`\`

  ### B. CourseSection (The Timetable)
  Stores physical execution details.

  - **Difference from Course**:
    - `professor`: Lives here (differs by section).
    - `time`: Lives here.
    - `eligibility`: Linked to **Course** (OR-merged) for search performance.

  ## 4. Optimization Strategy (Denormalization)

  For extreme read performance (e.g., "All courses for Me"), we can generate a **Search Document**.

  **Redis/Search Index Structure:**
  Key: `DEPT:{dept_id}:GRADE:{grade}`
  Value: `[course_id_1, course_id_2, ...]`

  This allows O(1) retrieval of the entire course list for a logged-in user.

  ## 5. Domain Logic (Department Mapping)

  The parser must map raw strings in Excel to normalized Department IDs.

  **Mapping Table (Code/Config)**
  | Raw String | Normalized Dept |
  |------------|-----------------|
  | "컴퓨터" | 컴퓨터학부 |
  | "전자" | 전자정보공학부 전자공학전공 |
  | "전체" | (All Departments) |
  | "IT융합" | 전자정보공학부 IT융합전공 |

  This mapping logic belongs in a `DepartmentMapper` domain service.
  ```

  **Content for Agents.md**:
  ```markdown
  # Agents & Logic Units

  This file defines the logical "Agents" (software components) responsible for the parsing and data structuring pipeline.

  ## 1. 🏗️ The Architect (Schema Designer)
  - **Role**: Defines the `Course`, `Section`, and `Eligibility` entities.
  - **Output**: Kotlin Entity classes, SQL Schema.
  - **Key Logic**:
    - Enforces the `One Course -> Many Sections` relationship.
    - Defines the `Bitmask` logic for grades.

  ## 2. 🔍 The Normalizer (Department Mapper)
  - **Role**: Standardizes chaotic department names.
  - **Input**: Raw strings like "기계", "화공", "전기", "컴퓨터".
  - **Output**: `DepartmentID` or Canonical Name.
  - **Logic**:
    - Maintains a Dictionary of Synonyms.
    - Handles "College-level" targets (e.g., "IT대학" -> expands to all depts in IT College).
    - Handles "All" (전체) -> expands to all 48 departments.

  ## 3. 🧩 The Parser (Excel Processor)
  - **Role**: Reads the raw Excel/CSV file and extracts structured objects.
  - **Components**:
    - `RowParser`: Parses a single row into a raw DTO.
    - `TargetParser`: Parses the "수강대상" string using Regex/Heuristics.
    - `ScheduleParser`: Parses "월 15:00-17:15" into standardized TimeBlocks.
  - **Strategy**:
    - **Hybrid Parsing**: Uses both Regex and specific keyword matching.
    - **Section Merging**: Groups rows by 8-digit Course Code to form the `Course` object.

  ## 4. ⚖️ The Arbitrator (Eligibility Calculator)
  - **Role**: Resolves conflicts between sections.
  - **Logic**:
    - **Union Policy**: If Section A is for "Dept X" and Section B is for "Dept Y", the Course is available to "Dept X OR Dept Y".
    - **Current Decision**: **UNION (OR)** logic. Maximizes visibility.

  ## 5. 💾 The Loader (DB Sync)
  - **Role**: Persists the structured data.
  - **Logic**:
    - **Idempotency**: Runs multiple times without duplicating data (Upsert).
    - **Versioning**: Tags data with `Semester (2026-1)` to allow historical data.

  ---

  ## Interaction Flow

  \`\`\`
  [Raw Excel] 
     ⬇️ (Parser)
  [Raw DTOs] 
     ⬇️ (Normalizer)
  [Normalized DTOs (Dept IDs resolved)]
     ⬇️ (Arbitrator)
  [Aggregated Course Model] + [Section Details]
     ⬇️ (Loader)
  [Database]
  \`\`\`
  ```

  **Content for PLAN.md**:
  ```markdown
  # Implementation Plan

  ## Phase 1: Foundation (Schema & Entities)
  - [ ] **Define Department Enum/Entity**:
    - Create `Department` enum or table based on `data.yml`.
    - **Suggestion**: Create `script/26-1/synonyms.yml` to share mappings between Python/Kotlin.
    - Add `synonyms` list to each department (e.g., COMPUTER("컴퓨터학부", ["컴퓨터", "컴학"])).
  - [ ] **Create Entities**:
    - `CourseEntity` (Base info)
    - `CourseSectionEntity` (Time, Prof)
    - `CourseEligibilityEntity` (Dept, Grade Mask)
  - [ ] **Migration**:
    - Generate DDL.
    - Verify against H2/MySQL.

  ## Phase 2: Parsing Logic (Core Algorithm)
  - [ ] **Department Normalizer**:
    - Implement `DepartmentResolver` service.
    - Input: String. Output: `List<Department>`.
    - Logic: Handle "College names" (expand to depts) and "Special names" (e.g., "전체").
  - [ ] **Target Parser**:
    - Input: "1학년 컴퓨터, 2학년 전체".
    - Output: Map of `Dept -> GradeMask`.
    - Regex logic to split by space/comma and associate grades with depts.
  - [ ] **Course Aggregator**:
    - Group sections by Course Code.
    - Merge Eligibility maps (OR operation).

  ## Phase 3: Integration
  - [ ] **Excel Reader**:
    - Add `fastexcel` or `poi` dependency.
    - Implement `ExcelReader` component.
  - [ ] **Service Layer**:
    - `CourseLoadService`: Orchestrates Reader -> Parser -> Repository.
  - [ ] **Controller (Optional)**:
    - Admin endpoint to trigger reload.

  ## Phase 4: Verification
  - [ ] **Unit Tests**:
    - Test `DepartmentResolver` with tricky strings.
    - Test `TargetParser` with edge cases.
  - [ ] **Integration Test**:
    - Load `ssu26-1.csv` sample and query "Computer 1st Year".

  ## Immediate To-Do (Script Prototype)
  Before writing Kotlin code, use the Python script in `script/26-1/` to verify the mapping logic.

  1. Update `main.py` to:
     - Load `data.yml` (Source of Truth for Departments).
     - Parse `ssu26-1.csv` unique "수강대상" strings.
     - Attempt to map them to Departments.
     - Report "Unmapped" strings.
  ```

  **Acceptance Criteria**:
  - [ ] File `script/26-1/ARCHITECTURE.md` exists.
  - [ ] File `script/26-1/Agents.md` exists.
  - [ ] File `script/26-1/PLAN.md` exists.

- [ ] 2. Implement Backend Entities

  **What to do**:
  - Create Kotlin Entity classes in `src/main/kotlin/com/yourssu/soongpt/domain/course/storage/`.
  - Refactor `CourseEntity` to remove flat fields and add relationships.

  **References**:
  - `src/main/kotlin/com/yourssu/soongpt/domain/course/storage/CourseEntity.kt`

  **Acceptance Criteria**:
  - [ ] `CourseEntity` refactored.
  - [ ] `CourseSectionEntity` created.
  - [ ] `CourseEligibilityEntity` created.
  - [ ] `DepartmentEntity` created.

- [ ] 3. Implement Parsing Service

  **What to do**:
  - Create `CourseParsingService`.
  - Implement logic to read Excel and map to new entities.

  **Acceptance Criteria**:
  - [ ] Service can read `ssu26-1.csv` (or xlsx).
  - [ ] Correctly populates the DB tables.

---

## Success Criteria
- [ ] Design documents created in `script/26-1/`.
- [ ] Schema supports "Dept + Grade" querying efficiently.
