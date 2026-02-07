from __future__ import annotations

import sys
import unittest
from pathlib import Path

MODULE_DIR = Path(__file__).resolve().parents[1]
if str(MODULE_DIR) not in sys.path:
    sys.path.insert(0, str(MODULE_DIR))

from decision_engine import DecisionEngine
from schemas import AiValidationResult, TargetRef, ValidationIssue


class DecisionEngineTest(unittest.TestCase):
    def setUp(self) -> None:
        self.engine = DecisionEngine(ai_confidence_threshold=0.8)
        self.parsed = [TargetRef(department="컴퓨터학부", grade=2)]

    def test_blocked_when_rule_error_exists(self) -> None:
        issues = [
            ValidationIssue(
                code="UNPARSED_TOKENS",
                severity="ERROR",
                message="error",
                row_no=1,
                course_code="C1",
            )
        ]
        status, reason, targets = self.engine.decide(self.parsed, issues, None)
        self.assertEqual(status, "BLOCKED")
        self.assertEqual(reason, "RULE_VIOLATION")
        self.assertEqual(targets, self.parsed)

    def test_manual_review_when_ai_warn(self) -> None:
        ai_result = AiValidationResult(
            verdict="WARN",
            normalized_targets=[TargetRef(department="컴퓨터학부", grade=3)],
            confidence=0.9,
            reason="ambiguous",
        )
        status, reason, targets = self.engine.decide(self.parsed, [], ai_result)
        self.assertEqual(status, "MANUAL_REVIEW")
        self.assertEqual(reason, "AI_WARN_WITH_AUTOFIX_CANDIDATE")
        self.assertEqual(targets, ai_result.normalized_targets)

    def test_pass_when_ai_ok(self) -> None:
        ai_result = AiValidationResult(
            verdict="OK",
            normalized_targets=[TargetRef(department="컴퓨터학부", grade=3)],
            confidence=0.95,
            reason="ok",
        )
        status, reason, targets = self.engine.decide(self.parsed, [], ai_result)
        self.assertEqual(status, "PASS")
        self.assertEqual(reason, "AI_OK_WITH_NORMALIZATION")
        self.assertEqual(targets, ai_result.normalized_targets)


if __name__ == "__main__":
    unittest.main()
