from __future__ import annotations

from dataclasses import asdict, dataclass, field
from typing import Any


@dataclass(frozen=True, order=True)
class TargetRef:
    department: str
    grade: int


@dataclass
class ValidationIssue:
    code: str
    severity: str
    message: str
    row_no: int
    course_code: str
    details: dict[str, Any] = field(default_factory=dict)

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


@dataclass
class AiValidationResult:
    verdict: str
    normalized_targets: list[TargetRef] = field(default_factory=list)
    unknown_terms: list[str] = field(default_factory=list)
    confidence: float = 0.0
    reason: str = ""

    def to_dict(self) -> dict[str, Any]:
        return {
            "verdict": self.verdict,
            "normalized_targets": [asdict(target) for target in self.normalized_targets],
            "unknown_terms": self.unknown_terms,
            "confidence": self.confidence,
            "reason": self.reason,
        }


@dataclass
class RowValidationResult:
    row_no: int
    course_code: str
    raw_target_text: str
    parsed_targets: list[TargetRef] = field(default_factory=list)
    unparsed_tokens: list[str] = field(default_factory=list)
    issues: list[ValidationIssue] = field(default_factory=list)
    ai_result: AiValidationResult | None = None
    status: str = "PASS"
    status_reason: str = ""
    final_targets: list[TargetRef] = field(default_factory=list)

    def to_dict(self) -> dict[str, Any]:
        return {
            "row_no": self.row_no,
            "course_code": self.course_code,
            "raw_target_text": self.raw_target_text,
            "parsed_targets": [asdict(target) for target in self.parsed_targets],
            "unparsed_tokens": self.unparsed_tokens,
            "issues": [issue.to_dict() for issue in self.issues],
            "ai_result": self.ai_result.to_dict() if self.ai_result else None,
            "status": self.status,
            "status_reason": self.status_reason,
            "final_targets": [asdict(target) for target in self.final_targets],
        }


@dataclass
class ValidationSummary:
    total_rows: int = 0
    pass_rows: int = 0
    manual_review_rows: int = 0
    blocked_rows: int = 0
    blocking_errors: int = 0
    unknown_department_count: int = 0
    unparsed_token_rows: int = 0
    coverage_rate: float = 0.0
    ai_invoked_rows: int = 0
    ai_skipped_rows: int = 0
    ai_api_calls: int = 0
    ai_cache_hits: int = 0
    gate_failures: list[str] = field(default_factory=list)

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


@dataclass
class ValidationReport:
    summary: ValidationSummary
    rows: list[RowValidationResult]

    def to_dict(self) -> dict[str, Any]:
        return {
            "summary": self.summary.to_dict(),
            "rows": [row.to_dict() for row in self.rows],
        }
