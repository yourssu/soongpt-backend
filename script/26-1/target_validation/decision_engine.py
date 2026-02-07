from __future__ import annotations

from schemas import AiValidationResult, TargetRef, ValidationIssue


class DecisionEngine:
    def __init__(self, ai_confidence_threshold: float = 0.8) -> None:
        self.ai_confidence_threshold = ai_confidence_threshold

    def decide(
        self,
        parsed_targets: list[TargetRef],
        issues: list[ValidationIssue],
        ai_result: AiValidationResult | None,
    ) -> tuple[str, str, list[TargetRef]]:
        if any(issue.severity == "ERROR" for issue in issues):
            return "BLOCKED", "RULE_VIOLATION", parsed_targets

        if ai_result is None:
            return "PASS", "AI_NOT_CONFIGURED", parsed_targets

        if ai_result.verdict == "FAIL":
            return "BLOCKED", "AI_FAIL", parsed_targets

        if ai_result.verdict == "WARN":
            if ai_result.confidence >= self.ai_confidence_threshold and ai_result.normalized_targets:
                return "MANUAL_REVIEW", "AI_WARN_WITH_AUTOFIX_CANDIDATE", ai_result.normalized_targets
            return "MANUAL_REVIEW", "AI_WARN_LOW_CONFIDENCE", parsed_targets

        if ai_result.verdict == "OK":
            if ai_result.confidence >= self.ai_confidence_threshold and ai_result.normalized_targets:
                return "PASS", "AI_OK_WITH_NORMALIZATION", ai_result.normalized_targets
            return "PASS", "AI_OK", parsed_targets

        return "PASS", "AI_SKIPPED", parsed_targets
