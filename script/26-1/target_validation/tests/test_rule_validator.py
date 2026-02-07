from __future__ import annotations

import sys
import unittest
from pathlib import Path

MODULE_DIR = Path(__file__).resolve().parents[1]
if str(MODULE_DIR) not in sys.path:
    sys.path.insert(0, str(MODULE_DIR))

from rule_validator import RuleValidator
from schemas import TargetRef


class RuleValidatorTest(unittest.TestCase):
    def setUp(self) -> None:
        self.validator = RuleValidator(allowed_departments=["소프트웨어학부", "컴퓨터학부"])

    def test_unknown_department_error(self) -> None:
        issues = self.validator.validate(
            row_no=2,
            course_code="C101",
            raw_target_text="미등록학과 2학년",
            parsed_targets=[TargetRef(department="미등록학과", grade=2)],
            unparsed_tokens=[],
        )
        codes = {issue.code for issue in issues}
        self.assertIn("UNKNOWN_DEPARTMENT", codes)

    def test_raw_grade_out_of_range_error(self) -> None:
        issues = self.validator.validate(
            row_no=3,
            course_code="C102",
            raw_target_text="컴퓨터학부 6학년",
            parsed_targets=[TargetRef(department="컴퓨터학부", grade=6)],
            unparsed_tokens=[],
        )
        codes = {issue.code for issue in issues}
        self.assertIn("RAW_GRADE_OUT_OF_RANGE", codes)
        self.assertIn("GRADE_OUT_OF_RANGE", codes)

    def test_raw_grade_out_of_range_detects_range_boundary(self) -> None:
        issues = self.validator.validate(
            row_no=4,
            course_code="C103",
            raw_target_text="컴퓨터학부 0~2학년",
            parsed_targets=[TargetRef(department="컴퓨터학부", grade=1), TargetRef(department="컴퓨터학부", grade=2)],
            unparsed_tokens=[],
        )
        raw_issue = next((issue for issue in issues if issue.code == "RAW_GRADE_OUT_OF_RANGE"), None)
        self.assertIsNotNone(raw_issue)
        self.assertEqual(raw_issue.details.get("grades"), [0])

    def test_unparsed_tokens_error(self) -> None:
        issues = self.validator.validate(
            row_no=5,
            course_code="C104",
            raw_target_text="컴퓨터학부 2학년 우선",
            parsed_targets=[TargetRef(department="컴퓨터학부", grade=2)],
            unparsed_tokens=["우선"],
        )
        code_by_severity = {(issue.code, issue.severity) for issue in issues}
        self.assertIn(("UNPARSED_QUALIFIERS", "WARN"), code_by_severity)

    def test_unparsed_qualifier_warn_when_targets_exist(self) -> None:
        issues = self.validator.validate(
            row_no=6,
            course_code="C105",
            raw_target_text="컴퓨터학부 2학년 (대상외수강제한)",
            parsed_targets=[TargetRef(department="컴퓨터학부", grade=2)],
            unparsed_tokens=["대상외수강제한"],
        )
        code_by_severity = {(issue.code, issue.severity) for issue in issues}
        self.assertIn(("UNPARSED_QUALIFIERS", "WARN"), code_by_severity)


if __name__ == "__main__":
    unittest.main()
